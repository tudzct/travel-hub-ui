package com.mobile.travelhub.data

import com.mobile.travelhub.data.api.NotificationApiService
import com.mobile.travelhub.data.model.NotificationResponse
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Singleton
class NotificationRepository @Inject constructor(
    private val notificationApiService: NotificationApiService
) {
    suspend fun getUnreadNotifications(pageNumber: Int = 0, pageSize: Int = 10): Result<List<NotificationResponse>> {
        return withContext(Dispatchers.IO) {
            runCatching {
                notificationApiService.getUnreadNotifications(
                    pageNumber = pageNumber,
                    pageSize = pageSize
                ).data
            }
        }
    }
}
