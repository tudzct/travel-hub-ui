package com.mobile.travelhub.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mobile.travelhub.data.userMessage
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
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

private const val MIN_RELOAD_LOADING_MS = 500L

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
    val saveCount: Int,
    val isLiked: Boolean,
    val isLikeLoading: Boolean,
    val isSaved: Boolean,
    val isSaveLoading: Boolean,
    val timeAgoLabel: String
)

data class HomeCommentUiModel(
    val id: String,
    val ownerId: Long,
    val username: String,
    val avatarUrl: String?,
    val content: String,
    val timeAgoLabel: String
)

data class HomeUiState(
    val isLoading: Boolean = false,
    val isLoadingMore: Boolean = false,
    val posts: List<HomePostUiModel> = emptyList(),
    val errorMessage: String? = null,
    val loadMoreErrorMessage: String? = null,
    val page: Int = 0,
    val totalPages: Int = 0,
    val activeCommentPostId: Long? = null,
    val commentInput: String = "",
    val isCommentsLoading: Boolean = false,
    val isCommentSubmitting: Boolean = false,
    val commentsErrorMessage: String? = null,
    val commentErrorMessage: String? = null,
    val commentsByPostId: Map<Long, List<HomeCommentUiModel>> = emptyMap()
)

@Singleton
class HomeFeedMemoryCache @Inject constructor() {
    private var state: HomeUiState? = null

    fun get(): HomeUiState? = state

