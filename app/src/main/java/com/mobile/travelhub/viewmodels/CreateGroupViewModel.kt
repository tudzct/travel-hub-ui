package com.mobile.travelhub.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mobile.travelhub.data.TripRepository
import com.mobile.travelhub.data.model.CreateTripRequest
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class CreateGroupUiState(
    val name: String = "",
    val destination: String = "",
    val startDate: String = "",
    val endDate: String = "",
    val budgetMin: String = "",
    val budgetMax: String = "",
    val isSaving: Boolean = false,
    val errorMessage: String? = null
)

@HiltViewModel
class CreateGroupViewModel @Inject constructor(
    private val tripRepository: TripRepository
) : ViewModel() {

    private val displayDateFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")

    private val _uiState = MutableStateFlow(CreateGroupUiState())
    val uiState: StateFlow<CreateGroupUiState> = _uiState.asStateFlow()

    fun updateName(value: String) {
        _uiState.update { it.copy(name = value, errorMessage = null) }
    }

    fun updateDestination(value: String) {
        _uiState.update { it.copy(destination = value, errorMessage = null) }
    }

    fun updateStartDate(value: String) {
        _uiState.update { it.copy(startDate = value, errorMessage = null) }
    }

    fun updateEndDate(value: String) {
        _uiState.update { it.copy(endDate = value, errorMessage = null) }
    }

    fun updateBudgetMin(value: String) {
        _uiState.update { it.copy(budgetMin = value, errorMessage = null) }
    }

    fun updateBudgetMax(value: String) {
        _uiState.update { it.copy(budgetMax = value, errorMessage = null) }
    }

    // Check if a date can be selected for start date (must be today or later)
    fun canSelectStartDate(date: LocalDate): Boolean {
        return !date.isBefore(LocalDate.now())
    }

    // Get maximum selectable end date (start date + 60 days)
    fun getMaxEndDate(startDate: LocalDate?): LocalDate? {
        return startDate?.plusDays(60)
    }

    // Validate all date constraints
    private fun validateDates(startDate: LocalDate, endDate: LocalDate): String? {
        val today = LocalDate.now()
        
        // Check start date >= today
        if (startDate.isBefore(today)) {
            return "Ngày đi phải lớn hơn hoặc bằng ngày hôm nay"
        }
        
        // Check end date >= start date
        if (endDate.isBefore(startDate)) {
            return "Ngày kết thúc phải lớn hơn hoặc bằng ngày đi"
        }
        
        // Check end date <= start date + 60 days
        if (endDate.isAfter(startDate.plusDays(60))) {
            return "Ngày kết thúc không được vượt quá 60 ngày từ ngày đi"
        }
        
        return null
    }

    fun createTrip(onCreated: (Long) -> Unit) {
        val state = _uiState.value
        val name = state.name.trim()
        val destination = state.destination.trim()
        val startDate = parseDisplayDate(state.startDate.trim())
        val endDate = parseDisplayDate(state.endDate.trim())

        if (name.isBlank() || destination.isBlank() || startDate == null || endDate == null) {
            _uiState.update { it.copy(errorMessage = "Vui lòng nhập đủ tên, điểm đến và ngày đi") }
            return
        }

        // Validate date constraints
        val dateError = validateDates(startDate, endDate)
        if (dateError != null) {
            _uiState.update { it.copy(errorMessage = dateError) }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, errorMessage = null) }
            val request = CreateTripRequest(
                name = name,
                destination = destination,
                startDate = startDate.toString(),
                endDate = endDate.toString(),
                budgetMin = state.budgetMin.trim().toDoubleOrNull(),
                budgetMax = state.budgetMax.trim().toDoubleOrNull()
            )

            tripRepository.createTrip(request)
                .onSuccess { tripId ->
                    _uiState.update { it.copy(isSaving = false, errorMessage = null) }
                    onCreated(tripId)
                }
                .onFailure { throwable ->
                    _uiState.update {
                        it.copy(
                            isSaving = false,
                            errorMessage = throwable.message ?: "Không tạo được chuyến đi"
                        )
                    }
                }
        }
    }

    private fun parseDisplayDate(value: String): LocalDate? {
        if (value.isBlank()) {
            return null
        }

        return runCatching {
            LocalDate.parse(value, displayDateFormatter)
        }.getOrNull()
    }
}