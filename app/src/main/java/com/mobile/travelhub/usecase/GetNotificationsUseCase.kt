package com.mobile.travelhub.usecase

import com.mobile.travelhub.data.NotificationRepository
import com.mobile.travelhub.data.model.NotificationResponse
import javax.inject.Inject

class GetNotificationsUseCase @Inject constructor(
    private val notificationRepository: NotificationRepository
) {
    suspend operator fun invoke(pageNumber: Int = 0, pageSize: Int = 10): Result<List<NotificationResponse>> {
        return notificationRepository.getNotifications(
            pageNumber = pageNumber,
            pageSize = pageSize
        )
    }
}
