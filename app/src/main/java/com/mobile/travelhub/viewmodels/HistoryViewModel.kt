package com.mobile.travelhub.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mobile.travelhub.data.PlaceRepository
import com.mobile.travelhub.data.httpStatusCode
import com.mobile.travelhub.data.model.TravelPlaceViewHistoryResponse
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class HistoryUiState(
    val isLoading: Boolean = false,
    val items: List<TravelPlaceViewHistoryResponse> = emptyList(),
    val errorMessage: String? = null,
    val unauthorized: Boolean = false
)

@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val placeRepository: PlaceRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HistoryUiState(isLoading = true))
    val uiState: StateFlow<HistoryUiState> = _uiState.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null, unauthorized = false) }
            runCatching { placeRepository.getViewHistory(page = 0, pageSize = 20) }
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
                            unauthorized = throwable.httpStatusCode() == 401,
                            errorMessage = when (throwable.httpStatusCode()) {
                                401 -> "Bạn cần đăng nhập để xem lịch sử"
                                else -> throwable.message ?: "Không thể tải lịch sử xem"
                            }
                        )
                    }
                }
        }
    }

    fun clearUnauthorized() {
        _uiState.update { it.copy(unauthorized = false) }
    }
}
