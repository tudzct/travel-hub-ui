package com.mobile.travelhub.data

import com.mobile.travelhub.data.api.ApplyItineraryAiProposalRequestDto
import com.mobile.travelhub.data.api.CreateItineraryAiProposalRequestDto
import com.mobile.travelhub.data.api.CreateItineraryDayRequestDto
import com.mobile.travelhub.data.api.CreateItineraryRequestDto
import com.mobile.travelhub.data.api.ItineraryAiChangeResponseDto
import com.mobile.travelhub.data.api.ItineraryAiDayDraftDto
import com.mobile.travelhub.data.api.ItineraryAiProposalResponseDto
import com.mobile.travelhub.data.api.ItineraryAiStopDraftDto
import com.mobile.travelhub.data.api.ItineraryApiService
import com.mobile.travelhub.data.api.UpdateItineraryDayRequestDto
import com.mobile.travelhub.data.api.UpsertItineraryStopRequestDto
import com.mobile.travelhub.data.model.AddDayChange
import com.mobile.travelhub.data.model.AddEventChange
import com.mobile.travelhub.data.model.DeleteDayChange
import com.mobile.travelhub.data.model.DeleteEventChange
import com.mobile.travelhub.data.model.FieldDiff
import com.mobile.travelhub.data.model.ItineraryAssistantEvent
import com.mobile.travelhub.data.model.ItineraryChange
import com.mobile.travelhub.data.model.ItineraryDay
import com.mobile.travelhub.data.model.ItineraryDayResponse
import com.mobile.travelhub.data.model.ItineraryEvent
import com.mobile.travelhub.data.model.ItineraryEventColors
import com.mobile.travelhub.data.model.ItineraryField
import com.mobile.travelhub.data.model.ItineraryProposal
import com.mobile.travelhub.data.model.ItineraryResponse
import com.mobile.travelhub.data.model.ItineraryStopResponse
import com.mobile.travelhub.data.model.ItineraryUserRole
import com.mobile.travelhub.data.model.ItineraryWorkspace
import com.mobile.travelhub.data.model.MoveEventChange
import com.mobile.travelhub.data.model.UpdateDayChange
import com.mobile.travelhub.data.model.UpdateEventChange
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.update
import retrofit2.HttpException

