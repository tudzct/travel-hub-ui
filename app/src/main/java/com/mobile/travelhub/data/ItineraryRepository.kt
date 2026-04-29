package com.mobile.travelhub.data

import com.mobile.travelhub.data.model.AddEventChange
import com.mobile.travelhub.data.model.DeleteEventChange
import com.mobile.travelhub.data.model.FieldDiff
import com.mobile.travelhub.data.model.ItineraryAssistantEvent
import com.mobile.travelhub.data.model.ItineraryChange
import com.mobile.travelhub.data.model.ItineraryDay
import com.mobile.travelhub.data.model.ItineraryEvent
import com.mobile.travelhub.data.model.ItineraryEventColors
import com.mobile.travelhub.data.model.ItineraryField
import com.mobile.travelhub.data.model.ItineraryProposal
import com.mobile.travelhub.data.model.ItineraryUserRole
import com.mobile.travelhub.data.model.ItineraryWorkspace
import com.mobile.travelhub.data.model.MoveEventChange
import com.mobile.travelhub.data.model.UpdateEventChange
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.update
import kotlin.math.max
import kotlin.math.min

@Singleton
class ItineraryRepository @Inject constructor() {

    private val workspaces = linkedMapOf<String, MutableStateFlow<ItineraryWorkspace>>()

    fun observeWorkspace(groupName: String): StateFlow<ItineraryWorkspace> {
        return workspaceState(groupName)
    }

    private fun workspaceState(groupName: String): MutableStateFlow<ItineraryWorkspace> {
        return workspaces.getOrPut(groupName) {
            MutableStateFlow(seedWorkspace(groupName))
        }
    }

    fun streamProposal(
        groupName: String,
        prompt: String,
        selectedDayIndex: Int
    ): Flow<ItineraryAssistantEvent> = flow {
        val workspace = workspaceState(groupName).value
        emit(ItineraryAssistantEvent.Thinking("Reviewing pacing, transport gaps, and timing tradeoffs..."))
        delay(350)

        val proposal = buildProposal(
            workspace = workspace,
            prompt = prompt,
            selectedDayIndex = selectedDayIndex
        )

        val response = buildAssistantResponse(prompt = prompt, proposal = proposal)
        response.chunked(72).forEach { chunk ->
            delay(90)
            emit(ItineraryAssistantEvent.MessageChunk(chunk))
        }

        workspaceState(groupName).update {
            it.copy(pendingProposal = proposal)
        }
        emit(ItineraryAssistantEvent.ProposalReady(proposal))
        emit(ItineraryAssistantEvent.Done)
    }

    fun discardPendingProposal(groupName: String) {
        workspaceState(groupName).update {
            it.copy(pendingProposal = null)
        }
    }

    fun applyProposalChanges(
        groupName: String,
        proposalId: String,
        selectedChangeIds: Set<String>,
        baseVersion: Int
    ): Result<Unit> {
        val workspaceFlow = workspaceState(groupName)
        val workspace = workspaceFlow.value
        val proposal = workspace.pendingProposal
            ?: return Result.failure(IllegalStateException("No pending proposal"))

        if (proposal.proposalId != proposalId) {
            return Result.failure(IllegalStateException("Proposal mismatch"))
        }
        if (workspace.version != baseVersion || proposal.baseVersion != baseVersion) {
            return Result.failure(IllegalStateException("Proposal is stale"))
        }

        val selectedChanges = proposal.changes.filter { it.changeId in selectedChangeIds }
        if (selectedChanges.isEmpty()) {
            return Result.failure(IllegalStateException("No changes selected"))
        }

        workspaceFlow.update { current ->
            var nextDays = current.days
            selectedChanges.forEach { change ->
                nextDays = applyChange(nextDays, change)
            }
            current.copy(
                days = nextDays,
                version = current.version + 1,
                pendingProposal = null
            )
        }
        return Result.success(Unit)
    }

