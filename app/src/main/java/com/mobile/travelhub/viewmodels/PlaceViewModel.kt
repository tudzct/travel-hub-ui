package com.mobile.travelhub.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mobile.travelhub.data.PlaceRepository
import com.mobile.travelhub.data.RecommendationRepository
import com.mobile.travelhub.models.EditablePlaceDraft
import com.mobile.travelhub.models.PlaceDetail
import com.mobile.travelhub.models.PlaceSummary
import com.mobile.travelhub.models.toSummary
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlin.math.ln
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

@HiltViewModel
class PlaceViewModel @Inject constructor(
    private val placeRepository: PlaceRepository,
    private val recommendationRepository: RecommendationRepository
) : ViewModel() {

    val places: StateFlow<List<PlaceSummary>> = combine(
        placeRepository.observePlaces(),
        recommendationRepository.placeClicks,
        recommendationRepository.provinceClicks
    ) { placeDetails, placeClicks, provinceClicks ->
        val summaries = placeDetails.map(PlaceDetail::toSummary)
        rankPlaces(summaries, placeClicks, provinceClicks)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = rankPlaces(
            placeRepository.getPlaces().map(PlaceDetail::toSummary),
            recommendationRepository.placeClicks.value,
            recommendationRepository.provinceClicks.value
        )
    )

    val hasPersonalSignals: StateFlow<Boolean> = recommendationRepository.placeClicks
        .map { it.isNotEmpty() }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = recommendationRepository.placeClicks.value.isNotEmpty()
        )

    fun observePlace(placeId: String): Flow<PlaceDetail?> {
        return placeRepository.observePlaces().map { places ->
            places.firstOrNull { it.id == placeId }
        }
    }

    fun recordPlaceOpened(place: PlaceSummary) {
        recommendationRepository.recordPlaceOpened(place.id, place.provinceName)
    }

    fun updatePlace(placeId: String, draft: EditablePlaceDraft): Result<PlaceDetail> {
        return placeRepository.updatePlace(placeId, draft)
    }

    private fun rankPlaces(
        places: List<PlaceSummary>,
        placeClicks: Map<String, Int>,
        provinceClicks: Map<String, Int>
    ): List<PlaceSummary> {
        if (placeClicks.isEmpty() && provinceClicks.isEmpty()) {
            return places
        }

        return places.sortedWith(
            compareByDescending<PlaceSummary> { place ->
                val clickScore = ln((placeClicks[place.id] ?: 0) + 1.0)
                val provinceScore = ln((provinceClicks[place.provinceName.trim().lowercase()] ?: 0) + 1.0)
                (clickScore * 0.7) + (provinceScore * 0.3)
            }.thenBy { it.title }
        )
    }
}
