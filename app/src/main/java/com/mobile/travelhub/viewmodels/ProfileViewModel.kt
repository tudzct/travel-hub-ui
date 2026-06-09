package com.mobile.travelhub.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mobile.travelhub.data.LocationRepository
import com.mobile.travelhub.data.AvatarRepository
import com.mobile.travelhub.data.AuthRepository
import com.mobile.travelhub.data.PostRepository
import com.mobile.travelhub.data.api.UserApiService
import com.mobile.travelhub.data.httpStatusCode
import com.mobile.travelhub.data.userMessage
import com.mobile.travelhub.data.model.AdminProvinceResponse
import com.mobile.travelhub.data.model.BankAccountRequest
import com.mobile.travelhub.data.model.FeedPostResponse
import com.mobile.travelhub.data.model.ChangePasswordRequest
import com.mobile.travelhub.data.model.PostCommentResponse
import com.mobile.travelhub.data.model.ProfileUpdateRequest
import com.mobile.travelhub.data.model.UserProfileResponse
import com.mobile.travelhub.data.model.UserSummaryResponse
import com.mobile.travelhub.utils.PostsUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val userApiService: UserApiService,
    private val postRepository: PostRepository,
    private val avatarRepository: AvatarRepository,
    private val locationRepository: LocationRepository
) : ViewModel() {
    private val sessionUserId: Long
        get() = authRepository.getSavedSession()?.userId?.toLong() ?: -1L

    private val _profileState = MutableStateFlow<UiState<UserProfileResponse>>(UiState.Loading)
    val profileState: StateFlow<UiState<UserProfileResponse>> = _profileState.asStateFlow()

    private val _otherUserProfileState = MutableStateFlow<UiState<UserProfileResponse>>(UiState.Idle)
    val otherUserProfileState: StateFlow<UiState<UserProfileResponse>> = _otherUserProfileState.asStateFlow()

    private val _followersState = MutableStateFlow<UiState<List<UserSummaryResponse>>>(UiState.Loading)
    val followersState: StateFlow<UiState<List<UserSummaryResponse>>> = _followersState.asStateFlow()

    private val _followingState = MutableStateFlow<UiState<List<UserSummaryResponse>>>(UiState.Loading)
    val followingState: StateFlow<UiState<List<UserSummaryResponse>>> = _followingState.asStateFlow()

    private val _updateStatus = MutableStateFlow<UiState<Boolean>>(UiState.Idle)
    val updateStatus: StateFlow<UiState<Boolean>> = _updateStatus.asStateFlow()

    private val _changePasswordState = MutableStateFlow<UiState<Boolean>>(UiState.Idle)
    val changePasswordState: StateFlow<UiState<Boolean>> = _changePasswordState.asStateFlow()

    private val _provincePickerState = MutableStateFlow(ProvincePickerUiState())
    val provincePickerState: StateFlow<ProvincePickerUiState> = _provincePickerState.asStateFlow()

    private val _profilePostsState = MutableStateFlow(ProfilePostsUiState(isLoading = true))
    val profilePostsState: StateFlow<ProfilePostsUiState> = _profilePostsState.asStateFlow()

    private val _unauthorized = MutableStateFlow(false)
    val unauthorized: StateFlow<Boolean> = _unauthorized.asStateFlow()

    init {
        loadUserProfile()
        loadProvinces()
    }

    fun getCurrentUserId(): Long = sessionUserId

    fun loadUserProfile() {
        viewModelScope.launch {
            _profileState.value = UiState.Loading
            try {
                _unauthorized.value = false
                if (sessionUserId <= 0L) {
                    error("Bạn cần đăng nhập để xem hồ sơ")
                }
                val response = userApiService.getMyProfile()
                _profileState.value = UiState.Success(response)
                Log.d("API_SUCCESS", "Tải Profile thành công: $response")
            } catch (e: Exception) {
                if (e.httpStatusCode() == 401) {
                    _unauthorized.value = true
                }
                val errorMsg = e.userMessage("Không thể tải hồ sơ")
                Log.e("API_ERROR", errorMsg, e)
                _profileState.value = UiState.Error(errorMsg)
            }
        }
    }

    fun selectProfileProvince(provinceId: Long) {
        _provincePickerState.update { state ->
            state.copy(selectedProvinceId = provinceId, errorMessage = null)
        }
    }

    fun clearProfileProvinceSelection() {
        _provincePickerState.update { state ->
            state.copy(selectedProvinceId = null, errorMessage = null)
        }
    }

    fun retryLoadProfileProvinces() {
        loadProvinces()
    }

    fun loadUserPosts(userId: Long = sessionUserId) {
        loadProfilePosts(userId = userId, tab = ProfilePostsTab.POSTS)
    }

    fun loadUserLikedPosts(userId: Long = sessionUserId) {
        loadProfilePosts(userId = userId, tab = ProfilePostsTab.LIKED)
    }

    fun loadUserSavedPosts(userId: Long = sessionUserId) {
        loadProfilePosts(userId = userId, tab = ProfilePostsTab.SAVED)
    }

    fun selectProfilePostsTab(tab: ProfilePostsTab, userId: Long = sessionUserId) {
        if (_profilePostsState.value.selectedTab == tab && !_profilePostsState.value.isLoading) return
        loadProfilePosts(userId = userId, tab = tab)
    }

    private fun loadProfilePosts(userId: Long, tab: ProfilePostsTab) {
        viewModelScope.launch {
            if (userId <= 0L) {
                _profilePostsState.value = ProfilePostsUiState(
                    isLoading = false,
                    selectedTab = tab,
                    errorMessage = "Bạn cần đăng nhập để xem bài viết"
                )
                return@launch
            }

            val currentComments = _profilePostsState.value.commentsByPostId
            _profilePostsState.value = ProfilePostsUiState(isLoading = true, selectedTab = tab)
            val result = when (tab) {
                ProfilePostsTab.POSTS -> postRepository.getPostsByUser(userId = userId, page = 0, pageSize = 20)
                ProfilePostsTab.SAVED -> postRepository.getSavedPostsByUser(userId = userId, page = 0, pageSize = 20)
                ProfilePostsTab.LIKED -> postRepository.getLikedPostsByUser(userId = userId, page = 0, pageSize = 20)
            }
            result
                .onSuccess { posts ->
                    _profilePostsState.value = ProfilePostsUiState(
                        isLoading = false,
                        selectedTab = tab,
                        posts = posts.mapNotNull { post ->
                            runCatching { toHomePostUiModel(post) }.getOrNull()
                        },
                        commentsByPostId = currentComments
                    )
                }
                .onFailure { throwable ->
                    _profilePostsState.value = ProfilePostsUiState(
                        isLoading = false,
                        selectedTab = tab,
                        errorMessage = throwable.userMessage("Không thể tải bài viết")
                    )
                }
        }
    }

    fun onLikeClicked(postId: Long) {
        val currentPost = _profilePostsState.value.posts.firstOrNull { it.id == postId } ?: return
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
                postRepository.unlikePost(postId)
            } else {
                postRepository.likePost(postId)
            }

            result
                .onSuccess { response ->
                    if (!response.liked && _profilePostsState.value.selectedTab == ProfilePostsTab.LIKED) {
                        _profilePostsState.update { state ->
                            state.copy(posts = state.posts.filterNot { it.id == postId })
                        }
                    } else {
                        updatePost(postId) {
                            it.copy(
                                isLiked = response.liked,
                                likeCount = response.likeCount.coerceAtLeast(0),
                                isLikeLoading = false
                            )
                        }
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
                    _profilePostsState.update {
                        it.copy(errorMessage = throwable.userMessage("Không thể cập nhật lượt thích"))
                    }
                }
        }
    }

    fun onSaveClicked(postId: Long) {
        val currentPost = _profilePostsState.value.posts.firstOrNull { it.id == postId } ?: return
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
            postRepository.toggleSavedPost(postId, currentlySaved = currentPost.isSaved)
                .onSuccess { response ->
                    if (!response.saved && _profilePostsState.value.selectedTab == ProfilePostsTab.SAVED) {
                        _profilePostsState.update { state ->
                            state.copy(posts = state.posts.filterNot { it.id == postId })
                        }
                    } else {
                        updatePost(postId) {
                            it.copy(
                                isSaved = response.saved,
                                saveCount = response.saveCount.coerceAtLeast(0),
                                isSaveLoading = false
                            )
                        }
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
                    _profilePostsState.update {
                        it.copy(errorMessage = throwable.userMessage("Không thể lưu bài viết"))
                    }
                }
        }
    }

    fun onCommentClicked(postId: Long) {
        _profilePostsState.update {
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
        _profilePostsState.update {
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
        _profilePostsState.update {
            it.copy(commentInput = value, commentErrorMessage = null)
        }
    }

    fun submitComment() {
        val currentState = _profilePostsState.value
        val postId = currentState.activeCommentPostId ?: return
        val content = currentState.commentInput.trim()

        if (content.isBlank()) {
            _profilePostsState.update { it.copy(commentErrorMessage = "Vui lòng nhập bình luận") }
            return
        }

        if (currentState.isCommentSubmitting) return

        _profilePostsState.update {
            it.copy(isCommentSubmitting = true, commentErrorMessage = null)
        }

        viewModelScope.launch {
            postRepository.addComment(postId = postId, content = content)
                .onSuccess { response ->
                    val commentUiModel = toCommentUiModel(response)
                    _profilePostsState.update { state ->
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
                    _profilePostsState.update {
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
            postRepository.getPostComments(postId = postId, page = 0, pageSize = 50)
                .onSuccess { response ->
                    _profilePostsState.update { state ->
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
                    _profilePostsState.update {
                        it.copy(
                            isCommentsLoading = false,
                            commentsErrorMessage = throwable.userMessage("Không thể tải bình luận")
                        )
                    }
                }
        }
    }

    private fun updatePost(postId: Long, transform: (HomePostUiModel) -> HomePostUiModel) {
        _profilePostsState.update { state ->
            state.copy(
                posts = state.posts.map { post ->
                    if (post.id == postId) transform(post) else post
                }
            )
        }
    }

    fun loadOtherUserProfile(userId: Long) {
        viewModelScope.launch {
            _otherUserProfileState.value = UiState.Loading
            try {
                val response = userApiService.getUserProfile(userId)
                _otherUserProfileState.value = UiState.Success(response)
                Log.d("API_SUCCESS", "Tải Other Profile thành công: $response")
            } catch (e: Exception) {
                val errorMsg = e.userMessage("Không thể tải hồ sơ người dùng")
                Log.e("API_ERROR", errorMsg, e)
                _otherUserProfileState.value = UiState.Error(errorMsg)
            }
        }
    }

    suspend fun uploadAvatar(
        imageBytes: ByteArray,
        mimeType: String,
        fileName: String
    ): String = avatarRepository.uploadAvatar(
        userId = sessionUserId,
        imageBytes = imageBytes,
        mimeType = mimeType,
        fileName = fileName
    )

    fun loadFollowers(userId: Long = sessionUserId) {
        viewModelScope.launch {
            _followersState.value = UiState.Loading
            try {
                if (userId <= 0L) {
                    error("Bạn cần đăng nhập để xem followers")
                }
                val response = userApiService.getFollowers(userId)
                _followersState.value = UiState.Success(response.content)
            } catch (e: Exception) {
                val errorMsg = e.userMessage("Không thể tải danh sách người theo dõi")
                Log.e("API_ERROR", errorMsg, e)
                _followersState.value = UiState.Error(errorMsg)
            }
        }
    }

    fun loadFollowing(userId: Long = sessionUserId) {
        viewModelScope.launch {
            _followingState.value = UiState.Loading
            try {
                if (userId <= 0L) {
                    error("Bạn cần đăng nhập để xem following")
                }
                val response = userApiService.getFollowing(userId)
                _followingState.value = UiState.Success(response.content)
            } catch (e: Exception) {
                val errorMsg = e.userMessage("Không thể tải danh sách đang theo dõi")
                Log.e("API_ERROR", errorMsg, e)
                _followingState.value = UiState.Error(errorMsg)
            }
        }
    }

    suspend fun updateProfile(
        name: String,
        username: String,
        bio: String,
        dob: String,
        gender: String,
        location: String,
        bankCode: String,
        bankName: String,
        accountNumber: String,
        accountName: String,
        avatarUrl: String? = null
    ): UserProfileResponse {
        _updateStatus.value = UiState.Loading
        try {
            if (sessionUserId <= 0L) {
                error("Bạn cần đăng nhập để cập nhật hồ sơ")
            }
            val bankValues = listOf(bankCode, bankName, accountNumber, accountName).map { it.trim() }
            val hasAnyBankValue = bankValues.any { it.isNotBlank() }
            if (hasAnyBankValue && bankValues.any { it.isBlank() }) {
                error("Vui lòng nhập đầy đủ ngân hàng, mã ngân hàng, số tài khoản và tên tài khoản")
            }
            val currentProfile = (_profileState.value as? UiState.Success)?.data

            val request = ProfileUpdateRequest(
                id = sessionUserId,
                username = username.trim(),
                name = name.trim(),
                bio = bio.takeIf { it.isNotBlank() },
                dateOfBirth = dob.takeIf { it.isNotBlank() },
                gender = gender.takeIf { it.isNotBlank() },
                location = location.takeIf { it.isNotBlank() },
                email = currentProfile?.email,
                phoneNumber = currentProfile?.phoneNumber,
                avatarUrl = avatarUrl?.takeIf { it.isNotBlank() } ?: currentProfile?.avatarUrl,
                isFollowing = currentProfile?.isFollowing ?: false,
                postsCount = currentProfile?.postsCount ?: 0,
                followersCount = currentProfile?.followersCount ?: 0,
                followingCount = currentProfile?.followingCount ?: 0
            )
            withContext(Dispatchers.IO) {
                userApiService.updateMyProfile(request)
            }
            if (hasAnyBankValue) {
                withContext(Dispatchers.IO) {
                    userApiService.upsertDefaultBankAccount(
                        BankAccountRequest(
                            bankCode = bankCode.trim(),
                            bankName = bankName.trim(),
                            accountNumber = accountNumber.trim(),
                            accountName = accountName.trim(),
                            isDefault = true
                        )
                    )
                }
            }
            val refreshedProfile = withContext(Dispatchers.IO) {
                userApiService.getMyProfile()
            }
            _profileState.value = UiState.Success(refreshedProfile)
            _updateStatus.value = UiState.Success(true)
            Log.d("API_SUCCESS", "Cập nhật Profile thành công!")
            return response
        } catch (e: Exception) {
            val errorMsg = e.userMessage("Không thể cập nhật hồ sơ")
            Log.e("API_ERROR", errorMsg, e)
            _updateStatus.value = UiState.Error(errorMsg)
            throw e
        }
    }

    private fun loadProvinces() {
        viewModelScope.launch {
            _provincePickerState.update { it.copy(isLoading = true, errorMessage = null) }
            runCatching { locationRepository.getProvinces() }
                .onSuccess { provinces ->
                    _provincePickerState.update {
                        it.copy(
                            provinces = provinces,
                            isLoading = false,
                            errorMessage = null
                        )
                    }
                }
                .onFailure { throwable ->
                    _provincePickerState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = throwable.userMessage("Không thể tải danh sách tỉnh/thành phố")
                        )
                    }
                }
        }
    }

    fun changePassword(
        currentPassword: String,
        newPassword: String,
        confirmPassword: String
    ) {
        val validationError = validateChangePassword(currentPassword, newPassword, confirmPassword)
        if (validationError != null) {
            _changePasswordState.value = UiState.Error(validationError)
            return
        }
        viewModelScope.launch {
            _changePasswordState.value = UiState.Loading
            try {
                withContext(Dispatchers.IO) {
                    userApiService.changePassword(
                        ChangePasswordRequest(
                            currentPassword = currentPassword,
                            newPassword = newPassword
                        )
                    )
                }
                _changePasswordState.value = UiState.Success(true)
            } catch (e: Exception) {
                val errorMsg = e.userMessage("Không thể đổi mật khẩu")
                Log.e("API_ERROR", "Lỗi đổi mật khẩu: $errorMsg", e)
                _changePasswordState.value = UiState.Error(errorMsg)
            }
        }
    }

    fun clearChangePasswordState() {
        _changePasswordState.value = UiState.Idle
    }

    private fun validateChangePassword(
        currentPassword: String,
        newPassword: String,
        confirmPassword: String
    ): String? {
        if (currentPassword.isBlank()) return "Vui lòng nhập mật khẩu hiện tại"
        if (newPassword.length < 8) return "Mật khẩu mới phải có ít nhất 8 ký tự"
        if (newPassword != confirmPassword) return "Mật khẩu mới không khớp"
        if (currentPassword == newPassword) return "Mật khẩu mới phải khác mật khẩu hiện tại"
        return null
    }

    fun toggleFollow(
        targetUserId: Long,
        isCurrentlyFollowing: Boolean,
        connectionsOwnerUserId: Long = sessionUserId
    ) {
        viewModelScope.launch {
            try {
                if (targetUserId == sessionUserId) return@launch

                if (isCurrentlyFollowing) {
                    userApiService.unfollowUser(targetUserId)
                } else {
                    userApiService.followUser(targetUserId)
                }

                loadUserProfile()
                if (connectionsOwnerUserId != sessionUserId) {
                    loadOtherUserProfile(connectionsOwnerUserId)
                }
                loadFollowers(connectionsOwnerUserId)
                loadFollowing(connectionsOwnerUserId)
            } catch (e: Exception) {
                Log.e("API_ERROR", "Lỗi theo dõi/bỏ theo dõi: ${e.userMessage("Không thể cập nhật trạng thái theo dõi")}", e)
            }
        }
    }

    fun toggleFollowOtherUser(targetUserId: Long, isCurrentlyFollowing: Boolean) {
        viewModelScope.launch {
            if (targetUserId == sessionUserId) return@launch

            val previousOtherProfileState = _otherUserProfileState.value
            val previousOwnProfileState = _profileState.value
            val nextFollowingState = !isCurrentlyFollowing

            updateOtherProfileFollowState(targetUserId, nextFollowingState)
            updateOwnFollowingCount(if (nextFollowingState) 1 else -1)

            try {
                if (isCurrentlyFollowing) {
                    userApiService.unfollowUser(targetUserId)
                } else {
                    userApiService.followUser(targetUserId)
                }
            } catch (e: Exception) {
                _otherUserProfileState.value = previousOtherProfileState
                _profileState.value = previousOwnProfileState
                Log.e("API_ERROR", "Lỗi theo dõi/bỏ theo dõi hồ sơ khác: ${e.userMessage("Không thể cập nhật trạng thái theo dõi")}", e)
            }
        }
    }

    private fun updateOtherProfileFollowState(targetUserId: Long, isFollowing: Boolean) {
        _otherUserProfileState.update { state ->
            val currentProfile = (state as? UiState.Success)?.data ?: return@update state
            if (currentProfile.id != targetUserId) return@update state

            val followerDelta = when {
                isFollowing && !currentProfile.isFollowing -> 1
                !isFollowing && currentProfile.isFollowing -> -1
                else -> 0
            }

            UiState.Success(
                currentProfile.copy(
                    isFollowing = isFollowing,
                    followersCount = (currentProfile.followersCount + followerDelta).coerceAtLeast(0)
                )
            )
        }
    }

    private fun updateOwnFollowingCount(delta: Int) {
        if (delta == 0) return

        _profileState.update { state ->
            val currentProfile = (state as? UiState.Success)?.data ?: return@update state
            UiState.Success(
                currentProfile.copy(
                    followingCount = (currentProfile.followingCount + delta).coerceAtLeast(0)
                )
            )
        }
    }

    fun resetUpdateStatus() {
        _updateStatus.value = UiState.Idle
    }

    fun clearUnauthorized() {
        _unauthorized.value = false
    }

    private fun toHomePostUiModel(post: FeedPostResponse): HomePostUiModel {
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
}

data class ProfilePostsUiState(
    val isLoading: Boolean = false,
    val selectedTab: ProfilePostsTab = ProfilePostsTab.POSTS,
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

enum class ProfilePostsTab {
    POSTS,
    SAVED,
    LIKED
}

sealed class UiState<out T> {
    data object Idle : UiState<Nothing>()
    data object Loading : UiState<Nothing>()
    data class Success<T>(val data: T) : UiState<T>()
    data class Error(val message: String) : UiState<Nothing>()
}

data class ProvincePickerUiState(
    val provinces: List<AdminProvinceResponse> = emptyList(),
    val selectedProvinceId: Long? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)