    fun updateEvent(groupName: String, updatedEvent: ItineraryEvent) {
        workspaceState(groupName).update { workspace ->
            val existingDayIndex = workspace.days.firstOrNull { day ->
                day.events.any { it.eventId == updatedEvent.eventId }
            }?.dayIndex ?: updatedEvent.dayIndex

            val eventForTargetDay = updatedEvent.copy(dayIndex = updatedEvent.dayIndex)
            val originalIndex = workspace.days
                .firstOrNull { it.dayIndex == existingDayIndex }
                ?.events
                ?.indexOfFirst { it.eventId == updatedEvent.eventId }
                ?.takeIf { it >= 0 }
                ?: Int.MAX_VALUE
            val strippedDays = workspace.days.map { day ->
                day.copy(events = day.events.filterNot { it.eventId == updatedEvent.eventId })
            }
            val nextDays = strippedDays.map { day ->
                if (day.dayIndex != updatedEvent.dayIndex) {
                    day
                } else {
                    val insertIndex = if (existingDayIndex == updatedEvent.dayIndex) {
                        min(day.events.size, originalIndex)
                    } else {
                        day.events.size
                    }
                    val nextEvents = day.events.toMutableList().apply {
                        add(insertIndex, eventForTargetDay)
                    }
                    day.copy(events = nextEvents)
                }
            }
            workspace.copy(days = nextDays, version = workspace.version + 1)
        }
    }

    fun updateDay(groupName: String, updatedDay: ItineraryDay) {
        workspaceState(groupName).update { workspace ->
            val nextDays = workspace.days.map { day ->
                if (day.dayIndex == updatedDay.dayIndex) {
                    day.copy(
                        label = updatedDay.label,
                        dateLabel = updatedDay.dateLabel
                    )
                } else {
                    day
                }
            }
            workspace.copy(days = nextDays, version = workspace.version + 1)
        }
    }

    fun deleteEvent(groupName: String, eventId: String) {
        workspaceState(groupName).update { workspace ->
            val nextDays = workspace.days.map { day ->
                day.copy(events = day.events.filterNot { it.eventId == eventId })
            }
            workspace.copy(days = nextDays, version = workspace.version + 1)
        }
    }

    fun deleteDay(groupName: String, dayIndex: Int) {
        workspaceState(groupName).update { workspace ->
            val remainingDays = workspace.days
                .filterNot { it.dayIndex == dayIndex }
                .sortedBy { it.dayIndex }
                .mapIndexed { index, day ->
                    val newDayIndex = index + 1
                    day.copy(
                        dayIndex = newDayIndex,
                        events = day.events.map { event -> event.copy(dayIndex = newDayIndex) }
                    )
                }
            workspace.copy(days = remainingDays, version = workspace.version + 1)
        }
    }

    fun addDay(groupName: String): Int {
        val workspaceFlow = workspaceState(groupName)
        val newDayIndex = (workspaceFlow.value.days.maxOfOrNull { it.dayIndex } ?: 0) + 1
        workspaceFlow.update { workspace ->
            val lastDay = workspace.days.maxByOrNull { it.dayIndex }
            val nextDay = ItineraryDay(
                dayIndex = newDayIndex,
                label = "Day $newDayIndex",
                dateLabel = nextDateLabel(lastDay?.dateLabel),
                events = emptyList()
            )
            workspace.copy(
                days = workspace.days + nextDay,
                version = workspace.version + 1
            )
        }
        return newDayIndex
    }

    fun reorderEvent(groupName: String, dayIndex: Int, eventId: String, moveUp: Boolean) {
        workspaceState(groupName).update { workspace ->
            val nextDays = workspace.days.map { day ->
                if (day.dayIndex != dayIndex) {
                    day
                } else {
                    val currentIndex = day.events.indexOfFirst { it.eventId == eventId }
                    if (currentIndex == -1) {
                        day
                    } else {
                        val swapIndex = if (moveUp) currentIndex - 1 else currentIndex + 1
                        if (swapIndex !in day.events.indices) {
                            day
                        } else {
                            val originalEvents = day.events
                            val nextEvents = originalEvents.toMutableList()
                            val currentEvent = nextEvents[currentIndex]
                            nextEvents[currentIndex] = nextEvents[swapIndex]
                            nextEvents[swapIndex] = currentEvent
                            day.copy(events = nextEvents.withTimeSlotsFrom(originalEvents))
                        }
                    }
                }
            }
            workspace.copy(days = nextDays, version = workspace.version + 1)
        }
    }

