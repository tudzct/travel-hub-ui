package com.mobile.travelhub.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mobile.travelhub.data.model.FeedPostResponse
import com.mobile.travelhub.data.model.PostCommentResponse
import com.mobile.travelhub.usecase.AddCommentUseCase
import com.mobile.travelhub.usecase.GetAllPostsUseCase
import com.mobile.travelhub.usecase.GetPostCommentsUseCase
import com.mobile.travelhub.usecase.LikePostUseCase
import com.mobile.travelhub.usecase.SavePostUseCase
import com.mobile.travelhub.usecase.UnlikePostUseCase
import com.mobile.travelhub.utils.PostsUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class HomePostUiModel(
    val id: Long,
    val ownerId: Long,
    val username: String,
    val ownerAvatarUrl: String?,
    val subtitle: String,
    val description: String,
    val imageUrls: List<String>,
    val likeCount: Int,
    val commentCount: Int,
    val isLiked: Boolean,
    val isLikeLoading: Boolean,
    val isSaved: Boolean,
    val isSaveLoading: Boolean,
    val timeAgoLabel: String
)

data class HomeCommentUiModel(
    val id: String,
    val username: String,
    val content: String,
    val timeAgoLabel: String
)

data class HomeUiState(
    val isLoading: Boolean = false,
    val posts: List<HomePostUiModel> = emptyList(),
    val errorMessage: String? = null,
    val activeCommentPostId: Long? = null,
    val commentInput: String = "",
    val isCommentsLoading: Boolean = false,
    val isCommentSubmitting: Boolean = false,
    val commentsErrorMessage: String? = null,
    val commentErrorMessage: String? = null,
    val commentsByPostId: Map<Long, List<HomeCommentUiModel>> = emptyMap()
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getAllPostsUseCase: GetAllPostsUseCase,
    private val likePostUseCase: LikePostUseCase,
    private val unlikePostUseCase: UnlikePostUseCase,
    private val savePostUseCase: SavePostUseCase,
    private val addCommentUseCase: AddCommentUseCase,
    private val getPostCommentsUseCase: GetPostCommentsUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState(isLoading = true))
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        refreshPosts()
    }

    fun refreshPosts() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            getAllPostsUseCase(page = 0, pageSize = 20)
                .onSuccess { posts ->
                    val safePosts = posts.mapNotNull { post ->
                        runCatching { toUiModel(post) }.getOrNull()
                    }

                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            posts = safePosts,
                            errorMessage = null
                        )
                    }
                }
                .onFailure { throwable ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            posts = emptyList(),
                            errorMessage = throwable.message ?: "Failed to load posts"
                        )
                    }
                }
        }
    }

    fun onLikeClicked(postId: Long) {
        val currentPost = _uiState.value.posts.firstOrNull { it.id == postId } ?: return
        if (currentPost.isLikeLoading) return

        val nextLiked = !currentPost.isLiked
        val nextLikeCount = (currentPost.likeCount + if (nextLiked) 1 else -1).coerceAtLeast(0)
        updatePost(postId) {
            it.copy(
                isLiked = nextLiked,
                likeCount = nextLikeCount,
                isLikeLoading = true
            )
        }

        viewModelScope.launch {
            val result = if (currentPost.isLiked) {
                unlikePostUseCase(postId)
            } else {
                likePostUseCase(postId)
            }

            result
                .onSuccess { response ->
                    updatePost(postId) {
                        it.copy(
                            isLiked = response.liked,
                            likeCount = response.likeCount.coerceAtLeast(0),
                            isLikeLoading = false
                        )
                    }
                }
                .onFailure { throwable ->
                    updatePost(postId) {
                        it.copy(
                            isLiked = currentPost.isLiked,
                            likeCount = currentPost.likeCount,
                            isLikeLoading = false
                        )
                    }
                    _uiState.update {
                        it.copy(errorMessage = throwable.message ?: "Failed to update like")
                    }
                }
        }
    }

    fun onSaveClicked(postId: Long) {
        val currentPost = _uiState.value.posts.firstOrNull { it.id == postId } ?: return
        if (currentPost.isSaveLoading || currentPost.isSaved) return

        updatePost(postId) {
            it.copy(
                isSaved = true,
                isSaveLoading = true
            )
        }

        viewModelScope.launch {
            savePostUseCase(postId)
                .onSuccess { response ->
                    updatePost(postId) {
                        it.copy(
                            isSaved = response.saved,
                            isSaveLoading = false
                        )
                    }
                }
                .onFailure { throwable ->
                    updatePost(postId) {
                        it.copy(
                            isSaved = currentPost.isSaved,
                            isSaveLoading = false
                        )
                    }
                    _uiState.update {
                        it.copy(errorMessage = throwable.message ?: "Failed to save post")
                    }
                }
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
        _uiState.update {
            it.copy(commentInput = value, commentErrorMessage = null)
        }
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

        _uiState.update {
            it.copy(isCommentSubmitting = true, commentErrorMessage = null)
        }

        viewModelScope.launch {
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
                                    post.copy(commentCount = (post.commentCount + 1).coerceAtLeast(0))
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

    private fun toUiModel(post: FeedPostResponse): HomePostUiModel {
        val safeId = runCatching { post.id }.getOrDefault(0L)
        val safeUsername = runCatching { post.owner.username }
            .getOrNull()
            ?.takeIf { it.isNotBlank() }
            ?: "unknown"
        val safeDescription = runCatching { post.description }
            .getOrNull()
            ?.takeIf { it.isNotBlank() }
            ?: ""
        val safeLocation = runCatching { post.location }
            .getOrNull()
            ?.takeIf { it.isNotBlank() }
            ?: "STUDIO NULL"
        val safeImageUrls = runCatching { post.imageUrls }
            .getOrNull()
            ?.filter { it.isNotBlank() }
            ?: emptyList()
        val safeCreatedAt = runCatching { post.createdAt }.getOrNull()
            ?: runCatching { post.updatedAt }.getOrNull()

        return HomePostUiModel(
            id = safeId,
            ownerId = runCatching { post.owner.id }.getOrDefault(0L),
            username = safeUsername,
            ownerAvatarUrl = runCatching { post.owner.avatarUrl }.getOrNull()?.takeIf { it.isNotBlank() },
            subtitle = safeLocation,
            description = safeDescription,
            imageUrls = safeImageUrls,
            likeCount = post.likeCount?.coerceAtLeast(0) ?: 0,
            commentCount = post.commentCount?.coerceAtLeast(0) ?: 0,
            isLiked = post.likedByCurrentUser == true,
            isLikeLoading = false,
            isSaved = post.savedByCurrentUser == true,
            isSaveLoading = false,
            timeAgoLabel = PostsUtils.formatTimeAgo(safeCreatedAt)
        )
    }

    private fun updatePost(postId: Long, transform: (HomePostUiModel) -> HomePostUiModel) {
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
