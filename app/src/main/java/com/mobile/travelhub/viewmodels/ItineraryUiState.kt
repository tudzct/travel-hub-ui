package com.mobile.travelhub.viewmodels

import com.mobile.travelhub.data.model.ItineraryDay
import com.mobile.travelhub.data.model.ItineraryEvent
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
    val isLoadingActivities: Boolean = false,
    val editingDay: ItineraryDay? = null,
    val editingEvent: ItineraryEvent? = null,
    val isCreatingEvent: Boolean = false,
    val isEditMode: Boolean = false,
    val errorMessage: String? = null,
    val isCompleted: Boolean = false
) {
    val isLeader: Boolean
        get() = role == ItineraryUserRole.LEADER

    val selectedDay: ItineraryDay?
        get() = days.firstOrNull { it.dayIndex == selectedDayIndex }
}