    fun reorderEvents(groupName: String, dayIndex: Int, fromIndex: Int, toIndex: Int) {
        if (fromIndex == toIndex) return
        workspaceState(groupName).update { workspace ->
            val nextDays = workspace.days.map { day ->
                if (day.dayIndex != dayIndex) {
                    day
                } else {
                    if (fromIndex !in day.events.indices || toIndex !in day.events.indices) {
                        day
                    } else {
                        val originalEvents = day.events
                        val nextEvents = originalEvents.toMutableList().apply {
                            add(toIndex, removeAt(fromIndex))
                        }
                        day.copy(events = nextEvents.withTimeSlotsFrom(originalEvents))
                    }
                }
            }
            workspace.copy(days = nextDays, version = workspace.version + 1)
        }
    }

    private fun applyChange(days: List<ItineraryDay>, change: ItineraryChange): List<ItineraryDay> {
        return when (change) {
            is AddEventChange -> {
                days.map { day ->
                    if (day.dayIndex != change.eventAfter.dayIndex) {
                        day
                    } else {
                        val insertIndex = change.insertAt.coerceIn(0, day.events.size)
                        val nextEvents = day.events.toMutableList().apply {
                            add(insertIndex, change.eventAfter)
                        }
                        day.copy(events = nextEvents)
                    }
                }
            }

            is UpdateEventChange -> {
                days.map { day ->
                    day.copy(
                        events = day.events.map { event ->
                            if (event.eventId == change.targetEventId) change.eventAfter else event
                        }
                    )
                }
            }

            is DeleteEventChange -> {
                days.map { day ->
                    day.copy(events = day.events.filterNot { it.eventId == change.targetEventId })
                }
            }

            is MoveEventChange -> {
                val event = days.firstNotNullOfOrNull { day ->
                    day.events.firstOrNull { it.eventId == change.targetEventId }
                } ?: return days

                val removed = days.map { day ->
                    day.copy(events = day.events.filterNot { it.eventId == change.targetEventId })
                }

                removed.map { day ->
                    if (day.dayIndex != change.toDayIndex) {
                        day
                    } else {
                        val insertIndex = change.toIndex.coerceIn(0, day.events.size)
                        val movedEvent = event.copy(dayIndex = change.toDayIndex)
                        val nextEvents = day.events.toMutableList().apply {
                            add(insertIndex, movedEvent)
                        }
                        day.copy(events = nextEvents)
                    }
                }
            }
        }
    }

    private fun buildProposal(
        workspace: ItineraryWorkspace,
        prompt: String,
        selectedDayIndex: Int
    ): ItineraryProposal {
        val normalized = prompt.lowercase()
        val chosenDayIndex = selectedDayIndex.coerceIn(1, workspace.days.size)
        val day = workspace.days.firstOrNull { it.dayIndex == chosenDayIndex } ?: workspace.days.first()
        val changes = mutableListOf<ItineraryChange>()

        if (normalized.contains("them") || normalized.contains("thêm") || normalized.contains("add")) {
            changes += buildAddChange(day)
        }
        if (
            normalized.contains("sua") ||
            normalized.contains("sửa") ||
            normalized.contains("doi") ||
            normalized.contains("đổi") ||
            normalized.contains("edit")
        ) {
            changes += buildUpdateChange(day)
        }
        if (
            normalized.contains("xoa") ||
            normalized.contains("xóa") ||
            normalized.contains("remove") ||
            normalized.contains("delete")
        ) {
            day.events.lastOrNull()?.let { target ->
                changes += buildDeleteChange(target)
            }
        }
        if (
            normalized.contains("move") ||
            normalized.contains("chuyen") ||
            normalized.contains("chuyển") ||
            normalized.contains("dời")
        ) {
            buildMoveChange(workspace = workspace, currentDay = day)?.let(changes::add)
        }

        if (changes.isEmpty()) {
            changes += buildUpdateChange(day)
            changes += buildAddChange(day)
            buildMoveChange(workspace = workspace, currentDay = day)?.let(changes::add)
        }

        return ItineraryProposal(
            proposalId = "proposal-${System.currentTimeMillis()}",
            baseVersion = workspace.version,
            summary = "Generated ${changes.size} change${if (changes.size > 1) "s" else ""} for ${day.label.lowercase()} based on your latest instruction.",
            changes = changes
        )
    }

