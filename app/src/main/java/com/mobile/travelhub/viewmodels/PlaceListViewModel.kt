package com.mobile.travelhub.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mobile.travelhub.data.AuthRepository
import com.mobile.travelhub.data.PlaceRepository
import com.mobile.travelhub.data.model.TravelPlaceListItemResponse
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class PlaceListUiState(
    val isLoading: Boolean = false,
    val items: List<TravelPlaceListItemResponse> = emptyList(),
    val keyword: String = "",
    val errorMessage: String? = null
)

@HiltViewModel
class PlaceListViewModel @Inject constructor(
    private val placeRepository: PlaceRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(PlaceListUiState(isLoading = true))
    val uiState: StateFlow<PlaceListUiState> = _uiState.asStateFlow()

    private var searchJob: Job? = null

    init {
        refresh()
    }

    fun onKeywordChange(keyword: String) {
        _uiState.update { it.copy(keyword = keyword) }
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            delay(300)
            refresh()
        }
    }

    fun refresh() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            val keyword = uiState.value.keyword.trim().ifBlank { null }
            val session = authRepository.getSavedSession()
            val shouldUseRecommendations = keyword == null && session?.isOnboarded == true

            runCatching {
                if (shouldUseRecommendations) {
                    runCatching {
                        placeRepository.getRecommendedPlaces()
                    }.getOrElse {
                        placeRepository.getPlaces(keyword = keyword)
                    }
                } else {
                    placeRepository.getPlaces(keyword = keyword)
                }
            }.onSuccess { response ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        items = response.data,
                        errorMessage = null
                    )
                }
            }.onFailure { throwable ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = throwable.message ?: "Unable to load places"
                    )
                }
            }
        }
    }
}
