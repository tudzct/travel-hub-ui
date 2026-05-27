package com.mobile.travelhub.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mobile.travelhub.data.PlaceRepository
import com.mobile.travelhub.data.model.TopTravelerResponse
import com.mobile.travelhub.models.EditablePlaceDraft
import com.mobile.travelhub.models.PlaceDetail
import com.mobile.travelhub.models.PlaceSummary
import com.mobile.travelhub.models.toSummary
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class ExploreUiState(
    val isLoading: Boolean = true,
    val popularPlaces: List<PlaceSummary> = emptyList(),
    val topTravelers: List<TopTravelerResponse> = emptyList(),
    val errorMessage: String? = null
)

@HiltViewModel
class PlaceViewModel @Inject constructor(
    private val placeRepository: PlaceRepository
) : ViewModel() {

    private val _exploreState = MutableStateFlow(ExploreUiState())
    val exploreState: StateFlow<ExploreUiState> = _exploreState.asStateFlow()

    val places: StateFlow<List<PlaceSummary>> = placeRepository.observePlaces()
        .map { items -> items.map(PlaceDetail::toSummary) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = placeRepository.getPlaces().map(PlaceDetail::toSummary)
        )

    init {
        refreshExplore()
    }

    fun refreshExplore() {
        viewModelScope.launch {
            _exploreState.value = _exploreState.value.copy(isLoading = true, errorMessage = null)

            val popularResult = runCatching { placeRepository.fetchPopularPlaces(limit = 8) }
            val travelerResult = runCatching { placeRepository.fetchTopTravelers(limit = 5) }

            val popularPlaces = popularResult.getOrNull().orEmpty().map(PlaceDetail::toSummary)
            val topTravelers = travelerResult.getOrNull().orEmpty()
            val errorMessage = buildList {
                popularResult.exceptionOrNull()?.localizedMessage?.let { add("Popular locations: $it") }
                travelerResult.exceptionOrNull()?.localizedMessage?.let { add("Top travelers: $it") }
            }.joinToString("\n").ifBlank { null }

            _exploreState.value = ExploreUiState(
                isLoading = false,
                popularPlaces = popularPlaces,
                topTravelers = topTravelers,
                errorMessage = errorMessage
            )
        }
    }

    fun observePlace(placeId: String): Flow<PlaceDetail?> {
        return placeRepository.observePlaces().map { places ->
            places.firstOrNull { it.id == placeId }
        }
    }

    fun updatePlace(placeId: String, draft: EditablePlaceDraft): Result<PlaceDetail> {
        return placeRepository.updatePlace(placeId, draft)
    }
}
