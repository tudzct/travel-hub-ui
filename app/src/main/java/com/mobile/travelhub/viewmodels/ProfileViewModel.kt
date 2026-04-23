package com.mobile.travelhub.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mobile.travelhub.data.AuthRepository
import com.mobile.travelhub.data.PostRepository
import com.mobile.travelhub.data.api.UserApiService
import com.mobile.travelhub.data.model.FeedPostResponse
import com.mobile.travelhub.data.httpStatusCode
import com.mobile.travelhub.data.model.ProfileUpdateRequest
import com.mobile.travelhub.data.model.UserProfileResponse
import com.mobile.travelhub.data.model.UserSummaryResponse
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

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val userApiService: UserApiService,
    private val postRepository: PostRepository
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

    private val _profilePostsState = MutableStateFlow(ProfilePostsUiState(isLoading = true))
    val profilePostsState: StateFlow<ProfilePostsUiState> = _profilePostsState.asStateFlow()

    private val _unauthorized = MutableStateFlow(false)
    val unauthorized: StateFlow<Boolean> = _unauthorized.asStateFlow()

    init {
        loadUserProfile()
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
                val errorMsg = "Lỗi gọi API Profile (/api/users/me): ${e.localizedMessage}"
                Log.e("API_ERROR", errorMsg, e)
                _profileState.value = UiState.Error(errorMsg)
            }
        }
    }

    fun loadUserPosts(userId: Long = sessionUserId) {
        viewModelScope.launch {
            if (userId <= 0L) {
                _profilePostsState.value = ProfilePostsUiState(
                    isLoading = false,
                    errorMessage = "Bạn cần đăng nhập để xem bài viết"
                )
                return@launch
            }

            _profilePostsState.value = ProfilePostsUiState(isLoading = true)
            postRepository.getPostsByUser(userId = userId, page = 0, pageSize = 20)
                .onSuccess { posts ->
                    _profilePostsState.value = ProfilePostsUiState(
                        isLoading = false,
                        posts = posts.mapNotNull { post ->
                            runCatching { toHomePostUiModel(post) }.getOrNull()
                        }
                    )
                }
                .onFailure { throwable ->
                    _profilePostsState.value = ProfilePostsUiState(
                        isLoading = false,
                        errorMessage = throwable.message ?: "Không thể tải bài viết"
                    )
                }
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
                val errorMsg = "Lỗi gọi API Other Profile (/api/users/$userId): ${e.localizedMessage}"
                Log.e("API_ERROR", errorMsg, e)
                _otherUserProfileState.value = UiState.Error(errorMsg)
            }
        }
    }

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
                val errorMsg = "Lỗi gọi API Followers: ${e.localizedMessage}"
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
                val errorMsg = "Lỗi gọi API Following: ${e.localizedMessage}"
                Log.e("API_ERROR", errorMsg, e)
                _followingState.value = UiState.Error(errorMsg)
            }
        }
    }

    fun updateProfile(name: String, username: String, bio: String, dob: String, gender: String, location: String) {
        viewModelScope.launch {
            _updateStatus.value = UiState.Loading
            try {
                if (sessionUserId <= 0L) {
                    error("Bạn cần đăng nhập để cập nhật hồ sơ")
                }
                val currentProfile = (_profileState.value as? UiState.Success)?.data

                val request = ProfileUpdateRequest(
                    id = sessionUserId,
                    username = username,
                    name = name,
                    bio = bio,
                    dateOfBirth = dob,
                    gender = gender,
                    location = location,
                    email = currentProfile?.email,
                    phoneNumber = currentProfile?.phoneNumber,
                    avatarUrl = currentProfile?.avatarUrl,
                    isFollowing = currentProfile?.isFollowing ?: false,
                    postsCount = currentProfile?.postsCount ?: 0,
                    followersCount = currentProfile?.followersCount ?: 0,
                    followingCount = currentProfile?.followingCount ?: 0
                )
                val response = userApiService.updateMyProfile(request)
                _profileState.value = UiState.Success(response)
                _updateStatus.value = UiState.Success(true)
                Log.d("API_SUCCESS", "Cập nhật Profile thành công!")
            } catch (e: Exception) {
                val errorMsg = "Lỗi cập nhật Profile (PUT): ${e.localizedMessage}"
                Log.e("API_ERROR", errorMsg, e)
                _updateStatus.value = UiState.Error(errorMsg)
            }
        }
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
                Log.e("API_ERROR", "Lỗi follow/unfollow: ${e.localizedMessage}", e)
            }
        }
    }

    fun toggleFollowOtherUser(targetUserId: Long, isCurrentlyFollowing: Boolean) {
        viewModelScope.launch {
            try {
                if (targetUserId == sessionUserId) return@launch

                if (isCurrentlyFollowing) {
                    userApiService.unfollowUser(targetUserId)
                } else {
                    userApiService.followUser(targetUserId)
                }

                loadOtherUserProfile(targetUserId)
                loadUserProfile()
                loadFollowers()
                loadFollowing()
            } catch (e: Exception) {
                Log.e("API_ERROR", "Lỗi follow/unfollow other profile: ${e.localizedMessage}", e)
            }
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

data class ProfilePostsUiState(
    val isLoading: Boolean = false,
    val posts: List<HomePostUiModel> = emptyList(),
    val errorMessage: String? = null
)

sealed class UiState<out T> {
    data object Idle : UiState<Nothing>()
    data object Loading : UiState<Nothing>()
    data class Success<T>(val data: T) : UiState<T>()
    data class Error(val message: String) : UiState<Nothing>()
}
