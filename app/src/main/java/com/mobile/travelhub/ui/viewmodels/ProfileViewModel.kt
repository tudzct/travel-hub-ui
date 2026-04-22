package com.mobile.travelhub.ui.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mobile.travelhub.data.AuthRepository
import com.mobile.travelhub.data.api.BusinessClient
import com.mobile.travelhub.data.httpStatusCode
import com.mobile.travelhub.data.model.ProfileUpdateRequest
import com.mobile.travelhub.data.model.UserProfileResponse
import com.mobile.travelhub.data.model.UserSummaryResponse
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {
    private val api = BusinessClient.create(accessTokenProvider = authRepository::getAccessToken)

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
                val response = api.getMyProfile()
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

    fun loadOtherUserProfile(userId: Long) {
        viewModelScope.launch {
            _otherUserProfileState.value = UiState.Loading
            try {
                val response = api.getUserProfile(userId)
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
                val response = api.getFollowers(userId)
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
                val response = api.getFollowing(userId)
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
                val response = api.updateMyProfile(request)
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
                    api.unfollowUser(targetUserId)
                } else {
                    api.followUser(targetUserId)
                }

                // Refresh all related states after follow/unfollow.
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
                    api.unfollowUser(targetUserId)
                } else {
                    api.followUser(targetUserId)
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
}

sealed class UiState<out T> {
    data object Idle : UiState<Nothing>()
    data object Loading : UiState<Nothing>()
    data class Success<T>(val data: T) : UiState<T>()
    data class Error(val message: String) : UiState<Nothing>()
}