    private fun buildAddChange(day: ItineraryDay): AddEventChange {
        val insertAt = min(1, day.events.size)
        val previous = day.events.getOrNull(insertAt)
        val startTime = previous?.startTime ?: "11:15"
        val event = ItineraryEvent(
            eventId = "event-${System.currentTimeMillis()}-add",
            dayIndex = day.dayIndex,
            startTime = startTime,
            endTime = "12:00",
            title = "Coffee & buffer stop",
            placeName = "Blue Bottle Shibuya",
            note = "Short recharge stop to absorb delays before the next long visit.",
            transportToNext = "Walk 8 minutes to the next venue.",
            estimatedCost = "$12 / person",
            isHighlighted = false,
            colorHex = eventColorForIndex(insertAt)
        )
        return AddEventChange(
            changeId = "change-add-${day.dayIndex}-${System.nanoTime()}",
            reason = "Adds a flexible buffer so the day is less rushed around midday.",
            insertAt = insertAt,
            eventAfter = event
        )
    }

    private fun buildUpdateChange(day: ItineraryDay): UpdateEventChange {
        val target = day.events.firstOrNull { it.isHighlighted } ?: day.events.first()
        val updated = target.copy(
            startTime = shiftHour(target.startTime, 1),
            endTime = shiftHour(target.endTime, 1),
            note = "${target.note} Updated to reduce commute pressure and leave more check-in margin.",
            estimatedCost = if (target.estimatedCost.isBlank()) "$18 / person" else target.estimatedCost
        )

        return UpdateEventChange(
            changeId = "change-update-${target.eventId}-${System.nanoTime()}",
            reason = "Moves the highlight slightly later and adds breathing room before the following leg.",
            targetEventId = target.eventId,
            fieldDiffs = buildFieldDiffs(target, updated),
            eventBefore = target,
            eventAfter = updated
        )
    }

    private fun buildDeleteChange(target: ItineraryEvent): DeleteEventChange {
        return DeleteEventChange(
            changeId = "change-delete-${target.eventId}-${System.nanoTime()}",
            reason = "Removes the lowest-priority stop to free up time for recovery or transit delays.",
            targetEventId = target.eventId,
            eventBefore = target
        )
    }

    private fun buildMoveChange(
        workspace: ItineraryWorkspace,
        currentDay: ItineraryDay
    ): MoveEventChange? {
        if (workspace.days.size < 2) return null
        val target = currentDay.events.lastOrNull() ?: return null
        val toDayIndex = if (currentDay.dayIndex == workspace.days.last().dayIndex) {
            currentDay.dayIndex - 1
        } else {
            currentDay.dayIndex + 1
        }
        val destinationDay = workspace.days.firstOrNull { it.dayIndex == toDayIndex } ?: return null
        return MoveEventChange(
            changeId = "change-move-${target.eventId}-${System.nanoTime()}",
            reason = "Shifts a later activity to balance energy and avoid stacking too many evening commitments.",
            targetEventId = target.eventId,
            fromDayIndex = currentDay.dayIndex,
            fromIndex = currentDay.events.lastIndex,
            toDayIndex = toDayIndex,
            toIndex = min(1, destinationDay.events.size),
            eventSnapshot = target
        )
    }

