package com.mobile.travelhub.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mobile.travelhub.data.PlaceRepository
import com.mobile.travelhub.data.httpStatusCode
import com.mobile.travelhub.data.userMessage
import com.mobile.travelhub.data.model.ProvinceResponse
import com.mobile.travelhub.data.model.TravelPlaceDetailResponse
import com.mobile.travelhub.data.model.TravelPlaceListItemResponse
import com.mobile.travelhub.data.model.TravelPlaceReviewResponse
import com.mobile.travelhub.data.model.TravelPlaceReviewSummaryResponse
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class PlaceDetailUiModel(
    val id: Long,
    val name: String,
    val description: String?,
    val province: ProvinceResponse,
    val lat: Double?,
    val lon: Double?,
    val mainImage: String?,
    val imageUrls: List<String>,
    val views: Int?,
    val openingTime: String?,
    val reviewSummary: TravelPlaceReviewSummaryResponse,
    val myReview: TravelPlaceReviewResponse? = null
)

data class PlaceDetailUiState(
    val isLoading: Boolean = false,
    val detail: PlaceDetailUiModel? = null,
    val relatedPlaces: List<TravelPlaceListItemResponse> = emptyList(),
    val relatedPlacesLoading: Boolean = false,
    val reviewPreview: List<TravelPlaceReviewResponse> = emptyList(),
    val reviewPreviewLoading: Boolean = false,
    val errorMessage: String? = null,
    val reviewErrorMessage: String? = null,
    val relatedPlacesErrorMessage: String? = null
)

