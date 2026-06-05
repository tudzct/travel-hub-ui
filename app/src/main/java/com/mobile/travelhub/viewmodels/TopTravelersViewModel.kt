package com.mobile.travelhub.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mobile.travelhub.data.userMessage
import com.mobile.travelhub.data.api.UserApiService
import com.mobile.travelhub.data.model.TopTravelerPeriod
import com.mobile.travelhub.data.model.TopTravelerResponse
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class TopTravelersUiState(
    val period: TopTravelerPeriod = TopTravelerPeriod.WEEK,
    val items: List<TopTravelerResponse> = emptyList(),
    val isLoading: Boolean = false,
    val isLoadingMore: Boolean = false,
    val errorMessage: String? = null,
    val actionErrorMessage: String? = null,
    val requestingFollowIds: Set<Long> = emptySet(),
    val page: Int = 0,
    val totalPages: Int = 0
)

@HiltViewModel
class TopTravelersViewModel @Inject constructor(
    private val userApiService: UserApiService
) : ViewModel() {
    private val _uiState = MutableStateFlow(TopTravelersUiState())
    val uiState: StateFlow<TopTravelersUiState> = _uiState.asStateFlow()

    private var pageSize: Int = PREVIEW_SIZE
    private var listMode: Boolean = false

    fun loadPreview(period: TopTravelerPeriod = _uiState.value.period) {
        listMode = false
        pageSize = PREVIEW_SIZE
        loadFirstPage(period)
    }

    fun loadList(period: TopTravelerPeriod = _uiState.value.period) {
        listMode = true
        pageSize = LIST_PAGE_SIZE
        loadFirstPage(period)
    }

    fun refresh() {
        if (listMode) loadList() else loadPreview()
    }

    fun loadMore() {
        val state = _uiState.value
        if (!listMode || state.isLoading || state.isLoadingMore || state.page + 1 >= state.totalPages) {
            return
        }
        val nextPage = state.page + 1
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingMore = true, errorMessage = null) }
            runCatching {
                userApiService.getTopTravelers(state.period, nextPage, pageSize)
            }.onSuccess { response ->
                _uiState.update {
                    it.copy(
                        items = it.items + response.data,
                        isLoadingMore = false,
                        page = response.pageNumber,
                        totalPages = response.totalPages
                    )
                }
            }.onFailure { throwable ->
                _uiState.update {
                    it.copy(
                        isLoadingMore = false,
                        errorMessage = throwable.userMessage("Không thể tải thêm người dùng")
                    )
                }
            }
        }
    }

    fun toggleFollow(traveler: TopTravelerResponse) {
        if (traveler.currentUser || traveler.id in _uiState.value.requestingFollowIds) return
        val wasFollowing = traveler.following
        _uiState.update {
            it.copy(
                items = it.items.updateFollowing(traveler.id, !wasFollowing),
                requestingFollowIds = it.requestingFollowIds + traveler.id,
                actionErrorMessage = null
            )
        }
        viewModelScope.launch {
            runCatching {
                if (wasFollowing) {
                    userApiService.unfollowUser(traveler.id)
                } else {
                    userApiService.followUser(traveler.id)
                }
            }.onFailure { throwable ->
                _uiState.update {
                    it.copy(
                        items = it.items.updateFollowing(traveler.id, wasFollowing),
                        actionErrorMessage = throwable.userMessage("Không thể cập nhật trạng thái theo dõi")
                    )
                }
            }
            _uiState.update {
                it.copy(requestingFollowIds = it.requestingFollowIds - traveler.id)
            }
        }
    }

    private fun loadFirstPage(period: TopTravelerPeriod) {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    period = period,
                    items = emptyList(),
                    isLoading = true,
                    isLoadingMore = false,
                    errorMessage = null,
                    actionErrorMessage = null,
                    page = 0,
                    totalPages = 0
                )
            }
            runCatching {
                userApiService.getTopTravelers(period, page = 0, pageSize = pageSize)
            }.onSuccess { response ->
                _uiState.update {
                    it.copy(
                        items = response.data,
                        isLoading = false,
                        page = response.pageNumber,
                        totalPages = response.totalPages
                    )
                }
            }.onFailure { throwable ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = throwable.userMessage("Không thể tải danh sách người dùng nổi bật")
                    )
                }
            }
        }
    }

    private fun List<TopTravelerResponse>.updateFollowing(
        userId: Long,
        following: Boolean
    ): List<TopTravelerResponse> = map { item ->
        if (item.id == userId) item.copy(following = following) else item
    }

    private companion object {
        const val PREVIEW_SIZE = 4
        const val LIST_PAGE_SIZE = 20
    }
}
