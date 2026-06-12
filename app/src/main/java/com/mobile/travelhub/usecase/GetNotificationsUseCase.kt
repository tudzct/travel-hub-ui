package com.mobile.travelhub.usecase

import com.mobile.travelhub.data.NotificationRepository
import com.mobile.travelhub.data.model.NotificationsPageResponse
import javax.inject.Inject

class GetNotificationsUseCase @Inject constructor(
    private val notificationRepository: NotificationRepository
) {
    suspend operator fun invoke(
        pageNumber: Int = 0,
        pageSize: Int = 10,
        unreadOnly: Boolean = false
    ): Result<NotificationsPageResponse> {
        return if (unreadOnly) {
            notificationRepository.getUnreadNotifications(
                pageNumber = pageNumber,
                pageSize = pageSize
            )
        } else {
            notificationRepository.getNotifications(
                pageNumber = pageNumber,
                pageSize = pageSize
            )
        }
    }
}