@HiltViewModel
class PlaceDetailViewModel @Inject constructor(
    private val placeRepository: PlaceRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(PlaceDetailUiState())
    val uiState: StateFlow<PlaceDetailUiState> = _uiState.asStateFlow()

    private var loadedPlaceId: Long? = null
    private var loadedDetailPlaceId: Long? = null

    fun loadPlace(place: TravelPlaceListItemResponse) {
        if (loadedPlaceId == place.id && loadedDetailPlaceId == place.id && uiState.value.detail != null) {
            return
        }
        loadedPlaceId = place.id
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isLoading = true,
                    detail = place.toDetailUiModel(),
                    errorMessage = null,
                    relatedPlaces = emptyList(),
                    relatedPlacesLoading = true,
                    relatedPlacesErrorMessage = null,
                    reviewErrorMessage = null,
                    reviewPreview = emptyList(),
                    reviewPreviewLoading = true
                )
            }
            runCatching {
                retryTransientServerError {
                    placeRepository.getPlaceDetail(place.id)
                }
            }.onSuccess { response ->
                val detail = response.toDetailUiModel()
                loadedDetailPlaceId = detail.id
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        detail = detail,
                        errorMessage = null,
                        relatedPlacesLoading = true,
                        reviewPreviewLoading = true
                    )
                }
                loadRelatedPlaces(detail.id, detail.province.id)
                loadReviewPreview(detail.id)
            }.onFailure { throwable ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = throwable.userMessage("Không thể tải chi tiết địa điểm"),
                        relatedPlacesLoading = true,
                        reviewPreviewLoading = true
                    )
                }
                loadRelatedPlaces(place.id, place.province.id)
                loadReviewPreview(place.id)
            }
        }
    }

    fun loadPlaceById(placeId: Long) {
        if (loadedPlaceId == placeId && uiState.value.detail != null) {
            return
        }
        loadedPlaceId = placeId
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isLoading = true,
                    detail = null,
                    errorMessage = null,
                    relatedPlaces = emptyList(),
                    relatedPlacesLoading = false,
                    relatedPlacesErrorMessage = null,
                    reviewErrorMessage = null,
                    reviewPreview = emptyList(),
                    reviewPreviewLoading = false
                )
            }
            runCatching {
                retryTransientServerError {
                    placeRepository.getPlaceDetail(placeId)
                }
            }.onSuccess { response ->
                val detail = response.toDetailUiModel()
                loadedDetailPlaceId = detail.id
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        detail = detail,
                        errorMessage = null,
                        relatedPlacesLoading = true,
                        reviewPreviewLoading = true
                    )
                }
                loadRelatedPlaces(detail.id, detail.province.id)
                loadReviewPreview(detail.id)
            }.onFailure { throwable ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = throwable.userMessage("Không thể tải địa điểm")
                    )
                }
            }
        }
    }

    fun refreshReviewPreview() {
        loadedPlaceId?.let(::loadReviewPreview)
    }

    private fun loadRelatedPlaces(placeId: Long, provinceId: Long) {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    relatedPlacesLoading = true,
                    relatedPlacesErrorMessage = null
                )
            }
            runCatching {
                retryTransientServerError {
                    placeRepository.getPlaces(
                        page = 0,
                        pageSize = 12,
                        provinceId = provinceId
                    ).data
                }
                    .filterNot { item -> item.id == placeId }
                    .take(6)
            }.onSuccess { places ->
                _uiState.update {
                    it.copy(
                        relatedPlaces = places,
                        relatedPlacesLoading = false,
                        relatedPlacesErrorMessage = null
                    )
                }
            }.onFailure { throwable ->
                _uiState.update {
                    it.copy(
                        relatedPlacesLoading = false,
                        relatedPlacesErrorMessage = throwable.userMessage("Không thể tải địa điểm liên quan")
                    )
                }
            }
        }
    }

    fun loadReviewPreview(placeId: Long) {
        viewModelScope.launch {
            _uiState.update { it.copy(reviewPreviewLoading = true, reviewErrorMessage = null) }
            runCatching {
                retryTransientServerError {
                    placeRepository.getReviews(placeId = placeId, page = 0, pageSize = 3)
                }
            }
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
                            reviewErrorMessage = throwable.userMessage("Không thể tải đánh giá")
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

    private fun TravelPlaceListItemResponse.toDetailUiModel(): PlaceDetailUiModel {
        val imageUrls = images
            .sortedByDescending { it.main }
            .map { it.imageUrl.trim() }
            .filter { it.isNotBlank() }
            .distinct()
            .ifEmpty {
                listOfNotNull(mainImage)
                    .map { it.trim() }
                    .filter { it.isNotBlank() }
                    .distinct()
            }
        return PlaceDetailUiModel(
            id = id,
            name = name,
            description = description,
            province = province,
            lat = null,
            lon = null,
            mainImage = imageUrls.firstOrNull() ?: mainImage,
            imageUrls = imageUrls,
            views = views,
            openingTime = openingTime,
            reviewSummary = TravelPlaceReviewSummaryResponse(
                averageRating = averageRating,
                reviewCount = reviewCount
            )
        )
    }

    private fun TravelPlaceDetailResponse.toDetailUiModel(): PlaceDetailUiModel {
        val imageUrls = images
            .sortedByDescending { it.main }
            .map { it.imageUrl.trim() }
            .filter { it.isNotBlank() }
            .distinct()
        val mainImage = imageUrls.firstOrNull()
        return PlaceDetailUiModel(
            id = id,
            name = name,
            description = description,
            province = province,
            lat = lat,
            lon = lon,
            mainImage = mainImage,
            imageUrls = imageUrls,
            views = views,
            openingTime = openingTime,
            reviewSummary = reviewSummary,
            myReview = myReview
        )
    }

    private suspend fun <T> retryTransientServerError(
        attempts: Int = 3,
        initialDelayMillis: Long = 350,
        block: suspend () -> T
    ): T {
        var nextDelay = initialDelayMillis
        var lastError: Throwable? = null

        repeat(attempts) { attempt ->
            try {
                return block()
            } catch (throwable: Throwable) {
                lastError = throwable
                val shouldRetry = throwable.httpStatusCode() == 500 && attempt < attempts - 1
                if (!shouldRetry) {
                    throw throwable
                }
                delay(nextDelay)
                nextDelay *= 2
            }
        }

        throw lastError ?: IllegalStateException("Yêu cầu không thành công")
    }
}
