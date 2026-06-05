package com.mobile.travelhub.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mobile.travelhub.data.ItineraryRepository
import com.mobile.travelhub.data.TripRepository
import com.mobile.travelhub.data.model.ItineraryDay
import com.mobile.travelhub.data.model.ItineraryEvent
import com.mobile.travelhub.data.model.ItineraryEventColors
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Locale
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class ItineraryViewModel @Inject constructor(
    private val repository: ItineraryRepository,
    private val tripRepository: TripRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ItineraryUiState())
    val uiState: StateFlow<ItineraryUiState> = _uiState.asStateFlow()

    private var workspaceJob: Job? = null
    private var boundGroupName: String? = null
    private var boundTripId: Long? = null
    private var isMutatingActivities = false

    fun bindGroup(groupName: String, tripId: Long? = null) {
        if (boundGroupName == groupName && boundTripId == tripId) return
        boundGroupName = groupName
        boundTripId = tripId
        isMutatingActivities = false
        workspaceJob?.cancel()
        _uiState.update {
            it.copy(
                groupName = groupName,
                isLoadingActivities = true,
                errorMessage = null
            )
        }
        workspaceJob = viewModelScope.launch {
            runCatching { repository.refreshWorkspace(groupName, tripId) }
                .onFailure { throwable ->
                    _uiState.update {
                        it.copy(
                            isLoadingActivities = false,
                            errorMessage = throwable.message ?: "Unable to load itinerary"
                        )
                    }
                }
            loadTripDayOptions(tripId)
            repository.observeWorkspace(groupName).collect { workspace ->
                val selectedDayIndex = _uiState.value.selectedDayIndex
                    .takeIf { value -> workspace.days.any { it.dayIndex == value } }
                    ?: workspace.days.firstOrNull()?.dayIndex
                    ?: 1

                _uiState.update {
                    it.copy(
                        groupName = workspace.groupName,
                        version = workspace.version,
                        role = workspace.role,
                        days = workspace.days,
                        isLoadingActivities = isMutatingActivities,
                        selectedDayIndex = selectedDayIndex
                    )
                }
            }
        }
    }

    fun selectDay(dayIndex: Int) {
        _uiState.update { it.copy(selectedDayIndex = dayIndex) }
    }

    fun startEditing(event: ItineraryEvent) {
        _uiState.update {
            it.copy(
                editingDay = null,
                editingEvent = event,
                isCreatingEvent = false,
                errorMessage = null
            )
        }
    }

    fun startEditingDay(day: ItineraryDay) {
        _uiState.update {
            it.copy(
                editingDay = day,
                editingEvent = null,
                isCreatingEvent = false,
                errorMessage = null
            )
        }
    }

    fun startAddingStop() {
        val state = _uiState.value
        if (state.isCompleted) {
            _uiState.update { it.copy(errorMessage = "Không thể chỉnh sửa chuyến đi đã hoàn thành") }
            return
        }
        if (state.dayOptions.isEmpty()) {
            _uiState.update { it.copy(errorMessage = "Không tải được danh sách ngày của trip") }
            return
        }
        val selectedOption = state.dayOptions.firstOrNull { it.dayIndex == state.selectedDayIndex }
            ?: state.dayOptions.firstOrNull()
        val selectedDay = state.days.firstOrNull { it.dayIndex == selectedOption?.dayIndex }
            ?: state.selectedDay
            ?: state.days.firstOrNull()
        val selectedDayIndex = selectedOption?.dayIndex ?: selectedDay?.dayIndex ?: 1
        val anchorEvent = selectedDay?.events?.lastOrNull()
        val eventCount = selectedDay?.events?.size ?: 0
        val draftEvent = ItineraryEvent(
            eventId = "manual-${System.currentTimeMillis()}",
            dayIndex = selectedDayIndex,
            startTime = anchorEvent?.endTime ?: "09:00",
            endTime = nextHour(anchorEvent?.endTime ?: "09:00"),
            title = "",
            placeName = "",
            note = "",
            transportToNext = "",
            estimatedCost = "",
            colorHex = ItineraryEventColors.Palette[eventCount % ItineraryEventColors.Palette.size],
            iconName = "Place",
            dayId = selectedDay?.dayId
        )
        _uiState.update {
            it.copy(
                selectedDayIndex = selectedDayIndex,
                editingEvent = draftEvent,
                isCreatingEvent = true,
                errorMessage = null
            )
        }
    }

    fun cancelEditing() {
        _uiState.update { it.copy(editingEvent = null, isCreatingEvent = false) }
    }

    fun cancelEditingDay() {
        _uiState.update { it.copy(editingDay = null) }
    }

    fun saveEvent(updatedEvent: ItineraryEvent) {
        val groupName = boundGroupName ?: return
        val wasCreating = _uiState.value.isCreatingEvent
        _uiState.update {
            it.copy(
                editingEvent = null,
                isCreatingEvent = false,
                isLoadingActivities = true,
                errorMessage = null
            )
        }
        launchMutation {
            val selectedOption = _uiState.value.dayOptions.firstOrNull { it.dayIndex == updatedEvent.dayIndex }
            if (wasCreating && selectedOption != null) {
                _uiState.value.dayOptions
                    .filter { it.dayIndex <= selectedOption.dayIndex }
                    .sortedBy { it.dayIndex }
                    .forEach { option ->
                        repository.ensureDay(
                            groupName = groupName,
                            dayIndex = option.dayIndex,
                            label = option.label,
                            dateLabel = option.dateLabel
                        )
                }
            }
            repository.updateEvent(groupName, updatedEvent)
        }
    }

    fun saveDay(updatedDay: ItineraryDay) {
        val groupName = boundGroupName ?: return
        launchMutation {
            repository.updateDay(groupName, updatedDay)
            _uiState.update { it.copy(editingDay = null) }
        }
    }

    fun deleteEditingEvent() {
        val groupName = boundGroupName ?: return
        val event = _uiState.value.editingEvent ?: return
        if (_uiState.value.isCreatingEvent) {
            _uiState.update { it.copy(editingEvent = null, isCreatingEvent = false) }
            return
        }
        _uiState.update {
            it.copy(
                editingEvent = null,
                isCreatingEvent = false,
                isLoadingActivities = true,
                errorMessage = null
            )
        }
        launchMutation {
            repository.deleteEvent(groupName, event)
        }
    }

    fun deleteEvent(eventId: String) {
        val groupName = boundGroupName ?: return
        _uiState.update {
            it.copy(
                editingEvent = null,
                isCreatingEvent = false,
                isLoadingActivities = true,
                errorMessage = null
            )
        }
        launchMutation {
            repository.deleteEvent(groupName, eventId)
        }
    }

    fun deleteEditingDay() {
        val groupName = boundGroupName ?: return
        val day = _uiState.value.editingDay ?: return
        launchMutation {
            repository.deleteDay(groupName, day.dayIndex)
            _uiState.update { it.copy(editingDay = null) }
        }
    }

    fun deleteDay(dayIndex: Int) {
        val groupName = boundGroupName ?: return
        launchMutation {
            repository.deleteDay(groupName, dayIndex)
            _uiState.update { it.copy(editingDay = null) }
        }
    }

    fun addDay() {
        val groupName = boundGroupName ?: return
        launchMutation {
            val newDayIndex = repository.addDay(groupName)
            _uiState.update { it.copy(selectedDayIndex = newDayIndex) }
        }
    }

    fun moveEvent(eventId: String, moveUp: Boolean) {
        val groupName = boundGroupName ?: return
        val dayIndex = _uiState.value.selectedDayIndex
        launchMutation {
            repository.reorderEvent(
                groupName = groupName,
                dayIndex = dayIndex,
                eventId = eventId,
                moveUp = moveUp
            )
        }
    }

    fun reorderDayEvents(fromIndex: Int, toIndex: Int) {
        val groupName = boundGroupName ?: return
        val dayIndex = _uiState.value.selectedDayIndex
        launchMutation {
            repository.reorderEvents(
                groupName = groupName,
                dayIndex = dayIndex,
                fromIndex = fromIndex,
                toIndex = toIndex
            )
        }
    }

    fun toggleEditMode() {
        _uiState.update { it.copy(isEditMode = !it.isEditMode) }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    private fun nextHour(time: String): String {
        val parts = time.split(":")
        if (parts.size != 2) return time
        val hour = parts[0].toIntOrNull() ?: return time
        val minute = parts[1].toIntOrNull() ?: return time
        val nextHour = (hour + 1).coerceAtMost(23)
        return "%02d:%02d".format(nextHour, minute)
    }

    private suspend fun loadTripDayOptions(tripId: Long?) {
        if (tripId == null || tripId <= 0) return
        tripRepository.getTripDetail(tripId)
            .onSuccess { detail ->
                val isCompleted = detail.tripInfo.status.equals("COMPLETED", ignoreCase = true) ||
                        detail.tripInfo.status?.contains("hoàn thành", ignoreCase = true) == true ||
                        isPastDate(detail.tripInfo.endDate)
                val options = buildDayOptions(
                    startDateText = detail.tripInfo.startDate,
                    endDateText = detail.tripInfo.endDate
                )
                _uiState.update { state ->
                    state.copy(
                        isCompleted = isCompleted,
                        dayOptions = options.ifEmpty { state.dayOptions }
                    )
                }
            }
    }

    private fun buildDayOptions(
        startDateText: String?,
        endDateText: String?
    ): List<ItineraryDayOption> {
        val startDate = parseTripDate(startDateText) ?: return emptyList()
        val endDate = parseTripDate(endDateText) ?: return emptyList()
        if (endDate.isBefore(startDate)) return emptyList()

        val displayFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy", Locale.getDefault())
        val dayCount = ChronoUnit.DAYS.between(startDate, endDate).toInt() + 1
        return List(dayCount) { index ->
            val date = startDate.plusDays(index.toLong())
            ItineraryDayOption(
                dayIndex = index + 1,
                label = "Day ${index + 1}",
                dateLabel = date.format(displayFormatter),
                epochDay = date.toEpochDay()
            )
        }
    }

    private fun parseTripDate(value: String?): LocalDate? {
        if (value.isNullOrBlank()) return null
        val normalized = value.substringBefore("T")
        return runCatching { LocalDate.parse(normalized) }
            .recoverCatching {
                LocalDate.parse(
                    normalized,
                    DateTimeFormatter.ofPattern("dd/MM/yyyy", Locale.getDefault())
                )
            }
            .getOrNull()
    }

    private fun isPastDate(dateText: String?): Boolean {
        val date = parseTripDate(dateText) ?: return false
        return date.isBefore(LocalDate.now())
    }

    private fun launchMutation(block: suspend () -> Unit) {
        isMutatingActivities = true
        _uiState.update {
            it.copy(
                isLoadingActivities = true,
                errorMessage = null
            )
        }
        viewModelScope.launch {
            val result = runCatching { block() }
            isMutatingActivities = false
            result
                .onFailure { throwable ->
                    _uiState.update {
                        it.copy(
                            isLoadingActivities = false,
                            errorMessage = throwable.message ?: "Unable to update itinerary"
                        )
                    }
                }
                .onSuccess {
                    _uiState.update { it.copy(isLoadingActivities = false) }
                }
        }
    }
}