    fun set(value: HomeUiState) {
        state = value.copy(
            isLoading = false,
            isLoadingMore = false,
            activeCommentPostId = null,
            commentInput = "",
            isCommentsLoading = false,
            isCommentSubmitting = false,
            commentsErrorMessage = null,
            commentErrorMessage = null
        )
    }
}

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getAllPostsUseCase: GetAllPostsUseCase,
    private val likePostUseCase: LikePostUseCase,
    private val unlikePostUseCase: UnlikePostUseCase,
    private val savePostUseCase: SavePostUseCase,
    private val addCommentUseCase: AddCommentUseCase,
    private val getPostCommentsUseCase: GetPostCommentsUseCase,
    private val homeFeedMemoryCache: HomeFeedMemoryCache
) : ViewModel() {

    private val cachedInitialState = homeFeedMemoryCache.get()
    private val _uiState = MutableStateFlow(cachedInitialState ?: HomeUiState(isLoading = true))
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()
    private var feedGeneration = 0

    init {
        if (cachedInitialState == null) {
            refreshPosts()
        }
    }

    fun refreshPosts() {
        val generation = ++feedGeneration
        viewModelScope.launch {
            val loadingStartedAt = System.currentTimeMillis()
            updateState {
                it.copy(
                    isLoading = true,
                    isLoadingMore = false,
                    errorMessage = null,
                    loadMoreErrorMessage = null,
                    page = 0,
                    totalPages = 0
                )
            }

            getAllPostsUseCase(page = 0, pageSize = POSTS_PAGE_SIZE)
                .onSuccess { response ->
                    if (generation != feedGeneration) return@onSuccess
                    val safePosts = response.data.mapNotNull { post ->
                        runCatching { toUiModel(post) }.getOrNull()
                    }
                    delayRemainingLoadingTime(loadingStartedAt)

                    updateState {
                        it.copy(
                            isLoading = false,
                            posts = safePosts,
                            errorMessage = null,
                            page = response.pageNumber,
                            totalPages = response.totalPages
                        )
                    }
                }
                .onFailure { throwable ->
                    if (generation != feedGeneration) return@onFailure
                    delayRemainingLoadingTime(loadingStartedAt)

                    updateState {
                        it.copy(
                            isLoading = false,
                            posts = emptyList(),
                            errorMessage = throwable.userMessage("Không thể tải bài viết")
                        )
                    }
                }
        }
    }

    fun loadMorePosts() {
        val state = _uiState.value
        if (
            state.isLoading ||
            state.isLoadingMore ||
            state.page + 1 >= state.totalPages
        ) {
            return
        }

        val nextPage = state.page + 1
        val generation = feedGeneration
        viewModelScope.launch {
            updateState {
                it.copy(
                    isLoadingMore = true,
                    loadMoreErrorMessage = null
                )
            }

            getAllPostsUseCase(page = nextPage, pageSize = POSTS_PAGE_SIZE)
                .onSuccess { response ->
                    if (generation != feedGeneration) return@onSuccess
                    val existingIds = _uiState.value.posts.asSequence()
                        .map { it.id }
                        .toHashSet()
                    val newPosts = response.data
                        .asSequence()
                        .filter { it.id !in existingIds }
                        .mapNotNull { post -> runCatching { toUiModel(post) }.getOrNull() }
                        .toList()

                    updateState {
                        it.copy(
                            posts = it.posts + newPosts,
                            isLoadingMore = false,
                            loadMoreErrorMessage = null,
                            page = response.pageNumber,
                            totalPages = response.totalPages
                        )
                    }

                    if (newPosts.isEmpty() && response.pageNumber + 1 < response.totalPages) {
                        loadMorePosts()
                    }
                }
                .onFailure { throwable ->
                    if (generation != feedGeneration) return@onFailure
                    updateState {
                        it.copy(
                            isLoadingMore = false,
                            loadMoreErrorMessage = throwable.userMessage("Không thể tải thêm bài viết")
                        )
                    }
                }
        }
    }

    private suspend fun delayRemainingLoadingTime(loadingStartedAt: Long) {
        val remainingMillis = MIN_RELOAD_LOADING_MS - (System.currentTimeMillis() - loadingStartedAt)
        if (remainingMillis > 0) {
            delay(remainingMillis)
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
                    updateState {
                        it.copy(errorMessage = throwable.userMessage("Không thể cập nhật lượt thích"))
                    }
                }
        }
    }

    fun onSaveClicked(postId: Long) {
        val currentPost = _uiState.value.posts.firstOrNull { it.id == postId } ?: return
        if (currentPost.isSaveLoading) return
        val targetSaved = !currentPost.isSaved
        val targetSaveCount = (currentPost.saveCount + if (targetSaved) 1 else -1).coerceAtLeast(0)

        updatePost(postId) {
            it.copy(
                isSaved = targetSaved,
                saveCount = targetSaveCount,
                isSaveLoading = true
            )
        }

        viewModelScope.launch {
            savePostUseCase(postId, currentlySaved = currentPost.isSaved)
                .onSuccess { response ->
                    updatePost(postId) {
                        it.copy(
                            isSaved = response.saved,
                            saveCount = response.saveCount.coerceAtLeast(0),
                            isSaveLoading = false
                        )
                    }
                }
                .onFailure { throwable ->
                    updatePost(postId) {
                        it.copy(
                            isSaved = currentPost.isSaved,
                            saveCount = currentPost.saveCount,
                            isSaveLoading = false
                        )
                    }
                    updateState {
                        it.copy(errorMessage = throwable.userMessage("Không thể lưu bài viết"))
                    }
                }
        }
    }

    fun onCommentClicked(postId: Long) {
        updateState {
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
        updateState {
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
        updateState {
            it.copy(commentInput = value, commentErrorMessage = null)
        }
    }

    fun submitComment() {
        val currentState = _uiState.value
        val postId = currentState.activeCommentPostId ?: return
        val content = currentState.commentInput.trim()

        if (content.isBlank()) {
            updateState { it.copy(commentErrorMessage = "Vui lòng nhập bình luận") }
            return
        }

        if (currentState.isCommentSubmitting) return

        updateState {
            it.copy(isCommentSubmitting = true, commentErrorMessage = null)
        }

        viewModelScope.launch {
            addCommentUseCase(postId = postId, content = content)
                .onSuccess { response ->
                    val commentUiModel = toCommentUiModel(response)
                    updateState { state ->
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
                    updateState {
                        it.copy(
                            isCommentSubmitting = false,
                            commentErrorMessage = throwable.userMessage("Không thể thêm bình luận")
                        )
                    }
                }
        }
    }

    private fun loadComments(postId: Long) {
        viewModelScope.launch {
            getPostCommentsUseCase(postId = postId, page = 0, pageSize = 50)
                .onSuccess { response ->
                    updateState { state ->
                        state.copy(
                            isCommentsLoading = false,
                            commentsErrorMessage = null,
                            commentsByPostId = state.commentsByPostId + (
                                postId to response.data.map(::toCommentUiModel)
                            ),
                            posts = state.posts.map { post ->
                                if (post.id == postId) {
                                    post.copy(commentCount = response.totalElements.toSafeCount())
                                } else {
                                    post
                                }
                            }
                        )
                    }
                }
                .onFailure { throwable ->
                    updateState {
                        it.copy(
                            isCommentsLoading = false,
                            commentsErrorMessage = throwable.userMessage("Không thể tải bình luận")
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
            saveCount = post.saveCount?.coerceAtLeast(0) ?: 0,
            isLiked = post.likedByCurrentUser == true,
            isLikeLoading = false,
            isSaved = post.savedByCurrentUser == true,
            isSaveLoading = false,
            timeAgoLabel = PostsUtils.formatTimeAgo(safeCreatedAt)
        )
    }

    private fun updateState(transform: (HomeUiState) -> HomeUiState) {
        _uiState.update { current ->
            transform(current).also(homeFeedMemoryCache::set)
        }
    }

    private fun updatePost(postId: Long, transform: (HomePostUiModel) -> HomePostUiModel) {
        updateState { state ->
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
            ownerId = response.owner?.id ?: 0L,
            username = username,
            avatarUrl = response.owner?.avatarUrl?.takeIf { it.isNotBlank() },
            content = content,
            timeAgoLabel = PostsUtils.formatTimeAgo(createdAt)
        )
    }

    private fun Long.toSafeCount(): Int = coerceIn(0L, Int.MAX_VALUE.toLong()).toInt()

    private companion object {
        const val POSTS_PAGE_SIZE = 10
    }
}
