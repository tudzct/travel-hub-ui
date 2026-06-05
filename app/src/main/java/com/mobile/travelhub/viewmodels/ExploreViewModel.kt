package com.mobile.travelhub.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mobile.travelhub.data.PlaceRepository
import com.mobile.travelhub.data.SearchHistoryRepository
import com.mobile.travelhub.data.userMessage
import com.mobile.travelhub.data.model.TravelPlaceListItemResponse
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ExploreUiState(
    val recentSearches: List<String> = emptyList(),
    val featuredLocations: List<TravelPlaceListItemResponse> = emptyList(),
    val isLoadingFeaturedLocations: Boolean = false,
    val featuredLocationsError: String? = null
)

@HiltViewModel
class ExploreViewModel @Inject constructor(
    private val searchHistoryRepository: SearchHistoryRepository,
    private val placeRepository: PlaceRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(
        ExploreUiState(recentSearches = searchHistoryRepository.recentSearches.value)
    )
    val uiState: StateFlow<ExploreUiState> = _uiState.asStateFlow()

    init {
        searchHistoryRepository.refresh()
        loadFeaturedLocations()
        viewModelScope.launch {
            searchHistoryRepository.recentSearches.collect { recentSearches ->
                _uiState.update { it.copy(recentSearches = recentSearches) }
            }
        }
    }

    fun loadFeaturedLocations() {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isLoadingFeaturedLocations = true,
                    featuredLocationsError = null
                )
            }
            runCatching {
                placeRepository.getFeaturedPlaces()
            }.onSuccess { featuredLocations ->
                _uiState.update {
                    it.copy(
                        featuredLocations = featuredLocations,
                        isLoadingFeaturedLocations = false,
                        featuredLocationsError = null
                    )
                }
            }.onFailure { throwable ->
                _uiState.update {
                    it.copy(
                        featuredLocations = emptyList(),
                        isLoadingFeaturedLocations = false,
                        featuredLocationsError = throwable.userMessage("Không thể tải địa điểm nổi bật")
                    )
                }
            }
        }
    }
}
