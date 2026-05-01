package com.mobile.travelhub.data.model

enum class ItineraryUserRole {
    LEADER,
    MEMBER
}

data class ItineraryWorkspace(
    val groupName: String,
    val version: Int,
    val role: ItineraryUserRole,
    val days: List<ItineraryDay>,
    val pendingProposal: ItineraryProposal? = null
)

data class ItineraryDay(
    val dayIndex: Int,
    val label: String,
    val dateLabel: String,
    val events: List<ItineraryEvent>
)

data class ItineraryEvent(
    val eventId: String,
    val dayIndex: Int,
    val startTime: String,
    val endTime: String,
    val title: String,
    val placeName: String,
    val note: String,
    val transportToNext: String,
    val estimatedCost: String,
    val isHighlighted: Boolean = false,
    val colorHex: Long = ItineraryEventColors.Default
)

data class ItineraryProposal(
    val proposalId: String,
    val baseVersion: Int,
    val summary: String,
    val changes: List<ItineraryChange>
)

enum class ItineraryChangeType {
    ADD_EVENT,
    UPDATE_EVENT,
    DELETE_EVENT,
    MOVE_EVENT
}

enum class ItineraryField(val label: String) {
    DAY("Day"),
    START_TIME("Start time"),
    END_TIME("End time"),
    TITLE("Title"),
    PLACE_NAME("Place"),
    NOTE("Note"),
    TRANSPORT("Transport"),
    ESTIMATED_COST("Cost"),
    HIGHLIGHT("Highlight"),
    COLOR("Color")
}

object ItineraryEventColors {
    const val Default: Long = 0xFF3E6AE1
    val Palette: List<Long> = listOf(
        Default,
        0xFF0D8A4B,
        0xFFCC5F00,
        0xFFB3261E,
        0xFF7A4DFF,
        0xFF00838F,
        0xFFAF3E6A,
        0xFF6B7280
    )
}

data class FieldDiff(
    val field: ItineraryField,
    val before: String?,
    val after: String?
) {
    val label: String = field.label
}

sealed interface ItineraryChange {
    val changeId: String
    val type: ItineraryChangeType
    val reason: String
}

data class AddEventChange(
    override val changeId: String,
    override val reason: String,
    val insertAt: Int,
    val eventAfter: ItineraryEvent
) : ItineraryChange {
    override val type: ItineraryChangeType = ItineraryChangeType.ADD_EVENT
}

data class UpdateEventChange(
    override val changeId: String,
    override val reason: String,
    val targetEventId: String,
    val fieldDiffs: List<FieldDiff>,
    val eventBefore: ItineraryEvent,
    val eventAfter: ItineraryEvent
) : ItineraryChange {
    override val type: ItineraryChangeType = ItineraryChangeType.UPDATE_EVENT
}

data class DeleteEventChange(
    override val changeId: String,
    override val reason: String,
    val targetEventId: String,
    val eventBefore: ItineraryEvent
) : ItineraryChange {
    override val type: ItineraryChangeType = ItineraryChangeType.DELETE_EVENT
}

data class MoveEventChange(
    override val changeId: String,
    override val reason: String,
    val targetEventId: String,
    val fromDayIndex: Int,
    val fromIndex: Int,
    val toDayIndex: Int,
    val toIndex: Int,
    val eventSnapshot: ItineraryEvent
) : ItineraryChange {
    override val type: ItineraryChangeType = ItineraryChangeType.MOVE_EVENT
}

enum class ItineraryChatRole {
    USER,
    ASSISTANT
}

data class ItineraryChatMessage(
    val id: String,
    val role: ItineraryChatRole,
    val text: String
)

sealed interface ItineraryAssistantEvent {
    data class Thinking(val text: String) : ItineraryAssistantEvent
    data class MessageChunk(val text: String) : ItineraryAssistantEvent
    data class ProposalReady(val proposal: ItineraryProposal) : ItineraryAssistantEvent
    data class Error(val message: String) : ItineraryAssistantEvent
    data object Done : ItineraryAssistantEvent
}
