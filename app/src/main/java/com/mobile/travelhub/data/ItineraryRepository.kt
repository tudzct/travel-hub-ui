package com.mobile.travelhub.data

import com.mobile.travelhub.data.api.CreateTripActivityRequestDto
import com.mobile.travelhub.data.api.ItineraryApiService
import com.mobile.travelhub.data.model.ItineraryAssistantEvent
import com.mobile.travelhub.data.model.ItineraryDay
import com.mobile.travelhub.data.model.ItineraryEvent
import com.mobile.travelhub.data.model.ItineraryEventColors
import com.mobile.travelhub.data.model.ItineraryProposal
import com.mobile.travelhub.data.model.ItineraryUserRole
import com.mobile.travelhub.data.model.ItineraryWorkspace
import com.mobile.travelhub.data.model.TripActivityResponse
import com.mobile.travelhub.data.model.TripDayResponse
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.update

@Singleton
class ItineraryRepository @Inject constructor(
    private val itineraryApiService: ItineraryApiService
) {
    private val workspaces = linkedMapOf<String, MutableStateFlow<ItineraryWorkspace>>()
    private val tripIdsByGroup = mutableMapOf<String, Long>()

    fun observeWorkspace(groupName: String): StateFlow<ItineraryWorkspace> {
        return workspaceState(groupName)
    }

    suspend fun refreshWorkspace(groupName: String, tripId: Long? = null) {
        val resolvedTripId = resolveTripId(groupName, tripId)
        val tripDays = itineraryApiService.listTripDays(resolvedTripId)
        cacheTripDays(groupName, tripDays, pendingProposal = workspaceState(groupName).value.pendingProposal)
    }

    fun streamProposal(
        groupName: String,
        tripId: Long? = null,
        prompt: String,
        selectedDayIndex: Int,
        inputType: String = "TEXT"
    ): Flow<ItineraryAssistantEvent> = flow {
        emit(ItineraryAssistantEvent.Error("Backend hiện chưa cung cấp API AI proposal cho TripDay/TripActivity."))
        emit(ItineraryAssistantEvent.Done)
    }

    suspend fun applyProposalChanges(
        groupName: String,
        proposalId: String,
        selectedChangeIds: Set<String>,
        baseVersion: Int
    ): Result<Unit> = runCatching {
        error("Backend hiện chưa cung cấp API apply proposal cho TripDay/TripActivity.")
    }

    fun discardPendingProposal(groupName: String) {
        workspaceState(groupName).update { it.copy(pendingProposal = null) }
    }

    suspend fun updateEvent(groupName: String, updatedEvent: ItineraryEvent) {
        val targetDay = findDay(groupName, dayId = null, dayIndex = updatedEvent.dayIndex)
        val tripId = tripId(groupName)
        val request = updatedEvent.toActivityRequest(
            date = targetDay.dateLabel.toApiDate(),
            orderIndex = updatedEvent.orderIndexIn(targetDay)
        )
        updatedEvent.stopId?.let { activityId ->
            itineraryApiService.updateTripActivity(tripId, activityId, request)
        } ?: itineraryApiService.createTripActivity(tripId, request)
        cacheTripDays(groupName, itineraryApiService.listTripDays(tripId), pendingProposal = null)
    }

    suspend fun updateDay(groupName: String, updatedDay: ItineraryDay) {
        workspaceState(groupName).update { workspace ->
            workspace.copy(
                days = workspace.days.map { day ->
                    if (day.dayIndex == updatedDay.dayIndex) {
                        day.copy(label = updatedDay.label, dateLabel = updatedDay.dateLabel)
                    } else {
                        day
                    }
                },
                pendingProposal = null
            )
        }
    }

    suspend fun deleteEvent(groupName: String, eventId: String) {
        val stopId = eventId.toLongOrNull() ?: return
        val tripId = tripId(groupName)
        itineraryApiService.deleteTripActivity(tripId, stopId)
        cacheTripDays(groupName, itineraryApiService.listTripDays(tripId), pendingProposal = null)
    }

    suspend fun deleteDay(groupName: String, dayIndex: Int) {
        val day = findDay(groupName, null, dayIndex)
        val tripId = tripId(groupName)
        day.events.mapNotNull { it.stopId }.forEach { activityId ->
            itineraryApiService.deleteTripActivity(tripId, activityId)
        }
        cacheTripDays(groupName, itineraryApiService.listTripDays(tripId), pendingProposal = null)
    }

    suspend fun addDay(groupName: String): Int {
        val workspace = workspaceState(groupName).value
        val nextIndex = (workspace.days.maxOfOrNull { it.dayIndex } ?: 0) + 1
        workspaceState(groupName).update {
            it.copy(
                days = (it.days + ItineraryDay(
                    dayIndex = nextIndex,
                    label = "Day $nextIndex",
                    dateLabel = nextDateLabel(workspace.days.maxByOrNull { day -> day.dayIndex }?.dateLabel, nextIndex),
                    events = emptyList()
                )).sortedBy { day -> day.dayIndex },
                pendingProposal = null
            )
        }
        return nextIndex
    }

    suspend fun ensureDay(
        groupName: String,
        dayIndex: Int,
        label: String,
        dateLabel: String
    ): ItineraryDay {
        workspaceState(groupName).value.days.firstOrNull { it.dayIndex == dayIndex }?.let { return it }
        workspaceState(groupName).update { workspace ->
            val existingDays = workspace.days.associateBy { it.dayIndex }
            val maxIndex = maxOf(dayIndex, workspace.days.maxOfOrNull { it.dayIndex } ?: 0)
            val mergedDays = (1..maxIndex).map { index ->
                existingDays[index] ?: ItineraryDay(
                    dayIndex = index,
                    label = if (index == dayIndex) label else "Day $index",
                    dateLabel = if (index == dayIndex) {
                        dateLabel
                    } else {
                        nextDateLabel(workspace.days.maxByOrNull { it.dayIndex }?.dateLabel, index)
                    },
                    events = emptyList()
                )
            }
            workspace.copy(days = mergedDays, pendingProposal = null)
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
        val request = moved.toActivityRequest(date = day.dateLabel.toApiDate(), orderIndex = toIndex + 1)
        val stopId = moved.stopId ?: return
        val tripId = tripId(groupName)
        itineraryApiService.updateTripActivity(tripId, stopId, request)
        cacheTripDays(groupName, itineraryApiService.listTripDays(tripId), pendingProposal = null)
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

    private fun cacheTripDays(
        groupName: String,
        tripDays: List<TripDayResponse>,
        pendingProposal: ItineraryProposal? = workspaceState(groupName).value.pendingProposal
    ) {
        val previous = workspaceState(groupName).value
        workspaceState(groupName).value = ItineraryWorkspace(
            groupName = groupName,
            version = previous.version + 1,
            role = ItineraryUserRole.LEADER,
            days = tripDays.sortedWith(compareBy<TripDayResponse> { it.dayNumber }.thenBy { it.date }).map { it.toDomain() },
            pendingProposal = pendingProposal
        )
    }

    private fun resolveTripId(groupName: String, tripId: Long?): Long {
        if (tripId != null && tripId > 0) {
            tripIdsByGroup[groupName] = tripId
            return tripId
        }
        return tripIdsByGroup[groupName] ?: error("Trip id is required to load itinerary")
    }

    private fun tripId(groupName: String): Long {
        return tripIdsByGroup[groupName] ?: error("Trip id is required to update itinerary")
    }

    private fun findDay(groupName: String, dayId: Long?, dayIndex: Int): ItineraryDay {
        return workspaceState(groupName).value.days.firstOrNull { day ->
            if (dayId != null) day.dayId == dayId else day.dayIndex == dayIndex
        } ?: error("Day not found")
    }

    private fun TripDayResponse.toDomain(): ItineraryDay {
        return ItineraryDay(
            dayIndex = dayNumber,
            label = "Day $dayNumber",
            dateLabel = date.toDisplayDate(),
            events = activities.sortedBy { it.orderIndex ?: Int.MAX_VALUE }.map {
                it.toDomain(dayId = id, dayIndex = dayNumber)
            },
            dayId = id
        )
    }

    private fun TripActivityResponse.toDomain(dayId: Long, dayIndex: Int): ItineraryEvent {
        return ItineraryEvent(
            eventId = id.toString(),
            dayIndex = dayIndex,
            startTime = startTime.toUiTime(),
            endTime = endTime.toUiTime(),
            title = title,
            placeName = locationName.orEmpty(),
            note = description.orEmpty(),
            transportToNext = address.orEmpty(),
            estimatedCost = estimatedCost?.toCostText().orEmpty(),
            colorHex = colorForType(type),
            iconName = iconForType(type),
            dayId = dayId,
            stopId = id
        )
    }

    private fun ItineraryEvent.toActivityRequest(date: String, orderIndex: Int): CreateTripActivityRequestDto {
        return CreateTripActivityRequestDto(
            date = date,
            title = title,
            description = note,
            startTime = startTime.toApiTime(),
            endTime = endTime.toApiTime(),
            locationName = placeName,
            address = transportToNext,
            type = iconName.toActivityType(),
            orderIndex = orderIndex,
            estimatedCost = estimatedCost.toDoubleOrNull() ?: 0.0
        )
    }

    private fun ItineraryEvent.orderIndexIn(day: ItineraryDay): Int {
        return day.events.indexOfFirst { it.eventId == eventId }
            .takeIf { it >= 0 }
            ?.plus(1)
            ?: (day.events.size + 1)
    }

    private fun nextDateLabel(previous: String?, dayIndex: Int): String {
        if (previous.isNullOrBlank()) return "Day $dayIndex"
        parseDisplayDate(previous)?.let { return it.plusDays(1).format(displayDateFormatter) }
        val parts = previous.split(" ")
        if (parts.size < 2) return "Day $dayIndex"
        val dayNumber = parts[0].toIntOrNull() ?: return "Day $dayIndex"
        val month = parts.drop(1).joinToString(" ")
        return "${dayNumber + 1} $month"
    }

    private fun String.toApiDate(): String {
        return parseDisplayDate(this)?.format(apiDateFormatter)
            ?: runCatching { LocalDate.parse(substringBefore("T")) }.getOrNull()?.format(apiDateFormatter)
            ?: error("Invalid trip day date: $this")
    }

    private fun String.toDisplayDate(): String {
        return runCatching { LocalDate.parse(substringBefore("T")).format(displayDateFormatter) }
            .getOrDefault(this)
    }

    private fun String?.toUiTime(): String {
        return this?.substringBeforeLast(":").orEmpty()
    }

    private fun String.toApiTime(): String {
        return when (count { it == ':' }) {
            0 -> "$this:00:00"
            1 -> "$this:00"
            else -> this
        }
    }

    private fun String.toActivityType(): String {
        return when (this) {
            "Restaurant", "LocalDrink" -> "FOOD"
            "Flight", "DirectionsBus", "DirectionsCar", "DirectionsWalk", "Train" -> "TRANSPORT"
            "Hotel" -> "HOTEL"
            "ShoppingBag" -> "SHOPPING"
            else -> "PLACE"
        }
    }

    private fun Double.toCostText(): String {
        return if (rem(1.0) == 0.0) toLong().toString() else toString()
    }

    private fun iconForType(type: String?): String {
        return when (type) {
            "FOOD" -> "Restaurant"
            "TRANSPORT" -> "DirectionsCar"
            "HOTEL" -> "Hotel"
            "SHOPPING" -> "ShoppingBag"
            else -> "Place"
        }
    }

    private fun colorForType(type: String?): Long {
        return when (type) {
            "FOOD" -> ItineraryEventColors.Palette.getOrElse(1) { ItineraryEventColors.Default }
            "TRANSPORT" -> ItineraryEventColors.Palette.getOrElse(5) { ItineraryEventColors.Default }
            "HOTEL" -> ItineraryEventColors.Palette.getOrElse(4) { ItineraryEventColors.Default }
            "SHOPPING" -> ItineraryEventColors.Palette.getOrElse(2) { ItineraryEventColors.Default }
            else -> ItineraryEventColors.Default
        }
    }

    private fun parseDisplayDate(value: String): LocalDate? {
        return runCatching { LocalDate.parse(value, displayDateFormatter) }.getOrNull()
    }

    private companion object {
        val apiDateFormatter: DateTimeFormatter = DateTimeFormatter.ISO_LOCAL_DATE
        val displayDateFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy", Locale.getDefault())
    }
}
