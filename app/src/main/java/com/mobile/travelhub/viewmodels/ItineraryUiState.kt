package com.mobile.travelhub.viewmodels

import com.mobile.travelhub.data.model.ItineraryChatMessage
import com.mobile.travelhub.data.model.ItineraryDay
import com.mobile.travelhub.data.model.ItineraryEvent
import com.mobile.travelhub.data.model.ItineraryProposal
import com.mobile.travelhub.data.model.ItineraryUserRole

data class ItineraryDayOption(
    val dayIndex: Int,
    val label: String,
    val dateLabel: String,
    val epochDay: Long? = null
)

data class ItineraryUiState(
    val groupName: String = "",
    val version: Int = 0,
    val role: ItineraryUserRole = ItineraryUserRole.MEMBER,
    val days: List<ItineraryDay> = emptyList(),
    val dayOptions: List<ItineraryDayOption> = emptyList(),
    val selectedDayIndex: Int = 1,
    val pendingProposal: ItineraryProposal? = null,
    val selectedChangeIds: Set<String> = emptySet(),
    val chatMessages: List<ItineraryChatMessage> = emptyList(),
    val chatInput: String = "",
    val chatInputType: String = "TEXT",
    val thinking: String = "",
    val isStreaming: Boolean = false,
    val isLoadingActivities: Boolean = false,
    val isChatSheetOpen: Boolean = false,
    val editingDay: ItineraryDay? = null,
    val editingEvent: ItineraryEvent? = null,
    val isCreatingEvent: Boolean = false,
    val isEditMode: Boolean = false,
    val errorMessage: String? = null
) {
    val isLeader: Boolean
        get() = role == ItineraryUserRole.LEADER

    val selectedDay: ItineraryDay?
        get() = days.firstOrNull { it.dayIndex == selectedDayIndex }

    val isProposalStale: Boolean
        get() = pendingProposal?.baseVersion?.let { it != version } == true
}
