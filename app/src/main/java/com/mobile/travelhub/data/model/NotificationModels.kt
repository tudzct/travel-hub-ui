package com.mobile.travelhub.data.model

import com.google.gson.annotations.SerializedName

data class NotificationsPageResponse(
    val pageNumber: Int,
    val pageSize: Int,
    val totalPages: Int,
    val totalElements: Long,
    val data: List<NotificationResponse>
)

data class NotificationResponse(
    val title: String,
    val body: String,
    val isRead: Boolean,
    val type: String? = null,
    @SerializedName(
        value = "targetId",
        alternate = ["target", "target_id", "targetUserId", "userId", "actorId", "senderId", "followerId"]
    )
    val targetId: Long? = null,
    val createdAt: String
)
