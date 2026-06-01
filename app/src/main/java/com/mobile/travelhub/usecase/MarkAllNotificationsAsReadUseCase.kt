package com.mobile.travelhub.usecase

import com.mobile.travelhub.data.NotificationRepository
import javax.inject.Inject

class MarkAllNotificationsAsReadUseCase @Inject constructor(
    private val notificationRepository: NotificationRepository
) {
    suspend operator fun invoke(): Result<Unit> {
        return notificationRepository.markAllNotificationsAsRead()
    }
}
