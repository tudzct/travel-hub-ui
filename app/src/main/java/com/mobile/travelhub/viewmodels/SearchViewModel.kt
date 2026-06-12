package com.mobile.travelhub.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mobile.travelhub.data.AuthRepository
import com.mobile.travelhub.data.PostRepository
import com.mobile.travelhub.data.SearchHistoryRepository
import com.mobile.travelhub.data.userMessage
import com.mobile.travelhub.data.api.PlaceApiService
import com.mobile.travelhub.data.api.UserApiService
import com.mobile.travelhub.data.model.FeedPostResponse
import com.mobile.travelhub.data.model.PostCommentResponse
import com.mobile.travelhub.data.model.TravelPlaceListItemResponse
import com.mobile.travelhub.data.model.UserProfileResponse
import com.mobile.travelhub.usecase.AddCommentUseCase
import com.mobile.travelhub.usecase.GetPostCommentsUseCase
import com.mobile.travelhub.usecase.LikePostUseCase
import com.mobile.travelhub.usecase.SavePostUseCase
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
    val recentSearches: List<String> = emptyList(),
    val posts: List<FeedPostResponse> = emptyList(),
    val users: List<UserProfileResponse> = emptyList(),
    val places: List<TravelPlaceListItemResponse> = emptyList(),
    val followingRequestUserIds: Set<Long> = emptySet(),
    val likingPostIds: Set<Long> = emptySet(),
    val savingPostIds: Set<Long> = emptySet(),
    val isLoadingPosts: Boolean = false,
    val isLoadingUsers: Boolean = false,
    val isLoadingPlaces: Boolean = false,
    val isLoadingMorePlaces: Boolean = false,
    val postsErrorMessage: String? = null,
    val usersErrorMessage: String? = null,
    val placesErrorMessage: String? = null,
    val placesLoadMoreErrorMessage: String? = null,
    val placesPage: Int = 0,
    val placesTotalPages: Int = 0,
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
    private val searchHistoryRepository: SearchHistoryRepository,
    private val postRepository: PostRepository,
    private val userApiService: UserApiService,
    private val placeApiService: PlaceApiService,
    private val likePostUseCase: LikePostUseCase,
    private val unlikePostUseCase: UnlikePostUseCase,
    private val savePostUseCase: SavePostUseCase,
    private val addCommentUseCase: AddCommentUseCase,
    private val getPostCommentsUseCase: GetPostCommentsUseCase,
    private val postInteractionEventBus: PostInteractionEventBus
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        SearchUiState(recentSearches = searchHistoryRepository.recentSearches.value)
    )
    val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()
    private var searchJob: Job? = null
    private var searchRequestId: Int = 0
    private var lastLoadedQuery: String? = null
    private val sessionUserId: Long
        get() = authRepository.getSavedSession()?.userId?.toLong() ?: -1L

    init {
        collectPostInteractionEvents()
        searchHistoryRepository.refresh()
        viewModelScope.launch {
            searchHistoryRepository.recentSearches.collect { recentSearches ->
                _uiState.update { it.copy(recentSearches = recentSearches) }
            }
        }
    }

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
                    places = emptyList(),
                    followingRequestUserIds = emptySet(),
                    likingPostIds = emptySet(),
                    savingPostIds = emptySet(),
                    isLoadingPosts = false,
                    isLoadingUsers = false,
                    isLoadingPlaces = false,
                    isLoadingMorePlaces = false,
                    postsErrorMessage = null,
                    usersErrorMessage = null,
                    placesErrorMessage = null,
                    placesLoadMoreErrorMessage = null,
                    placesPage = 0,
                    placesTotalPages = 0,
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
                    isLoadingPlaces = true,
                    postsErrorMessage = null,
                    usersErrorMessage = null,
                    placesErrorMessage = null
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
                    isLoadingPlaces = true,
                    postsErrorMessage = null,
                    usersErrorMessage = null,
                    placesErrorMessage = null,
                    placesLoadMoreErrorMessage = null,
                    placesPage = 0,
                    placesTotalPages = 0
                )
            }

            val postsResult = async {
                runCatching {
                    postRepository.searchPosts(
                        description = trimmedQuery,
                        page = 0,
                        pageSize = 20
                    ).getOrThrow()
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
            val placesResult = async {
                runCatching {
                    placeApiService.searchPlaces(
                        query = trimmedQuery,
                        page = 0,
                        pageSize = PLACES_PAGE_SIZE
                    )
                }
            }

            val postsSearchResult = postsResult.await()
            val usersSearchResult = usersResult.await()
            val placesSearchResult = placesResult.await()

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
                            postsErrorMessage = throwable.userMessage("Không thể tìm kiếm bài viết")
                        )
                    }
                }

            usersSearchResult
                .onSuccess { users ->
                    val currentUserId = sessionUserId
                    _uiState.update {
                        it.copy(
                            users = users.filter { user -> user.id != currentUserId },
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
                            usersErrorMessage = throwable.userMessage("Không thể tìm kiếm người dùng")
                        )
                    }
                }

            placesSearchResult
                .onSuccess { places ->
                    _uiState.update {
                        it.copy(
                            places = places.data,
                            isLoadingPlaces = false,
                            placesErrorMessage = null,
                            placesLoadMoreErrorMessage = null,
                            placesPage = places.pageNumber,
                            placesTotalPages = places.totalPages
                        )
                    }
                }
                .onFailure { throwable ->
                    _uiState.update {
                        it.copy(
                            places = emptyList(),
                            isLoadingPlaces = false,
                            placesErrorMessage = throwable.userMessage("Không thể tìm kiếm địa điểm"),
                            placesLoadMoreErrorMessage = null,
                            placesPage = 0,
                            placesTotalPages = 0
                        )
                    }
                }

            if (
                postsSearchResult.isSuccess &&
                usersSearchResult.isSuccess &&
                placesSearchResult.isSuccess &&
                requestId == searchRequestId
            ) {
                lastLoadedQuery = trimmedQuery
                searchHistoryRepository.addRecentSearch(trimmedQuery)
            }
        }
    }

    fun applyRecentSearch(query: String) {
        val trimmedQuery = query.trim()
        if (trimmedQuery.isBlank()) return
        _uiState.update {
            it.copy(
                query = trimmedQuery,
                isLoadingPosts = true,
                isLoadingUsers = true,
                isLoadingPlaces = true,
                postsErrorMessage = null,
                usersErrorMessage = null,
                placesErrorMessage = null
            )
        }
        search(trimmedQuery)
    }

    fun loadMorePlaces() {
        val state = _uiState.value
        val trimmedQuery = state.query.trim()
        if (
            trimmedQuery.isBlank() ||
            state.isLoadingPlaces ||
            state.isLoadingMorePlaces ||
            state.placesPage + 1 >= state.placesTotalPages
        ) {
            return
        }

        val nextPage = state.placesPage + 1
        val requestId = searchRequestId
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isLoadingMorePlaces = true,
                    placesLoadMoreErrorMessage = null
                )
            }

            runCatching {
                placeApiService.searchPlaces(
                    query = trimmedQuery,
                    page = nextPage,
                    pageSize = PLACES_PAGE_SIZE
                )
            }.onSuccess { response ->
                if (requestId != searchRequestId || _uiState.value.query.trim() != trimmedQuery) return@onSuccess
                val existingIds = _uiState.value.places.map { it.id }.toHashSet()
                _uiState.update {
                    it.copy(
                        places = it.places + response.data.filter { place -> place.id !in existingIds },
                        isLoadingMorePlaces = false,
                        placesLoadMoreErrorMessage = null,
                        placesPage = response.pageNumber,
                        placesTotalPages = response.totalPages
                    )
                }
            }.onFailure { throwable ->
                if (requestId != searchRequestId || _uiState.value.query.trim() != trimmedQuery) return@onFailure
                _uiState.update {
                    it.copy(
                        isLoadingMorePlaces = false,
                        placesLoadMoreErrorMessage = throwable.userMessage("Không thể tải thêm địa điểm")
                    )
                }
            }
        }
    }

    fun removeRecentSearch(query: String) {
        searchHistoryRepository.removeRecentSearch(query)
    }

    fun clearRecentSearches() {
        searchHistoryRepository.clearRecentSearches()
    }

    private fun hasLoadedSearchResults(query: String): Boolean {
        val state = _uiState.value
        return lastLoadedQuery == query &&
            state.query.trim() == query &&
            !state.isLoadingPosts &&
            !state.isLoadingUsers &&
            !state.isLoadingPlaces &&
            state.postsErrorMessage == null &&
            state.usersErrorMessage == null &&
            state.placesErrorMessage == null
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
        val nextLiked = !wasLiked
        val currentLikeCount = currentPost.likeCount?.coerceAtLeast(0) ?: 0
        val nextLikeCount = (currentLikeCount + if (nextLiked) 1 else -1).coerceAtLeast(0)

        viewModelScope.launch {
            _uiState.update { it.copy(likingPostIds = it.likingPostIds + postId) }
            updatePost(postId) { post ->
                post.copy(
                    likedByCurrentUser = nextLiked,
                    likeCount = nextLikeCount
                )
            }

            val result = if (wasLiked) {
                unlikePostUseCase(postId)
            } else {
                likePostUseCase(postId)
            }

            result
                .onSuccess { response ->
                    val likeCount = response.likeCount.coerceAtLeast(0)
                    updatePost(postId) { post ->
                        post.copy(
                            likedByCurrentUser = response.liked,
                            likeCount = likeCount
                        )
                    }
                    postInteractionEventBus.publish(
                        PostInteractionEvent.LikeChanged(
                            postId = postId,
                            liked = response.liked,
                            likeCount = likeCount
                        )
                    )
                }
                .onFailure { throwable ->
                    updatePost(postId) { post ->
                        post.copy(
                            likedByCurrentUser = wasLiked,
                            likeCount = currentPost.likeCount
                        )
                    }
                    _uiState.update {
                        it.copy(postsErrorMessage = throwable.userMessage("Không thể cập nhật lượt thích"))
                    }
                }

            _uiState.update { it.copy(likingPostIds = it.likingPostIds - postId) }
        }
    }

    fun onSaveClicked(postId: Long) {
        val currentPost = _uiState.value.posts.firstOrNull { it.id == postId } ?: return
        if (postId in _uiState.value.savingPostIds) return
        val currentlySaved = currentPost.savedByCurrentUser == true
        val targetSaved = !currentlySaved
        val currentSaveCount = currentPost.saveCount?.coerceAtLeast(0) ?: 0
        val targetSaveCount = (currentSaveCount + if (targetSaved) 1 else -1).coerceAtLeast(0)

        viewModelScope.launch {
            _uiState.update { it.copy(savingPostIds = it.savingPostIds + postId) }
            updatePost(postId) {
                post -> post.copy(savedByCurrentUser = targetSaved, saveCount = targetSaveCount)
            }

            savePostUseCase(postId, currentlySaved = currentlySaved)
                .onSuccess { response ->
                    val saveCount = response.saveCount.coerceAtLeast(0)
                    updatePost(postId) {
                        post -> post.copy(
                            savedByCurrentUser = response.saved,
                            saveCount = saveCount
                        )
                    }
                    postInteractionEventBus.publish(
                        PostInteractionEvent.SaveChanged(
                            postId = postId,
                            saved = response.saved,
                            saveCount = saveCount
                        )
                    )
                }
                .onFailure { throwable ->
                    updatePost(postId) {
                        post -> post.copy(
                            savedByCurrentUser = currentPost.savedByCurrentUser,
                            saveCount = currentPost.saveCount
                        )
                    }
                    _uiState.update {
                        it.copy(postsErrorMessage = throwable.userMessage("Không thể lưu bài viết"))
                    }
                }

            _uiState.update { it.copy(savingPostIds = it.savingPostIds - postId) }
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
            _uiState.update { it.copy(commentErrorMessage = "Vui lòng nhập bình luận") }
            return
        }
        if (currentState.isCommentSubmitting) return

        viewModelScope.launch {
            _uiState.update { it.copy(isCommentSubmitting = true, commentErrorMessage = null) }

            addCommentUseCase(postId = postId, content = content)
                .onSuccess { response ->
                    val commentUiModel = toCommentUiModel(response)
                    var updatedCommentCount: Int? = null
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
                                    val nextCommentCount = ((post.commentCount ?: 0) + 1).coerceAtLeast(0)
                                    updatedCommentCount = nextCommentCount
                                    post.copy(commentCount = nextCommentCount)
                                } else {
                                    post
                                }
                            }
                        )
                    }
                    updatedCommentCount?.let { commentCount ->
                        postInteractionEventBus.publish(
                            PostInteractionEvent.CommentCountChanged(
                                postId = postId,
                                commentCount = commentCount
                            )
                        )
                    }
                }
                .onFailure { throwable ->
                    _uiState.update {
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
                    val commentCount = response.totalElements.toSafeCount()
                    _uiState.update { state ->
                        state.copy(
                            isCommentsLoading = false,
                            commentsErrorMessage = null,
                            commentsByPostId = state.commentsByPostId + (
                                postId to response.data.map(::toCommentUiModel)
                            ),
                            posts = state.posts.map { post ->
                                if (post.id == postId) {
                                    post.copy(commentCount = commentCount)
                                } else {
                                    post
                                }
                            }
                        )
                    }
                    postInteractionEventBus.publish(
                        PostInteractionEvent.CommentCountChanged(
                            postId = postId,
                            commentCount = commentCount
                        )
                    )
                }
                .onFailure { throwable ->
                    _uiState.update {
                        it.copy(
                            isCommentsLoading = false,
                            commentsErrorMessage = throwable.userMessage("Không thể tải bình luận")
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

    private fun collectPostInteractionEvents() {
        viewModelScope.launch {
            postInteractionEventBus.events.collect { event ->
                when (event) {
                    is PostInteractionEvent.LikeChanged -> updatePost(event.postId) { post ->
                        post.copy(
                            likedByCurrentUser = event.liked,
                            likeCount = event.likeCount.coerceAtLeast(0)
                        )
                    }

                    is PostInteractionEvent.SaveChanged -> updatePost(event.postId) { post ->
                        post.copy(
                            savedByCurrentUser = event.saved,
                            saveCount = event.saveCount.coerceAtLeast(0)
                        )
                    }

                    is PostInteractionEvent.CommentCountChanged -> updatePost(event.postId) { post ->
                        post.copy(commentCount = event.commentCount.coerceAtLeast(0))
                    }

                    is PostInteractionEvent.UserProfileChanged -> updateUserProfile(event)
                }
            }
        }
    }

    private fun updateUserProfile(event: PostInteractionEvent.UserProfileChanged) {
        val username = event.username.takeIf { it.isNotBlank() }
        val name = event.name.takeIf { it.isNotBlank() }
        val avatarUrl = event.avatarUrl?.takeIf { it.isNotBlank() }
        _uiState.update { state ->
            state.copy(
                posts = state.posts.map { post ->
                    if (post.owner.id == event.userId) {
                        post.copy(
                            owner = post.owner.copy(
                                username = username ?: post.owner.username,
                                avatarUrl = avatarUrl
                            )
                        )
                    } else {
                        post
                    }
                },
                users = state.users.map { user ->
                    if (user.id == event.userId) {
                        user.copy(
                            username = username ?: user.username,
                            name = name ?: user.name,
                            avatarUrl = avatarUrl
                        )
                    } else {
                        user
                    }
                },
                commentsByPostId = state.commentsByPostId.mapValues { (_, comments) ->
                    comments.map { comment ->
                        if (comment.ownerId == event.userId) {
                            comment.copy(
                                username = username ?: comment.username,
                                avatarUrl = avatarUrl
                            )
                        } else {
                            comment
                        }
                    }
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

    private companion object {
        const val PLACES_PAGE_SIZE = 10
    }
    private fun Long.toSafeCount(): Int = coerceIn(0L, Int.MAX_VALUE.toLong()).toInt()

}
