package com.mobile.travelhub.viewmodels

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

data class OnboardingUiState(
    val tripType: String? = null,
    val interests: List<String> = emptyList(),
    val destination: String? = null,
    val startDate: String = "Oct 12, 2024",
    val endDate: String = "Oct 24, 2024",
    val travelers: Int = 2,
    val budgetLevel: String = "Mid-Range"
)

@HiltViewModel
class OnboardingViewModel @Inject constructor() : ViewModel() {
    private val _uiState = MutableStateFlow(OnboardingUiState())
    val uiState: StateFlow<OnboardingUiState> = _uiState.asStateFlow()

    fun updateTripType(tripType: String) {
        _uiState.update { it.copy(tripType = tripType) }
    }

    fun updateInterests(interests: List<String>) {
        _uiState.update { it.copy(interests = interests) }
    }

    fun updateDestination(destination: String) {
        _uiState.update { it.copy(destination = destination) }
    }

    fun updateDetails(
        startDate: String,
        endDate: String,
        travelers: Int,
        budgetLevel: String
    ) {
        _uiState.update {
            it.copy(
                startDate = startDate,
                endDate = endDate,
                travelers = travelers.coerceAtLeast(1),
                budgetLevel = budgetLevel
            )
        }
    }
}
