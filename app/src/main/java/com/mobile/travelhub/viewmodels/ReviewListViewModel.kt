package com.mobile.travelhub.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mobile.travelhub.data.PlaceRepository
import com.mobile.travelhub.data.httpStatusCode
import com.mobile.travelhub.data.userMessage
import com.mobile.travelhub.data.model.TravelPlaceReviewListSummaryResponse
import com.mobile.travelhub.data.model.TravelPlaceReviewResponse
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ReviewListUiState(
    val isLoading: Boolean = false,
    val isLoadingMore: Boolean = false,
    val summary: TravelPlaceReviewListSummaryResponse? = null,
    val items: List<TravelPlaceReviewResponse> = emptyList(),
    val totalElements: Long = 0,
    val totalPages: Int = 0,
    val page: Int = 0,
    val selectedRating: Int? = null,
    val sort: String = "NEWEST",
    val errorMessage: String? = null
) {
    val hasMore: Boolean
        get() = page + 1 < totalPages
}

@HiltViewModel
class ReviewListViewModel @Inject constructor(
    private val placeRepository: PlaceRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ReviewListUiState(isLoading = true))
    val uiState: StateFlow<ReviewListUiState> = _uiState.asStateFlow()

    private var loadedPlaceId: Long? = null
    private val pageSize = 20

    fun load(placeId: Long) {
        if (loadedPlaceId == placeId && uiState.value.summary != null) {
            return
        }
        loadedPlaceId = placeId
        _uiState.value = ReviewListUiState(isLoading = true)
        loadFirstPage()
    }

    fun refresh() {
        loadFirstPage()
    }

    fun selectRating(rating: Int?) {
        if (_uiState.value.selectedRating == rating) {
            return
        }
        _uiState.update { it.copy(selectedRating = rating, isLoading = true, errorMessage = null) }
        loadFirstPage()
    }

    fun selectSort(sort: String) {
        if (_uiState.value.sort == sort) {
            return
        }
        _uiState.update { it.copy(sort = sort, isLoading = true, errorMessage = null) }
        loadFirstPage()
    }

    fun loadMore() {
        val placeId = loadedPlaceId ?: return
        val state = _uiState.value
        if (state.isLoading || state.isLoadingMore || !state.hasMore) {
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingMore = true, errorMessage = null) }
            runCatching {
                retryTransientServerError {
                    placeRepository.getReviews(
                        placeId = placeId,
                        page = state.page + 1,
                        pageSize = pageSize,
                        rating = state.selectedRating,
                        sort = state.sort
                    )
                }
            }
                .onSuccess { response ->
                    _uiState.update {
                        it.copy(
                            isLoadingMore = false,
                            items = it.items + response.data,
                            page = response.pageNumber,
                            totalPages = response.totalPages,
                            totalElements = response.totalElements,
                            errorMessage = null
                        )
                    }
                }
                .onFailure { throwable ->
                    _uiState.update {
                        it.copy(
                            isLoadingMore = false,
                            errorMessage = throwable.userMessage("Không thể tải đánh giá")
                        )
                    }
                }
        }
    }

    private fun loadFirstPage() {
        val placeId = loadedPlaceId ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, isLoadingMore = false, errorMessage = null) }
            runCatching {
                retryTransientServerError {
                    val summary = placeRepository.getReviewSummary(placeId)
                    val response = placeRepository.getReviews(
                        placeId = placeId,
                        page = 0,
                        pageSize = pageSize,
                        rating = _uiState.value.selectedRating,
                        sort = _uiState.value.sort
                    )
                    summary to response
                }
            }
                .onSuccess { (summary, response) ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            summary = summary,
                            items = response.data,
                            page = response.pageNumber,
                            totalPages = response.totalPages,
                            totalElements = response.totalElements,
                            errorMessage = null
                        )
                    }
                }
                .onFailure { throwable ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = throwable.userMessage("Không thể tải đánh giá")
                        )
                    }
                }
        }
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
