package com.mobile.travelhub.data.model

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
    val createdAt: String
)
