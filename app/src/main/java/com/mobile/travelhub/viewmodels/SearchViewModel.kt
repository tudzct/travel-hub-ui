package com.mobile.travelhub.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mobile.travelhub.data.AuthRepository
import com.mobile.travelhub.data.api.PostApiService
import com.mobile.travelhub.data.api.UserApiService
import com.mobile.travelhub.data.model.FeedPostResponse
import com.mobile.travelhub.data.model.UserProfileResponse
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SearchUiState(
    val query: String = "",
    val posts: List<FeedPostResponse> = emptyList(),
    val users: List<UserProfileResponse> = emptyList(),
    val followingRequestUserIds: Set<Long> = emptySet(),
    val isLoadingPosts: Boolean = false,
    val isLoadingUsers: Boolean = false,
    val postsErrorMessage: String? = null,
    val usersErrorMessage: String? = null
)

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val postApiService: PostApiService,
    private val userApiService: UserApiService
) : ViewModel() {

    private val _uiState = MutableStateFlow(SearchUiState())
    val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()
    private var searchJob: Job? = null
    private var searchRequestId: Int = 0
    private val sessionUserId: Long
        get() = authRepository.getSavedSession()?.userId?.toLong() ?: -1L

    fun updateQuery(query: String) {
        if (query.isBlank()) {
            _uiState.update { it.copy(query = query) }
            searchJob?.cancel()
            searchRequestId += 1
            _uiState.update {
                it.copy(
                    posts = emptyList(),
                    users = emptyList(),
                    followingRequestUserIds = emptySet(),
                    isLoadingPosts = false,
                    isLoadingUsers = false,
                    postsErrorMessage = null,
                    usersErrorMessage = null
                )
            }
        } else {
            _uiState.update {
                it.copy(
                    query = query,
                    isLoadingPosts = true,
                    isLoadingUsers = true,
                    postsErrorMessage = null,
                    usersErrorMessage = null
                )
            }
        }
    }

    fun search(query: String) {
        val trimmedQuery = query.trim()
        if (trimmedQuery.isBlank()) {
            updateQuery("")
            return
        }

        searchJob?.cancel()
        val requestId = ++searchRequestId
        searchJob = viewModelScope.launch {
            _uiState.update {
                it.copy(
                    query = query,
                    isLoadingPosts = true,
                    isLoadingUsers = true,
                    postsErrorMessage = null,
                    usersErrorMessage = null
                )
            }

            val postsResult = async {
                runCatching {
                    postApiService.searchPosts(
                        description = trimmedQuery,
                        page = 0,
                        pageSize = 20
                    ).data
                }
            }
            val usersResult = async {
                runCatching {
                    userApiService.searchUsers(
                        username = trimmedQuery,
                        page = 0,
                        pageSize = 20
                    ).data
                }
            }

            val postsSearchResult = postsResult.await()
            val usersSearchResult = usersResult.await()

            if (requestId != searchRequestId) return@launch

            postsSearchResult
                .onSuccess { posts ->
                    _uiState.update {
                        it.copy(
                            posts = posts,
                            isLoadingPosts = false,
                            postsErrorMessage = null
                        )
                    }
                }
                .onFailure { throwable ->
                    _uiState.update {
                        it.copy(
                            posts = emptyList(),
                            isLoadingPosts = false,
                            postsErrorMessage = throwable.message ?: "Failed to search posts"
                        )
                    }
                }

            usersSearchResult
                .onSuccess { users ->
                    _uiState.update {
                        it.copy(
                            users = users,
                            isLoadingUsers = false,
                            usersErrorMessage = null
                        )
                    }
                }
                .onFailure { throwable ->
                    _uiState.update {
                        it.copy(
                            users = emptyList(),
                            isLoadingUsers = false,
                            usersErrorMessage = throwable.message ?: "Failed to search users"
                        )
                    }
                }
        }
    }

    fun toggleFollow(user: UserProfileResponse) {
        if (user.id <= 0L || user.id == sessionUserId) return
        if (user.id in _uiState.value.followingRequestUserIds) return

        val wasFollowing = user.isFollowing
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    followingRequestUserIds = it.followingRequestUserIds + user.id
                )
            }
            updateSearchUserFollowState(user.id, isFollowing = !wasFollowing)

            runCatching {
                if (wasFollowing) {
                    userApiService.unfollowUser(user.id)
                } else {
                    userApiService.followUser(user.id)
                }
            }.onFailure {
                updateSearchUserFollowState(user.id, isFollowing = wasFollowing)
            }

            _uiState.update {
                it.copy(followingRequestUserIds = it.followingRequestUserIds - user.id)
            }
        }
    }

    private fun updateSearchUserFollowState(userId: Long, isFollowing: Boolean) {
        _uiState.update { state ->
            state.copy(
                users = state.users.map { user ->
                    if (user.id != userId) {
                        user
                    } else {
                        val followerDelta = when {
                            isFollowing && !user.isFollowing -> 1
                            !isFollowing && user.isFollowing -> -1
                            else -> 0
                        }
                        user.copy(
                            isFollowing = isFollowing,
                            followersCount = (user.followersCount + followerDelta).coerceAtLeast(0)
                        )
                    }
                }
            )
        }
    }
}
