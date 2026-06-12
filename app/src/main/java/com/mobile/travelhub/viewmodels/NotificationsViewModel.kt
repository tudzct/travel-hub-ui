package com.mobile.travelhub.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mobile.travelhub.data.userMessage
import com.mobile.travelhub.data.api.UserApiService
import com.mobile.travelhub.data.model.NotificationResponse
import com.mobile.travelhub.usecase.GetNotificationsUseCase
import com.mobile.travelhub.usecase.MarkAllNotificationsAsReadUseCase
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
    private val markAllNotificationsAsReadUseCase: MarkAllNotificationsAsReadUseCase,
    private val userApiService: UserApiService
) : ViewModel() {
    private val _uiState = MutableStateFlow(NotificationsUiState(isLoading = true))
    val uiState: StateFlow<NotificationsUiState> = _uiState.asStateFlow()

    fun setFilter(filter: NotificationFilter) {
        if (_uiState.value.activeFilter == filter) {
            return
        }
        _uiState.update { state -> state.copy(activeFilter = filter) }
        refreshNotifications()
    }

    fun markAllRead() {
        val hasUnreadNotifications = _uiState.value.notifications.any { !it.isRead }
        if (!hasUnreadNotifications || _uiState.value.isMarkingAllRead) {
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isMarkingAllRead = true, errorMessage = null) }

            markAllNotificationsAsReadUseCase()
                .onSuccess {
                    _uiState.update { state ->
                        state.copy(
                            isMarkingAllRead = false,
                            notifications = state.notifications.map { notification ->
                                notification.copy(isRead = true)
                            },
                            errorMessage = null
                        )
                    }
                }
                .onFailure { throwable ->
                    _uiState.update {
                        it.copy(
                            isMarkingAllRead = false,
                            errorMessage = throwable.userMessage("Không thể đánh dấu thông báo đã đọc")
                        )
                    }
                }
        }
    }

    fun refreshNotifications(pageNumber: Int = 0, pageSize: Int = NOTIFICATIONS_PAGE_SIZE) {
        viewModelScope.launch {
            val activeFilter = _uiState.value.activeFilter
            _uiState.update {
                it.copy(
                    isLoading = true,
                    isLoadingMore = false,
                    errorMessage = null,
                    loadMoreErrorMessage = null,
                    page = 0,
                    totalPages = 0,
                    totalElements = 0L
                )
            }

            getNotificationsUseCase(
                pageNumber = pageNumber,
                pageSize = pageSize,
                unreadOnly = activeFilter == NotificationFilter.Unread
            )
                .onSuccess { response ->
                    val mapped = response.data.mapNotNull { response ->
                        toNotificationModel(response)
                    }
                    val resolved = resolveMissingFollowTargets(mapped)
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            notifications = resolved,
                            page = response.pageNumber,
                            totalPages = response.totalPages,
                            totalElements = response.totalElements,
                            errorMessage = null
                        )
                    }
                }
                .onFailure { throwable ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            notifications = emptyList(),
                            loadMoreErrorMessage = null,
                            errorMessage = throwable.userMessage("Không thể tải thông báo")
                        )
                    }
                }
        }
    }

    fun loadMoreNotifications(pageSize: Int = NOTIFICATIONS_PAGE_SIZE) {
        val state = _uiState.value
        if (
            state.isLoading ||
            state.isLoadingMore ||
            state.page + 1 >= state.totalPages
        ) {
            return
        }

        val nextPage = state.page + 1
        val activeFilter = state.activeFilter
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isLoadingMore = true,
                    loadMoreErrorMessage = null
                )
            }

            getNotificationsUseCase(
                pageNumber = nextPage,
                pageSize = pageSize,
                unreadOnly = activeFilter == NotificationFilter.Unread
            )
                .onSuccess { response ->
                    val mapped = response.data.mapNotNull { notification ->
                        toNotificationModel(notification)
                    }
                    val resolved = resolveMissingFollowTargets(mapped)
                    _uiState.update { current ->
                        current.copy(
                            notifications = current.notifications + resolved,
                            isLoadingMore = false,
                            loadMoreErrorMessage = null,
                            page = response.pageNumber,
                            totalPages = response.totalPages,
                            totalElements = response.totalElements
                        )
                    }
                }
                .onFailure { throwable ->
                    _uiState.update {
                        it.copy(
                            isLoadingMore = false,
                            loadMoreErrorMessage = throwable.userMessage("Không thể tải thêm thông báo")
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
    val isLoadingMore: Boolean = false,
    val isMarkingAllRead: Boolean = false,
    val page: Int = 0,
    val totalPages: Int = 0,
    val totalElements: Long = 0L,
    val errorMessage: String? = null,
    val loadMoreErrorMessage: String? = null
) {
    val hasMore: Boolean
        get() = page + 1 < totalPages
}

enum class NotificationFilter(val label: String) {
    All("Tất cả"),
    Unread("Chưa đọc")
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

private const val NOTIFICATIONS_PAGE_SIZE = 10