@Singleton
class ItineraryRepository @Inject constructor(
    private val itineraryApiService: ItineraryApiService
) {
    private val workspaces = linkedMapOf<String, MutableStateFlow<ItineraryWorkspace>>()
    private val itineraryIdsByGroup = mutableMapOf<String, Long>()

    fun observeWorkspace(groupName: String): StateFlow<ItineraryWorkspace> {
        return workspaceState(groupName)
    }

    suspend fun refreshWorkspace(groupName: String, tripId: Long? = null) {
        val itinerary = loadOrCreateItinerary(groupName, tripId)
        cacheItinerary(groupName, itinerary, pendingProposal = workspaceState(groupName).value.pendingProposal)
    }

    fun streamProposal(
        groupName: String,
        tripId: Long? = null,
        prompt: String,
        selectedDayIndex: Int,
        inputType: String = "TEXT"
    ): Flow<ItineraryAssistantEvent> = flow {
        try {
            emit(ItineraryAssistantEvent.Thinking("Preparing itinerary context..."))
            val itinerary = loadOrCreateItinerary(groupName, tripId)
            cacheItinerary(groupName, itinerary)
            val workspace = workspaceState(groupName).value
            val selectedDay = workspace.days.firstOrNull { it.dayIndex == selectedDayIndex }

            emit(ItineraryAssistantEvent.Thinking("Generating a reviewable itinerary proposal..."))
            val proposal = itineraryApiService.createAiProposal(
                itineraryId = itinerary.id,
                request = CreateItineraryAiProposalRequestDto(
                    prompt = prompt,
                    inputType = inputType,
                    selectedDayId = selectedDay?.dayId,
                    selectedDayIndex = selectedDayIndex,
                    desiredDays = if (workspace.days.isEmpty()) 3 else null,
                    destination = groupName,
                    task = if (workspace.days.isEmpty()) "GENERATE_ITINERARY" else "EDIT_ITINERARY",
                    baseVersion = workspace.version
                )
            ).toDomain()

            workspaceState(groupName).update { it.copy(pendingProposal = proposal) }
            emit(ItineraryAssistantEvent.MessageChunk(proposal.summary))
            emit(ItineraryAssistantEvent.ProposalReady(proposal))
            emit(ItineraryAssistantEvent.Done)
        } catch (throwable: Throwable) {
            emit(ItineraryAssistantEvent.Error(throwable.message ?: "Unable to generate itinerary proposal"))
            emit(ItineraryAssistantEvent.Done)
        }
    }

    suspend fun applyProposalChanges(
        groupName: String,
        proposalId: String,
        selectedChangeIds: Set<String>,
        baseVersion: Int
    ): Result<Unit> = runCatching {
        if (selectedChangeIds.isEmpty()) {
            error("No changes selected")
        }
        val itineraryId = itineraryId(groupName)
        val itinerary = itineraryApiService.applyAiProposal(
            itineraryId = itineraryId,
            proposalId = proposalId,
            request = ApplyItineraryAiProposalRequestDto(
                selectedChangeIds = selectedChangeIds.toList(),
                baseVersion = baseVersion
            )
        )
        cacheItinerary(groupName, itinerary, pendingProposal = null)
    }

    fun discardPendingProposal(groupName: String) {
        workspaceState(groupName).update { it.copy(pendingProposal = null) }
    }

    suspend fun updateEvent(groupName: String, updatedEvent: ItineraryEvent) {
        val itineraryId = itineraryId(groupName)
        val targetDay = findDay(groupName, dayId = null, dayIndex = updatedEvent.dayIndex)
        val request = updatedEvent.toUpsertRequest(targetDay.dayId ?: error("Day id is missing"))
        val updatedItinerary = updatedEvent.stopId?.let { stopId ->
            itineraryApiService.updateStop(itineraryId, stopId, request)
        } ?: itineraryApiService.createStop(itineraryId, request)
        cacheItinerary(groupName, updatedItinerary, pendingProposal = null)
    }

    suspend fun updateDay(groupName: String, updatedDay: ItineraryDay) {
        val itineraryId = itineraryId(groupName)
        val day = findDay(groupName, updatedDay.dayId, updatedDay.dayIndex)
        val updatedItinerary = itineraryApiService.updateDay(
            itineraryId = itineraryId,
            dayId = day.dayId ?: error("Day id is missing"),
            request = UpdateItineraryDayRequestDto(
                label = updatedDay.label,
                dateLabel = updatedDay.dateLabel
            )
        )
        cacheItinerary(groupName, updatedItinerary, pendingProposal = null)
    }

    suspend fun deleteEvent(groupName: String, eventId: String) {
        val stopId = eventId.toLongOrNull() ?: return
        val updatedItinerary = itineraryApiService.deleteStop(itineraryId(groupName), stopId)
        cacheItinerary(groupName, updatedItinerary, pendingProposal = null)
    }

    suspend fun deleteDay(groupName: String, dayIndex: Int) {
        val day = findDay(groupName, null, dayIndex)
        val updatedItinerary = itineraryApiService.deleteDay(
            itineraryId = itineraryId(groupName),
            dayId = day.dayId ?: error("Day id is missing")
        )
        cacheItinerary(groupName, updatedItinerary, pendingProposal = null)
    }

    suspend fun addDay(groupName: String): Int {
        val workspace = workspaceState(groupName).value
        val nextIndex = (workspace.days.maxOfOrNull { it.dayIndex } ?: 0) + 1
        val updatedItinerary = itineraryApiService.createDay(
            itineraryId = itineraryId(groupName),
            request = CreateItineraryDayRequestDto(
                label = "Day $nextIndex",
                dateLabel = nextDateLabel(workspace.days.maxByOrNull { it.dayIndex }?.dateLabel, nextIndex)
            )
        )
        cacheItinerary(groupName, updatedItinerary, pendingProposal = null)
        return nextIndex
    }

    suspend fun ensureDay(
        groupName: String,
        dayIndex: Int,
        label: String,
        dateLabel: String
    ): ItineraryDay {
        workspaceState(groupName).value.days.firstOrNull { it.dayIndex == dayIndex }?.let { return it }

        var workspace = workspaceState(groupName).value
        while (workspace.days.none { it.dayIndex == dayIndex }) {
            val nextIndex = (workspace.days.maxOfOrNull { it.dayIndex } ?: 0) + 1
            val nextLabel = if (nextIndex == dayIndex) label else "Day $nextIndex"
            val nextDateLabel = if (nextIndex == dayIndex) {
                dateLabel
            } else {
                nextDateLabel(workspace.days.maxByOrNull { it.dayIndex }?.dateLabel, nextIndex)
            }
            val updatedItinerary = itineraryApiService.createDay(
                itineraryId = itineraryId(groupName),
                request = CreateItineraryDayRequestDto(
                    label = nextLabel,
                    dateLabel = nextDateLabel
                )
            )
            cacheItinerary(groupName, updatedItinerary, pendingProposal = null)
            workspace = workspaceState(groupName).value
        }

        return workspaceState(groupName).value.days.first { it.dayIndex == dayIndex }
    }

    suspend fun reorderEvent(groupName: String, dayIndex: Int, eventId: String, moveUp: Boolean) {
        val day = workspaceState(groupName).value.days.firstOrNull { it.dayIndex == dayIndex } ?: return
        val currentIndex = day.events.indexOfFirst { it.eventId == eventId }
        if (currentIndex == -1) return
        val targetIndex = if (moveUp) currentIndex - 1 else currentIndex + 1
        reorderEvents(groupName, dayIndex, currentIndex, targetIndex)
    }

    suspend fun reorderEvents(groupName: String, dayIndex: Int, fromIndex: Int, toIndex: Int) {
        if (fromIndex == toIndex) return
        val day = workspaceState(groupName).value.days.firstOrNull { it.dayIndex == dayIndex } ?: return
        if (fromIndex !in day.events.indices || toIndex !in day.events.indices) return
        val moved = day.events[fromIndex]
        val targetDayId = day.dayId ?: return
        val request = moved.toUpsertRequest(targetDayId, sortOrder = toIndex + 1)
        val stopId = moved.stopId ?: return
        val updatedItinerary = itineraryApiService.updateStop(itineraryId(groupName), stopId, request)
        cacheItinerary(groupName, updatedItinerary, pendingProposal = null)
    }

    private fun workspaceState(groupName: String): MutableStateFlow<ItineraryWorkspace> {
        return workspaces.getOrPut(groupName) {
            MutableStateFlow(
                ItineraryWorkspace(
                    groupName = groupName,
                    version = 0,
                    role = ItineraryUserRole.LEADER,
                    days = emptyList()
                )
            )
        }
    }

    private suspend fun loadOrCreateItinerary(groupName: String, tripId: Long? = null): ItineraryResponse {
        return try {
            itineraryApiService.getByGroupName(groupName)
        } catch (error: HttpException) {
            if (error.code() != 404) throw error
            itineraryApiService.createItinerary(CreateItineraryRequestDto(groupName = groupName, tripId = tripId))
        }
    }

    private fun cacheItinerary(
        groupName: String,
        itinerary: ItineraryResponse,
        pendingProposal: ItineraryProposal? = workspaceState(groupName).value.pendingProposal
    ) {
        itineraryIdsByGroup[groupName] = itinerary.id
        workspaceState(groupName).value = itinerary.toWorkspace(pendingProposal)
    }

    private suspend fun itineraryId(groupName: String): Long {
        return itineraryIdsByGroup[groupName] ?: loadOrCreateItinerary(groupName).also {
            cacheItinerary(groupName, it)
        }.id
    }

    private fun findDay(groupName: String, dayId: Long?, dayIndex: Int): ItineraryDay {
        return workspaceState(groupName).value.days.firstOrNull { day ->
            if (dayId != null) day.dayId == dayId else day.dayIndex == dayIndex
        } ?: error("Day not found")
    }

    private fun ItineraryResponse.toWorkspace(pendingProposal: ItineraryProposal?): ItineraryWorkspace {
        return ItineraryWorkspace(
            groupName = groupName,
            version = version,
            role = ItineraryUserRole.LEADER,
            days = days.map { it.toDomain() },
            pendingProposal = pendingProposal
        )
    }

    private fun ItineraryDayResponse.toDomain(): ItineraryDay {
        return ItineraryDay(
            dayIndex = dayIndex,
            label = label,
            dateLabel = dateLabel,
            events = stops.sortedBy { it.sortOrder }.map { it.toDomain(dayId = id, dayIndex = dayIndex) },
            dayId = id
        )
    }

    private fun ItineraryStopResponse.toDomain(dayId: Long, dayIndex: Int): ItineraryEvent {
        return ItineraryEvent(
            eventId = id.toString(),
            dayIndex = dayIndex,
            startTime = startTime.orEmpty(),
            endTime = endTime.orEmpty(),
            title = title,
            placeName = placeName,
            note = note.orEmpty(),
            transportToNext = transportToNext.orEmpty(),
            estimatedCost = estimatedCost.orEmpty(),
            colorHex = colorHex ?: ItineraryEventColors.Default,
            iconName = iconName.orEmpty().ifBlank { "Place" },
            dayId = dayId,
            stopId = id
        )
    }

    private fun ItineraryAiProposalResponseDto.toDomain(): ItineraryProposal {
        return ItineraryProposal(
            proposalId = proposalId,
            baseVersion = baseVersion,
            summary = summary,
            changes = changes.mapNotNull { it.toDomain() }
        )
    }

    private fun ItineraryAiChangeResponseDto.toDomain(): ItineraryChange? {
        return when (type) {
            "ADD_DAY" -> dayAfter?.let {
                AddDayChange(changeId, reason, insertAt ?: it.dayIndex - 1, it.toDomain())
            }
            "UPDATE_DAY" -> if (dayBefore != null && dayAfter != null) {
                UpdateDayChange(changeId, reason, dayBefore.toDomain(), dayAfter.toDomain())
            } else {
                null
            }
            "DELETE_DAY" -> dayBefore?.let { DeleteDayChange(changeId, reason, it.toDomain()) }
            "ADD_EVENT" -> stopAfter?.let {
                AddEventChange(
                    changeId = changeId,
                    reason = reason,
                    insertAt = insertAt ?: ((it.sortOrder ?: 1) - 1).coerceAtLeast(0),
                    eventAfter = it.toEvent()
                )
            }
            "UPDATE_EVENT" -> if (stopBefore != null && stopAfter != null) {
                val before = stopBefore.toEvent()
                val after = stopAfter.toEvent()
                UpdateEventChange(
                    changeId = changeId,
                    reason = reason,
                    targetEventId = (targetStopId ?: stopBefore.id).toString(),
                    fieldDiffs = buildFieldDiffs(before, after),
                    eventBefore = before,
                    eventAfter = after
                )
            } else {
                null
            }
            "DELETE_EVENT" -> stopBefore?.let {
                DeleteEventChange(
                    changeId = changeId,
                    reason = reason,
                    targetEventId = (targetStopId ?: it.id).toString(),
                    eventBefore = it.toEvent()
                )
            }
            "MOVE_EVENT" -> stopBefore?.let {
                MoveEventChange(
                    changeId = changeId,
                    reason = reason,
                    targetEventId = (targetStopId ?: it.id).toString(),
                    fromDayIndex = fromDayIndex ?: it.dayIndex ?: 1,
                    fromIndex = fromIndex ?: 0,
                    toDayIndex = toDayIndex ?: it.dayIndex ?: 1,
                    toIndex = toIndex ?: 0,
                    eventSnapshot = it.toEvent()
                )
            }
            else -> null
        }
    }

    private fun ItineraryAiDayDraftDto.toDomain(): ItineraryDay {
        return ItineraryDay(
            dayIndex = dayIndex,
            label = label,
            dateLabel = dateLabel,
            events = stops.orEmpty().map { it.toEvent(defaultDayIndex = dayIndex, defaultDayId = id) },
            dayId = id
        )
    }

    private fun ItineraryAiStopDraftDto.toEvent(
        defaultDayIndex: Int? = null,
        defaultDayId: Long? = null
    ): ItineraryEvent {
        val resolvedDayIndex = dayIndex ?: defaultDayIndex ?: 1
        val resolvedId = id?.toString() ?: "draft-$resolvedDayIndex-${sortOrder ?: 0}-${title.hashCode()}"
        return ItineraryEvent(
            eventId = resolvedId,
            dayIndex = resolvedDayIndex,
            startTime = startTime.orEmpty(),
            endTime = endTime.orEmpty(),
            title = title,
            placeName = placeName,
            note = note.orEmpty(),
            transportToNext = transportToNext.orEmpty(),
            estimatedCost = estimatedCost.orEmpty(),
            colorHex = colorHex ?: ItineraryEventColors.Default,
            iconName = iconName.orEmpty().ifBlank { "Place" },
            dayId = dayId ?: defaultDayId,
            stopId = id
        )
    }

    private fun ItineraryEvent.toUpsertRequest(dayId: Long, sortOrder: Int? = null): UpsertItineraryStopRequestDto {
        return UpsertItineraryStopRequestDto(
            dayId = dayId,
            sortOrder = sortOrder,
            startTime = startTime,
            endTime = endTime,
            title = title,
            placeName = placeName,
            note = note,
            transportToNext = transportToNext,
            estimatedCost = estimatedCost,
            colorHex = colorHex,
            iconName = iconName
        )
    }

    private fun buildFieldDiffs(before: ItineraryEvent, after: ItineraryEvent): List<FieldDiff> {
        val diffs = mutableListOf<FieldDiff>()
        if (before.dayIndex != after.dayIndex) diffs += FieldDiff(ItineraryField.DAY, before.dayIndex.toString(), after.dayIndex.toString())
        if (before.startTime != after.startTime) diffs += FieldDiff(ItineraryField.START_TIME, before.startTime, after.startTime)
        if (before.endTime != after.endTime) diffs += FieldDiff(ItineraryField.END_TIME, before.endTime, after.endTime)
        if (before.title != after.title) diffs += FieldDiff(ItineraryField.TITLE, before.title, after.title)
        if (before.placeName != after.placeName) diffs += FieldDiff(ItineraryField.PLACE_NAME, before.placeName, after.placeName)
        if (before.note != after.note) diffs += FieldDiff(ItineraryField.NOTE, before.note, after.note)
        if (before.transportToNext != after.transportToNext) diffs += FieldDiff(ItineraryField.TRANSPORT, before.transportToNext, after.transportToNext)
        if (before.estimatedCost != after.estimatedCost) diffs += FieldDiff(ItineraryField.ESTIMATED_COST, before.estimatedCost, after.estimatedCost)
        if (before.colorHex != after.colorHex) diffs += FieldDiff(ItineraryField.COLOR, before.colorHex.toString(), after.colorHex.toString())
        if (before.iconName != after.iconName) diffs += FieldDiff(ItineraryField.ICON, before.iconName, after.iconName)
        return diffs
    }

    private fun nextDateLabel(previous: String?, dayIndex: Int): String {
        if (previous.isNullOrBlank()) return "Day $dayIndex"
        val parts = previous.split(" ")
        if (parts.size < 2) return "Day $dayIndex"
        val dayNumber = parts[0].toIntOrNull() ?: return "Day $dayIndex"
        val month = parts.drop(1).joinToString(" ")
        return "${dayNumber + 1} $month"
    }
}