    private fun buildFieldDiffs(
        before: ItineraryEvent,
        after: ItineraryEvent
    ): List<FieldDiff> {
        val diffs = mutableListOf<FieldDiff>()
        if (before.dayIndex != after.dayIndex) {
            diffs += FieldDiff(ItineraryField.DAY, before.dayIndex.toString(), after.dayIndex.toString())
        }
        if (before.startTime != after.startTime) {
            diffs += FieldDiff(ItineraryField.START_TIME, before.startTime, after.startTime)
        }
        if (before.endTime != after.endTime) {
            diffs += FieldDiff(ItineraryField.END_TIME, before.endTime, after.endTime)
        }
        if (before.title != after.title) {
            diffs += FieldDiff(ItineraryField.TITLE, before.title, after.title)
        }
        if (before.placeName != after.placeName) {
            diffs += FieldDiff(ItineraryField.PLACE_NAME, before.placeName, after.placeName)
        }
        if (before.note != after.note) {
            diffs += FieldDiff(ItineraryField.NOTE, before.note, after.note)
        }
        if (before.transportToNext != after.transportToNext) {
            diffs += FieldDiff(ItineraryField.TRANSPORT, before.transportToNext, after.transportToNext)
        }
        if (before.estimatedCost != after.estimatedCost) {
            diffs += FieldDiff(ItineraryField.ESTIMATED_COST, before.estimatedCost, after.estimatedCost)
        }
        if (before.isHighlighted != after.isHighlighted) {
            diffs += FieldDiff(
                ItineraryField.HIGHLIGHT,
                before.isHighlighted.toString(),
                after.isHighlighted.toString()
            )
        }
        if (before.colorHex != after.colorHex) {
            diffs += FieldDiff(
                ItineraryField.COLOR,
                before.colorHex.toString(),
                after.colorHex.toString()
            )
        }
        return diffs
    }

    private fun buildAssistantResponse(prompt: String, proposal: ItineraryProposal): String {
        return buildString {
            append("I reviewed \"$prompt\" and prepared a proposal against the current itinerary version. ")
            append(proposal.summary)
            append(" Review the diff cards below and apply only the changes you want to keep.")
        }
    }

