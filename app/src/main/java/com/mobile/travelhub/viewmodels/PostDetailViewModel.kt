package com.mobile.travelhub.viewmodels

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mobile.travelhub.data.model.FeedPostResponse
import com.mobile.travelhub.data.model.PostCommentResponse
import com.mobile.travelhub.usecase.AddCommentUseCase
import com.mobile.travelhub.usecase.GetPostByIdUseCase
import com.mobile.travelhub.usecase.GetPostCommentsUseCase
import com.mobile.travelhub.usecase.LikePostUseCase
import com.mobile.travelhub.usecase.SavePostUseCase
import com.mobile.travelhub.usecase.UnlikePostUseCase
import com.mobile.travelhub.utils.PostsUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class PostDetailUiState(
    val isLoading: Boolean = false,
    val post: HomePostUiModel? = null,
    val errorMessage: String? = null,
    val isCommentSheetVisible: Boolean = false,
    val commentInput: String = "",
    val isCommentsLoading: Boolean = false,
    val isCommentSubmitting: Boolean = false,
    val commentsErrorMessage: String? = null,
    val commentErrorMessage: String? = null,
    val comments: List<HomeCommentUiModel> = emptyList()
)

@HiltViewModel
class PostDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getPostByIdUseCase: GetPostByIdUseCase,
    private val likePostUseCase: LikePostUseCase,
    private val unlikePostUseCase: UnlikePostUseCase,
    private val savePostUseCase: SavePostUseCase,
    private val addCommentUseCase: AddCommentUseCase,
    private val getPostCommentsUseCase: GetPostCommentsUseCase
) : ViewModel() {
    private val postId: Long = checkNotNull(savedStateHandle["postId"])

    private val _uiState = MutableStateFlow(PostDetailUiState(isLoading = true))
    val uiState: StateFlow<PostDetailUiState> = _uiState.asStateFlow()

    init {
        refreshPost()
    }

    fun refreshPost() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            getPostByIdUseCase(postId)
                .onSuccess { response ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            post = toPostUiModel(response),
                            errorMessage = null
                        )
                    }
                }
                .onFailure { throwable ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            post = null,
                            errorMessage = throwable.message ?: "Failed to load post"
                        )
                    }
                }
        }
    }

    fun onLikeClicked() {
        val currentPost = _uiState.value.post ?: return
        if (currentPost.isLikeLoading) return

        val nextLiked = !currentPost.isLiked
        val nextLikeCount = (currentPost.likeCount + if (nextLiked) 1 else -1).coerceAtLeast(0)
        updatePost {
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
                    updatePost {
                        it.copy(
                            isLiked = response.liked,
                            likeCount = response.likeCount.coerceAtLeast(0),
                            isLikeLoading = false
                        )
                    }
                }
                .onFailure { throwable ->
                    updatePost {
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

    fun onSaveClicked() {
        val currentPost = _uiState.value.post ?: return
        if (currentPost.isSaveLoading) return
        val targetSaved = !currentPost.isSaved

        updatePost {
            it.copy(
                isSaved = targetSaved,
                isSaveLoading = true
            )
        }

        viewModelScope.launch {
            savePostUseCase(postId, currentlySaved = currentPost.isSaved)
                .onSuccess { response ->
                    updatePost {
                        it.copy(
                            isSaved = response.saved,
                            isSaveLoading = false
                        )
                    }
                }
                .onFailure { throwable ->
                    updatePost {
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

    fun onCommentClicked() {
        _uiState.update {
            it.copy(
                isCommentSheetVisible = true,
                commentInput = "",
                isCommentsLoading = true,
                commentsErrorMessage = null,
                commentErrorMessage = null
            )
        }
        loadComments()
    }

    fun onCommentDismissed() {
        _uiState.update {
            it.copy(
                isCommentSheetVisible = false,
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
        val content = currentState.commentInput.trim()

        if (content.isBlank()) {
            _uiState.update { it.copy(commentErrorMessage = "Comment cannot be empty") }
            return
        }

        if (currentState.isCommentSubmitting) return

        _uiState.update { it.copy(isCommentSubmitting = true, commentErrorMessage = null) }

        viewModelScope.launch {
            addCommentUseCase(postId = postId, content = content)
                .onSuccess { response ->
                    val commentUiModel = toCommentUiModel(response)
                    _uiState.update { state ->
                        state.copy(
                            isCommentSubmitting = false,
                            commentInput = "",
                            commentsErrorMessage = null,
                            commentErrorMessage = null,
                            comments = state.comments + commentUiModel,
                            post = state.post?.copy(
                                commentCount = (state.post.commentCount + 1).coerceAtLeast(0)
                            )
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

    private fun loadComments() {
        viewModelScope.launch {
            getPostCommentsUseCase(postId = postId, page = 0, pageSize = 50)
                .onSuccess { response ->
                    _uiState.update {
                        it.copy(
                            isCommentsLoading = false,
                            commentsErrorMessage = null,
                            comments = response.data.map(::toCommentUiModel)
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

    private fun updatePost(transform: (HomePostUiModel) -> HomePostUiModel) {
        _uiState.update { state ->
            state.copy(post = state.post?.let(transform))
        }
    }

    private fun toPostUiModel(post: FeedPostResponse): HomePostUiModel {
        val username = post.owner.username.takeIf { it.isNotBlank() } ?: "unknown"
        val location = post.location?.takeIf { it.isNotBlank() } ?: "STUDIO NULL"
        val createdAt = post.createdAt ?: post.updatedAt

        return HomePostUiModel(
            id = post.id,
            ownerId = post.owner.id,
            username = username,
            ownerAvatarUrl = post.owner.avatarUrl?.takeIf { it.isNotBlank() },
            subtitle = location,
            description = post.description.takeIf { it.isNotBlank() } ?: "",
            imageUrls = post.imageUrls.filter { it.isNotBlank() },
            likeCount = post.likeCount?.coerceAtLeast(0) ?: 0,
            commentCount = post.commentCount?.coerceAtLeast(0) ?: 0,
            isLiked = post.likedByCurrentUser == true,
            isLikeLoading = false,
            isSaved = post.savedByCurrentUser == true,
            isSaveLoading = false,
            timeAgoLabel = PostsUtils.formatTimeAgo(createdAt)
        )
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
