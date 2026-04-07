package com.mobile.travelhub.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mobile.travelhub.data.PlaceRepository
import com.mobile.travelhub.models.EditablePlaceDraft
import com.mobile.travelhub.models.PlaceDetail
import com.mobile.travelhub.models.PlaceSummary
import com.mobile.travelhub.models.toSummary
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

@HiltViewModel
class PlaceViewModel @Inject constructor(
    private val placeRepository: PlaceRepository
) : ViewModel() {

    val places: StateFlow<List<PlaceSummary>> = placeRepository.observePlaces()
        .map { items -> items.map(PlaceDetail::toSummary) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = placeRepository.getPlaces().map(PlaceDetail::toSummary)
        )

    fun observePlace(placeId: String): Flow<PlaceDetail?> {
        return placeRepository.observePlaces().map { places ->
            places.firstOrNull { it.id == placeId }
        }
    }

    fun updatePlace(placeId: String, draft: EditablePlaceDraft): Result<PlaceDetail> {
        return placeRepository.updatePlace(placeId, draft)
    }
}
