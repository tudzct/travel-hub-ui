package com.mobile.travelhub.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mobile.travelhub.data.AuthRepository
import com.mobile.travelhub.data.api.PostApiService
import com.mobile.travelhub.data.api.UserApiService
import com.mobile.travelhub.data.model.FeedPostResponse
import com.mobile.travelhub.data.model.PostCommentResponse
import com.mobile.travelhub.data.model.UserProfileResponse
import com.mobile.travelhub.usecase.AddCommentUseCase
import com.mobile.travelhub.usecase.GetPostCommentsUseCase
import com.mobile.travelhub.usecase.LikePostUseCase
import com.mobile.travelhub.usecase.UnlikePostUseCase
import com.mobile.travelhub.utils.PostsUtils
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
    val likingPostIds: Set<Long> = emptySet(),
    val isLoadingPosts: Boolean = false,
    val isLoadingUsers: Boolean = false,
    val postsErrorMessage: String? = null,
    val usersErrorMessage: String? = null,
    val activeCommentPostId: Long? = null,
    val commentInput: String = "",
    val isCommentsLoading: Boolean = false,
    val isCommentSubmitting: Boolean = false,
    val commentsErrorMessage: String? = null,
    val commentErrorMessage: String? = null,
    val commentsByPostId: Map<Long, List<HomeCommentUiModel>> = emptyMap()
)

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val postApiService: PostApiService,
    private val userApiService: UserApiService,
    private val likePostUseCase: LikePostUseCase,
    private val unlikePostUseCase: UnlikePostUseCase,
    private val addCommentUseCase: AddCommentUseCase,
    private val getPostCommentsUseCase: GetPostCommentsUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(SearchUiState())
    val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()
    private var searchJob: Job? = null
    private var searchRequestId: Int = 0
    private var lastLoadedQuery: String? = null
    private val sessionUserId: Long
        get() = authRepository.getSavedSession()?.userId?.toLong() ?: -1L

    fun updateQuery(query: String) {
        if (query.isBlank()) {
            _uiState.update { it.copy(query = query) }
            searchJob?.cancel()
            searchRequestId += 1
            lastLoadedQuery = null
            _uiState.update {
                it.copy(
                    posts = emptyList(),
                    users = emptyList(),
                    followingRequestUserIds = emptySet(),
                    likingPostIds = emptySet(),
                    isLoadingPosts = false,
                    isLoadingUsers = false,
                    postsErrorMessage = null,
                    usersErrorMessage = null,
                    activeCommentPostId = null,
                    commentInput = "",
                    isCommentsLoading = false,
                    isCommentSubmitting = false,
                    commentsErrorMessage = null,
                    commentErrorMessage = null,
                    commentsByPostId = emptyMap()
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
        if (hasLoadedSearchResults(trimmedQuery)) return

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

            if (
                postsSearchResult.isSuccess &&
                usersSearchResult.isSuccess &&
                requestId == searchRequestId
            ) {
                lastLoadedQuery = trimmedQuery
            }
        }
    }

    private fun hasLoadedSearchResults(query: String): Boolean {
        val state = _uiState.value
        return lastLoadedQuery == query &&
            state.query.trim() == query &&
            !state.isLoadingPosts &&
            !state.isLoadingUsers &&
            state.postsErrorMessage == null &&
            state.usersErrorMessage == null
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

    fun onLikeClicked(postId: Long) {
        val currentPost = _uiState.value.posts.firstOrNull { it.id == postId } ?: return
        if (postId in _uiState.value.likingPostIds) return

        val wasLiked = currentPost.likedByCurrentUser == true
        viewModelScope.launch {
            _uiState.update { it.copy(likingPostIds = it.likingPostIds + postId) }

            val result = if (wasLiked) {
                unlikePostUseCase(postId)
            } else {
                likePostUseCase(postId)
            }

            result
                .onSuccess { response ->
                    updatePost(postId) { post ->
                        post.copy(
                            likedByCurrentUser = response.liked,
                            likeCount = response.likeCount.coerceAtLeast(0)
                        )
                    }
                }
                .onFailure { throwable ->
                    _uiState.update {
                        it.copy(postsErrorMessage = throwable.message ?: "Failed to update like")
                    }
                }

            _uiState.update { it.copy(likingPostIds = it.likingPostIds - postId) }
        }
    }

    fun onCommentClicked(postId: Long) {
        _uiState.update {
            it.copy(
                activeCommentPostId = postId,
                commentInput = "",
                isCommentsLoading = true,
                commentsErrorMessage = null,
                commentErrorMessage = null
            )
        }
        loadComments(postId)
    }

    fun onCommentDismissed() {
        _uiState.update {
            it.copy(
                activeCommentPostId = null,
                commentInput = "",
                isCommentsLoading = false,
                isCommentSubmitting = false,
                commentsErrorMessage = null,
                commentErrorMessage = null
            )
        }
    }

    fun onCommentInputChanged(value: String) {
        _uiState.update { it.copy(commentInput = value, commentErrorMessage = null) }
    }

    fun submitComment() {
        val currentState = _uiState.value
        val postId = currentState.activeCommentPostId ?: return
        val content = currentState.commentInput.trim()

        if (content.isBlank()) {
            _uiState.update { it.copy(commentErrorMessage = "Comment cannot be empty") }
            return
        }
        if (currentState.isCommentSubmitting) return

        viewModelScope.launch {
            _uiState.update { it.copy(isCommentSubmitting = true, commentErrorMessage = null) }

            addCommentUseCase(postId = postId, content = content)
                .onSuccess { response ->
                    val commentUiModel = toCommentUiModel(response)
                    _uiState.update { state ->
                        val currentComments = state.commentsByPostId[postId].orEmpty()
                        state.copy(
                            isCommentSubmitting = false,
                            commentInput = "",
                            commentsErrorMessage = null,
                            commentErrorMessage = null,
                            commentsByPostId = state.commentsByPostId + (postId to (currentComments + commentUiModel)),
                            posts = state.posts.map { post ->
                                if (post.id == postId) {
                                    post.copy(commentCount = ((post.commentCount ?: 0) + 1).coerceAtLeast(0))
                                } else {
                                    post
                                }
                            }
                        )
                    }
                }
                .onFailure { throwable ->
                    _uiState.update {
                        it.copy(
                            isCommentSubmitting = false,
                            commentErrorMessage = throwable.message ?: "Failed to add comment"
                        )
                    }
                }
        }
    }

    private fun loadComments(postId: Long) {
        viewModelScope.launch {
            getPostCommentsUseCase(postId = postId, page = 0, pageSize = 50)
                .onSuccess { response ->
                    _uiState.update { state ->
                        state.copy(
                            isCommentsLoading = false,
                            commentsErrorMessage = null,
                            commentsByPostId = state.commentsByPostId + (
                                postId to response.data.map(::toCommentUiModel)
                            )
                        )
                    }
                }
                .onFailure { throwable ->
                    _uiState.update {
                        it.copy(
                            isCommentsLoading = false,
                            commentsErrorMessage = throwable.message ?: "Failed to load comments"
                        )
                    }
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

    private fun updatePost(postId: Long, transform: (FeedPostResponse) -> FeedPostResponse) {
        _uiState.update { state ->
            state.copy(
                posts = state.posts.map { post ->
                    if (post.id == postId) transform(post) else post
                }
            )
        }
    }

    private fun toCommentUiModel(response: PostCommentResponse): HomeCommentUiModel {
        val username = response.owner?.username
            ?.takeIf { it.isNotBlank() }
            ?: "unknown"
        val content = response.content.trim()
        val createdAt = response.createdAt ?: response.updatedAt

        return HomeCommentUiModel(
            id = response.id?.toString() ?: "${createdAt.orEmpty()}-${username}-${content.hashCode()}",
            username = username,
            content = content,
            timeAgoLabel = PostsUtils.formatTimeAgo(createdAt)
        )
    }
}
