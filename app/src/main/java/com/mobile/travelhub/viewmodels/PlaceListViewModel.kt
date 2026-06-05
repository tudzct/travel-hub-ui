package com.mobile.travelhub.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mobile.travelhub.data.PlaceRepository
import com.mobile.travelhub.data.model.TravelPlaceListItemResponse
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

private const val MIN_RELOAD_LOADING_MS = 500L

data class PlaceListUiState(
    val isLoading: Boolean = false,
    val items: List<TravelPlaceListItemResponse> = emptyList(),
    val errorMessage: String? = null
)

@HiltViewModel
class PlaceListViewModel @Inject constructor(
    private val placeRepository: PlaceRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(PlaceListUiState(isLoading = true))
    val uiState: StateFlow<PlaceListUiState> = _uiState.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            val loadingStartedAt = System.currentTimeMillis()
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            runCatching {
                placeRepository.getRecommendedPlaces()
            }.onSuccess { response ->
                delayRemainingLoadingTime(loadingStartedAt)

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        items = response.data,
                        errorMessage = null
                    )
                }
            }.onFailure { throwable ->
                delayRemainingLoadingTime(loadingStartedAt)

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = throwable.message ?: "Unable to load places"
                    )
                }
            }
        }
    }

    private suspend fun delayRemainingLoadingTime(loadingStartedAt: Long) {
        val remainingMillis = MIN_RELOAD_LOADING_MS - (System.currentTimeMillis() - loadingStartedAt)
        if (remainingMillis > 0) {
            delay(remainingMillis)
        }
    }
}
