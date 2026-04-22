package com.mobile.travelhub.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mobile.travelhub.data.model.FeedPostResponse
import com.mobile.travelhub.data.model.PostCommentResponse
import com.mobile.travelhub.usecase.AddCommentUseCase
import com.mobile.travelhub.usecase.GetAllPostsUseCase
import com.mobile.travelhub.usecase.LikePostUseCase
import com.mobile.travelhub.usecase.UnlikePostUseCase
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
    val username: String,
    val subtitle: String,
    val description: String,
    val imageUrls: List<String>,
    val likeCount: Int,
    val commentCount: Int,
    val isLiked: Boolean,
    val isLikeLoading: Boolean,
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
    val isCommentSubmitting: Boolean = false,
    val commentErrorMessage: String? = null,
    val commentsByPostId: Map<Long, List<HomeCommentUiModel>> = emptyMap()
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getAllPostsUseCase: GetAllPostsUseCase,
    private val likePostUseCase: LikePostUseCase,
    private val unlikePostUseCase: UnlikePostUseCase,
    private val addCommentUseCase: AddCommentUseCase
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

        updatePost(postId) { it.copy(isLikeLoading = true) }

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
                    updatePost(postId) { it.copy(isLikeLoading = false) }
                    _uiState.update {
                        it.copy(errorMessage = throwable.message ?: "Failed to update like")
                    }
                }
        }
    }

    fun onCommentClicked(postId: Long) {
        _uiState.update {
            it.copy(
                activeCommentPostId = postId,
                commentInput = "",
                commentErrorMessage = null
            )
        }
    }

    fun onCommentDismissed() {
        _uiState.update {
            it.copy(
                activeCommentPostId = null,
                commentInput = "",
                isCommentSubmitting = false,
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
            username = safeUsername,
            subtitle = safeLocation,
            description = safeDescription,
            imageUrls = safeImageUrls,
            likeCount = post.likeCount?.coerceAtLeast(0) ?: 0,
            commentCount = post.commentCount?.coerceAtLeast(0) ?: 0,
            isLiked = post.likedByCurrentUser == true,
            isLikeLoading = false,
            timeAgoLabel = formatTimeAgo(safeCreatedAt)
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
            id = "${createdAt.orEmpty()}-${username}-${content.hashCode()}",
            username = username,
            content = content,
            timeAgoLabel = formatTimeAgo(createdAt)
        )
    }

    private fun formatTimeAgo(rawTimestamp: String?): String {
        if (rawTimestamp.isNullOrBlank()) return "JUST NOW"

        val createdAtMillis = parseTimestampToMillis(rawTimestamp) ?: return "JUST NOW"
        val durationMillis = (System.currentTimeMillis() - createdAtMillis).coerceAtLeast(0L)

        val minutes = durationMillis / 60_000
        return when {
            minutes < 1 -> "JUST NOW"
            minutes < 60 -> "$minutes MINUTES AGO"
            minutes < 60 * 24 -> "${minutes / 60} HOURS AGO"
            else -> "${minutes / (60 * 24)} DAYS AGO"
        }
    }

    private fun parseTimestampToMillis(value: String): Long? {
        val formats = listOf(
            "yyyy-MM-dd'T'HH:mm:ss.SSSSSSX",
            "yyyy-MM-dd'T'HH:mm:ss.SSSSSS'Z'",
            "yyyy-MM-dd'T'HH:mm:ss.SSSSSS",
            "yyyy-MM-dd'T'HH:mm:ss.SSSX",
            "yyyy-MM-dd'T'HH:mm:ss.SSS",
            "yyyy-MM-dd'T'HH:mm:ssX",
            "yyyy-MM-dd'T'HH:mm:ss",
            "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
            "yyyy-MM-dd'T'HH:mm:ss'Z'",
            "yyyy-MM-dd HH:mm:ss"
        )

        for (pattern in formats) {
            val parser = SimpleDateFormat(pattern, Locale.US).apply {
                timeZone = TimeZone.getTimeZone("UTC")
                isLenient = false
            }
            val parsed = runCatching { parser.parse(value) as Date? }.getOrNull()
            if (parsed != null) {
                return parsed.time
            }
        }

        return null
    }
}
