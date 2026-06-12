package com.mobile.travelhub.data

import com.mobile.travelhub.data.api.NotificationApiService
import com.mobile.travelhub.data.model.NotificationsPageResponse
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Singleton
class NotificationRepository @Inject constructor(
    private val notificationApiService: NotificationApiService
) {
    suspend fun getNotifications(pageNumber: Int = 0, pageSize: Int = 10): Result<NotificationsPageResponse> {
        return withContext(Dispatchers.IO) {
            runCatching {
                notificationApiService.getNotifications(
                    pageNumber = pageNumber,
                    pageSize = pageSize
                )
            }
        }
    }

    suspend fun getUnreadNotifications(pageNumber: Int = 0, pageSize: Int = 10): Result<NotificationsPageResponse> {
        return withContext(Dispatchers.IO) {
            runCatching {
                notificationApiService.getUnreadNotifications(
                    pageNumber = pageNumber,
                    pageSize = pageSize
                )
            }
        }
    }

    suspend fun markAllNotificationsAsRead(): Result<Unit> {
        return withContext(Dispatchers.IO) {
            runCatching {
                notificationApiService.markAllNotificationsAsRead()
            }
        }
    }
}
