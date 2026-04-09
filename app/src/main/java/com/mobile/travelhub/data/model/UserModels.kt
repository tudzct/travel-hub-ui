package com.mobile.travelhub.data.model

import com.google.gson.annotations.SerializedName

data class UserProfileResponse(
    val id: Long = 0,
    val username: String = "",
    val name: String = "",
    val bio: String? = null,
    val dateOfBirth: String? = null,
    val gender: String? = null,
    val location: String? = null,
    val email: String? = null,
    val phoneNumber: String? = null,
    @SerializedName("following")
    val isFollowing: Boolean = false,
    val postsCount: Int = 0,
    val followersCount: Int = 0,
    val followingCount: Int = 0,
    val avatarUrl: String? = null
)

data class UserSummaryResponse(
    val id: Long = 0,
    val username: String = "",
    val name: String = "",
    val avatarUrl: String? = null,
    @SerializedName("following")
    val isFollowing: Boolean = false
)

data class ProfileUpdateRequest(
    val id: Long,
    val username: String,
    val name: String,
    val bio: String?,
    val dateOfBirth: String?,
    val gender: String?,
    val location: String?,
    val email: String?,
    val phoneNumber: String?,
    val avatarUrl: String?,
    @SerializedName("following")
    val isFollowing: Boolean = false,
    val postsCount: Int = 0,
    val followersCount: Int = 0,
    val followingCount: Int = 0
)

data class PageResponse<T>(
    val content: List<T> = emptyList(),
    val totalPages: Int = 0,
    val totalElements: Long = 0,
    val size: Int = 0,
    val number: Int = 0
)
