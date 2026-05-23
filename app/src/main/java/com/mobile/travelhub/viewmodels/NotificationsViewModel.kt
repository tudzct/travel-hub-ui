package com.mobile.travelhub.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mobile.travelhub.data.api.UserApiService
import com.mobile.travelhub.data.model.NotificationResponse
import com.mobile.travelhub.usecase.GetNotificationsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import java.time.Instant
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class NotificationsViewModel @Inject constructor(
    private val getNotificationsUseCase: GetNotificationsUseCase,
    private val userApiService: UserApiService
) : ViewModel() {
    private val _uiState = MutableStateFlow(NotificationsUiState(isLoading = true))
    val uiState: StateFlow<NotificationsUiState> = _uiState.asStateFlow()

    init {
        refreshNotifications()
    }

    fun setFilter(filter: NotificationFilter) {
        _uiState.update { state ->
            state.copy(activeFilter = filter)
        }
    }

    fun markAllRead() {
        _uiState.update { state ->
            state.copy(
                notifications = state.notifications.map { it.copy(isRead = true) }
            )
        }
    }

    fun refreshNotifications(pageNumber: Int = 0, pageSize: Int = 10) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            getNotificationsUseCase(pageNumber = pageNumber, pageSize = pageSize)
                .onSuccess { items ->
                    val mapped = items.mapNotNull { response ->
                        toNotificationModel(response)
                    }
                    val resolved = resolveMissingFollowTargets(mapped)
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            notifications = resolved,
                            errorMessage = null
                        )
                    }
                }
                .onFailure { throwable ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            notifications = emptyList(),
                            errorMessage = throwable.message ?: "Failed to load unread notifications"
                        )
                    }
                }
        }
    }

    private suspend fun resolveMissingFollowTargets(
        notifications: List<NotificationModel>
    ): List<NotificationModel> {
        return notifications.map { notification ->
            if (notification.type != NotificationType.FOLLOW || notification.targetId != null) {
                return@map notification
            }

            val username = extractFollowerUsername(notification.body) ?: return@map notification
            val resolvedUserId = runCatching {
                userApiService.searchUsers(username = username, page = 0, pageSize = 1)
                    .data
                    .firstOrNull { it.username.equals(username, ignoreCase = true) }
                    ?.id
            }.getOrNull()

            if (resolvedUserId != null) {
                notification.copy(targetId = resolvedUserId)
            } else {
                notification
            }
        }
    }
}

data class NotificationsUiState(
    val notifications: List<NotificationModel> = emptyList(),
    val activeFilter: NotificationFilter = NotificationFilter.All,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

enum class NotificationFilter(val label: String) {
    All("All"),
    Unread("Unread")
}

data class NotificationModel(
    val title: String,
    val body: String,
    val isRead: Boolean,
    val createdAt: Instant,
    val type: NotificationType?,
    val targetId: Long?
)

enum class NotificationType {
    COMMENT,
    LIKE,
    FOLLOW
}

private fun toNotificationModel(response: NotificationResponse): NotificationModel? {
    val createdAt = runCatching { Instant.parse(response.createdAt) }.getOrNull() ?: return null
    val type = parseNotificationType(response)

    return NotificationModel(
        title = response.title,
        body = response.body,
        isRead = response.isRead,
        createdAt = createdAt,
        type = type,
        targetId = response.targetId
    )
}

private fun parseNotificationType(response: NotificationResponse): NotificationType? {
    val rawType = response.type
        ?.trim()
        ?.uppercase()
        .orEmpty()

    return when {
        rawType.contains("FOLLOW") -> NotificationType.FOLLOW
        rawType.contains("COMMENT") -> NotificationType.COMMENT
        rawType.contains("LIKE") -> NotificationType.LIKE
        isFollowNotification(response) -> NotificationType.FOLLOW
        else -> null
    }
}

private fun isFollowNotification(response: NotificationResponse): Boolean {
    val text = "${response.title} ${response.body}".uppercase()
    return text.contains("FOLLOWER") || text.contains("FOLLOWING")
}

private fun extractFollowerUsername(body: String): String? {
    return body.substringBefore(" started following you")
        .trim()
        .takeIf { it.isNotBlank() && it != body }
}
