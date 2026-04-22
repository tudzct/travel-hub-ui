package com.mobile.travelhub.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mobile.travelhub.data.AuthRepository
import com.mobile.travelhub.data.PlaceRepository
import com.mobile.travelhub.data.model.TravelPlaceDetailResponse
import com.mobile.travelhub.data.model.TravelPlaceReviewResponse
import com.mobile.travelhub.data.model.TravelPlaceReviewSummaryResponse
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class PlaceDetailUiState(
    val isLoading: Boolean = false,
    val detail: TravelPlaceDetailResponse? = null,
    val reviewPreview: List<TravelPlaceReviewResponse> = emptyList(),
    val reviewPreviewLoading: Boolean = false,
    val errorMessage: String? = null,
    val reviewErrorMessage: String? = null,
    val isAdmin: Boolean = false
)

@HiltViewModel
class PlaceDetailViewModel @Inject constructor(
    private val placeRepository: PlaceRepository,
    authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(PlaceDetailUiState(isAdmin = authRepository.isAdmin()))
    val uiState: StateFlow<PlaceDetailUiState> = _uiState.asStateFlow()

    private var loadedPlaceId: Long? = null

    fun loadPlace(placeId: Long) {
        if (loadedPlaceId == placeId && uiState.value.detail != null) {
            return
        }
        loadedPlaceId = placeId
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isLoading = true,
                    errorMessage = null,
                    reviewErrorMessage = null,
                    reviewPreview = emptyList()
                )
            }
            runCatching { placeRepository.getPlaceDetail(placeId) }
                .onSuccess { detail ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            detail = detail,
                            errorMessage = null
                        )
                    }
                    loadReviewPreview(placeId)
                }
                .onFailure { throwable ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = throwable.message ?: "Unable to load place detail"
                        )
                    }
                }
        }
    }

    fun refreshReviewPreview() {
        loadedPlaceId?.let(::loadReviewPreview)
    }

    fun loadReviewPreview(placeId: Long) {
        viewModelScope.launch {
            _uiState.update { it.copy(reviewPreviewLoading = true, reviewErrorMessage = null) }
            runCatching { placeRepository.getReviews(placeId = placeId, page = 0, pageSize = 3) }
                .onSuccess { response ->
                    _uiState.update {
                        it.copy(
                            reviewPreviewLoading = false,
                            reviewPreview = response.data,
                            reviewErrorMessage = null
                        )
                    }
                }
                .onFailure { throwable ->
                    _uiState.update {
                        it.copy(
                            reviewPreviewLoading = false,
                            reviewErrorMessage = throwable.message ?: "Unable to load reviews"
                        )
                    }
                }
        }
    }

    fun applyReviewSaved(review: TravelPlaceReviewResponse) {
        val currentDetail = uiState.value.detail ?: return
        val previousReview = currentDetail.myReview
        val currentSummary = currentDetail.reviewSummary

        val updatedSummary = if (previousReview == null) {
            val newCount = currentSummary.reviewCount + 1
            val newAverage = if (newCount <= 0) {
                review.rating.toDouble()
            } else {
                ((currentSummary.averageRating * currentSummary.reviewCount) + review.rating) / newCount
            }
            TravelPlaceReviewSummaryResponse(
                averageRating = newAverage,
                reviewCount = newCount
            )
        } else {
            val count = currentSummary.reviewCount
            val newAverage = if (count <= 0) {
                currentSummary.averageRating
            } else {
                ((currentSummary.averageRating * count) - previousReview.rating + review.rating) / count
            }
            TravelPlaceReviewSummaryResponse(
                averageRating = newAverage,
                reviewCount = count
            )
        }

        _uiState.update {
            it.copy(
                detail = currentDetail.copy(
                    myReview = review,
                    reviewSummary = updatedSummary
                )
            )
        }
        refreshReviewPreview()
    }
}