    private fun seedWorkspace(groupName: String): ItineraryWorkspace {
        return ItineraryWorkspace(
            groupName = groupName,
            version = 7,
            role = ItineraryUserRole.LEADER,
            days = listOf(
                ItineraryDay(
                    dayIndex = 1,
                    label = "Day 1",
                    dateLabel = "12 Oct",
                    events = listOf(
                        ItineraryEvent(
                            eventId = "d1-e1",
                            dayIndex = 1,
                            startTime = "09:00",
                            endTime = "11:00",
                            title = "Senso-ji Temple",
                            placeName = "Asakusa",
                            note = "Start early to avoid the densest crowds at Nakamise.",
                            transportToNext = "Ginza Line, 15 minutes",
                            estimatedCost = "Free",
                            isHighlighted = true,
                            colorHex = ItineraryEventColors.Palette[0]
                        ),
                        ItineraryEvent(
                            eventId = "d1-e2",
                            dayIndex = 1,
                            startTime = "11:45",
                            endTime = "13:15",
                            title = "Sushi Dai lunch",
                            placeName = "Tsukiji Outer Market",
                            note = "Queue can spike quickly; keep a backup lunch option nearby.",
                            transportToNext = "Walk 10 minutes",
                            estimatedCost = "$45 / person",
                            colorHex = ItineraryEventColors.Palette[2]
                        ),
                        ItineraryEvent(
                            eventId = "d1-e3",
                            dayIndex = 1,
                            startTime = "14:00",
                            endTime = "17:00",
                            title = "Akihabara wander",
                            placeName = "Akihabara",
                            note = "Light shopping and arcade stop before sunset.",
                            transportToNext = "JR Yamanote, 22 minutes",
                            estimatedCost = "Variable",
                            colorHex = ItineraryEventColors.Palette[4]
                        )
                    )
                ),
                ItineraryDay(
                    dayIndex = 2,
                    label = "Day 2",
                    dateLabel = "13 Oct",
                    events = listOf(
                        ItineraryEvent(
                            eventId = "d2-e1",
                            dayIndex = 2,
                            startTime = "08:30",
                            endTime = "10:30",
                            title = "Meiji Shrine",
                            placeName = "Shibuya",
                            note = "Arrive before tour buses and leave room for the forest walk.",
                            transportToNext = "Taxi 12 minutes",
                            estimatedCost = "Free",
                            colorHex = ItineraryEventColors.Palette[1]
                        ),
                        ItineraryEvent(
                            eventId = "d2-e2",
                            dayIndex = 2,
                            startTime = "11:00",
                            endTime = "13:00",
                            title = "Omotesando brunch",
                            placeName = "Aoyama Flower Market Tea House",
                            note = "Good indoor pause if weather turns.",
                            transportToNext = "Walk 14 minutes",
                            estimatedCost = "$28 / person",
                            colorHex = ItineraryEventColors.Palette[5]
                        ),
                        ItineraryEvent(
                            eventId = "d2-e3",
                            dayIndex = 2,
                            startTime = "17:00",
                            endTime = "19:00",
                            title = "Shibuya Sky",
                            placeName = "Shibuya Scramble Square",
                            note = "Sunset slot already reserved.",
                            transportToNext = "Dinner nearby",
                            estimatedCost = "$15 prepaid",
                            isHighlighted = true,
                            colorHex = ItineraryEventColors.Palette[3]
                        )
                    )
                ),
                ItineraryDay(
                    dayIndex = 3,
                    label = "Day 3",
                    dateLabel = "14 Oct",
                    events = listOf(
                        ItineraryEvent(
                            eventId = "d3-e1",
                            dayIndex = 3,
                            startTime = "09:30",
                            endTime = "12:00",
                            title = "Ueno Museum block",
                            placeName = "Ueno Park",
                            note = "Pick 1 museum instead of trying to do all three.",
                            transportToNext = "Walk 6 minutes",
                            estimatedCost = "$20 / person",
                            colorHex = ItineraryEventColors.Palette[6]
                        ),
                        ItineraryEvent(
                            eventId = "d3-e2",
                            dayIndex = 3,
                            startTime = "13:00",
                            endTime = "16:00",
                            title = "Yanaka slow stroll",
                            placeName = "Yanaka Ginza",
                            note = "Low-intensity afternoon for recovery after two dense days.",
                            transportToNext = "Train 18 minutes",
                            estimatedCost = "Snacks only",
                            colorHex = ItineraryEventColors.Palette[7]
                        )
                    )
                )
            )
        )
    }

    private fun shiftHour(time: String, deltaHours: Int): String {
        val parts = time.split(":")
        if (parts.size != 2) return time
        val hour = parts[0].toIntOrNull() ?: return time
        val minute = parts[1].toIntOrNull() ?: return time
        val nextHour = max(0, min(23, hour + deltaHours))
        return "%02d:%02d".format(nextHour, minute)
    }

    private fun nextDateLabel(previous: String?): String {
        if (previous.isNullOrBlank()) return "Day"
        val parts = previous.split(" ")
        if (parts.size < 2) return previous
        val dayNumber = parts[0].toIntOrNull() ?: return previous
        val month = parts.drop(1).joinToString(" ")
        return "${dayNumber + 1} $month"
    }

    private fun eventColorForIndex(index: Int): Long {
        return ItineraryEventColors.Palette[index % ItineraryEventColors.Palette.size]
    }

    private fun List<ItineraryEvent>.withTimeSlotsFrom(sourceEvents: List<ItineraryEvent>): List<ItineraryEvent> {
        return mapIndexed { index, event ->
            val source = sourceEvents.getOrNull(index) ?: return@mapIndexed event
            event.copy(
                startTime = source.startTime,
                endTime = source.endTime
            )
        }
    }
}
