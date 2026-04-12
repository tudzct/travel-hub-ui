package com.mobile.travelhub.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mobile.travelhub.data.RecommendationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class OnboardingUiState(
    val tripType: String? = null,
    val interests: List<String> = emptyList(),
    val destination: String? = null,
    val startDate: String = "Oct 12, 2024",
    val endDate: String = "Oct 24, 2024",
    val travelers: Int = 2,
    val budgetLevel: String = "Mid-Range",
    val preferenceSyncErrorMessage: String? = null
)

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val recommendationRepository: RecommendationRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(OnboardingUiState())
    val uiState: StateFlow<OnboardingUiState> = _uiState.asStateFlow()

    fun updateTripType(tripType: String) {
        _uiState.update { it.copy(tripType = tripType) }
        syncPreferences()
    }

    fun updateInterests(interests: List<String>) {
        val normalizedInterests = interests
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .distinct()
        _uiState.update {
            it.copy(
                interests = normalizedInterests,
                preferenceSyncErrorMessage = null
            )
        }
        syncPreferences()
    }

    fun updateDestination(destination: String) {
        _uiState.update { it.copy(destination = destination) }
        syncPreferences()
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

    private fun syncPreferences() {
        val state = _uiState.value
        viewModelScope.launch {
            recommendationRepository
                .syncPreferencesToServer(
                    tripType = state.tripType,
                    interests = state.interests,
                    destination = state.destination
                )
                .onFailure { throwable ->
                    _uiState.update {
                        it.copy(
                            preferenceSyncErrorMessage = throwable.message ?: "Failed to sync preferences"
                        )
                    }
                }
        }
    }
}
