package com.mobile.travelhub.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mobile.travelhub.data.PlaceRepository
import com.mobile.travelhub.data.model.TravelPlaceReviewResponse
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ReviewListUiState(
    val isLoading: Boolean = false,
    val items: List<TravelPlaceReviewResponse> = emptyList(),
    val errorMessage: String? = null
)

@HiltViewModel
class ReviewListViewModel @Inject constructor(
    private val placeRepository: PlaceRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ReviewListUiState(isLoading = true))
    val uiState: StateFlow<ReviewListUiState> = _uiState.asStateFlow()

    private var loadedPlaceId: Long? = null

    fun load(placeId: Long) {
        if (loadedPlaceId == placeId && uiState.value.items.isNotEmpty()) {
            return
        }
        loadedPlaceId = placeId
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            runCatching { placeRepository.getReviews(placeId = placeId, page = 0, pageSize = 20) }
                .onSuccess { response ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            items = response.data,
                            errorMessage = null
                        )
                    }
                }
                .onFailure { throwable ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = throwable.message ?: "Không thể tải review"
                        )
                    }
                }
        }
    }
}
