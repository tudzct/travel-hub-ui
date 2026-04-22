package com.mobile.travelhub.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mobile.travelhub.data.PlaceRepository
import com.mobile.travelhub.data.httpStatusCode
import com.mobile.travelhub.data.model.TravelPlaceReviewResponse
import com.mobile.travelhub.data.model.UpsertTravelPlaceReviewRequest
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ReviewUiState(
    val rating: Int = 5,
    val content: String = "",
    val isSubmitting: Boolean = false,
    val errorMessage: String? = null,
    val unauthorized: Boolean = false,
    val submittedReview: TravelPlaceReviewResponse? = null
)

@HiltViewModel
class ReviewViewModel @Inject constructor(
    private val placeRepository: PlaceRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ReviewUiState())
    val uiState: StateFlow<ReviewUiState> = _uiState.asStateFlow()

    private var initializedReviewId: Long? = null

    fun initialize(existingReview: TravelPlaceReviewResponse?) {
        if (initializedReviewId == existingReview?.id) {
            return
        }
        initializedReviewId = existingReview?.id
        _uiState.value = ReviewUiState(
            rating = existingReview?.rating ?: 5,
            content = existingReview?.content.orEmpty()
        )
    }

    fun updateRating(rating: Int) {
        _uiState.update { it.copy(rating = rating.coerceIn(1, 5), errorMessage = null) }
    }

    fun updateContent(content: String) {
        _uiState.update { it.copy(content = content, errorMessage = null) }
    }

    fun submit(placeId: Long) {
        val content = uiState.value.content.trim()
        if (content.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Nội dung review không được để trống") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true, errorMessage = null, unauthorized = false) }
            runCatching {
                placeRepository.upsertReview(
                    placeId = placeId,
                    body = UpsertTravelPlaceReviewRequest(
                        rating = uiState.value.rating,
                        content = content
                    )
                )
            }.onSuccess { response ->
                _uiState.update {
                    it.copy(
                        isSubmitting = false,
                        submittedReview = response
                    )
                }
            }.onFailure { throwable ->
                _uiState.update {
                    it.copy(
                        isSubmitting = false,
                        unauthorized = throwable.httpStatusCode() == 401,
                        errorMessage = when (throwable.httpStatusCode()) {
                            400 -> "Rating hoặc nội dung review không hợp lệ"
                            401 -> "Bạn cần đăng nhập để review"
                            403 -> "Bạn không có quyền thực hiện thao tác này"
                            404 -> "Không tìm thấy địa điểm"
                            else -> throwable.message ?: "Không thể gửi review"
                        }
                    )
                }
            }
        }
    }

    fun consumeSubmittedReview() {
        _uiState.update { it.copy(submittedReview = null) }
    }

    fun clearUnauthorized() {
        _uiState.update { it.copy(unauthorized = false) }
    }
}
