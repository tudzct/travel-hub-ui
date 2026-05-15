package com.mobile.travelhub.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
    private val getNotificationsUseCase: GetNotificationsUseCase
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
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            notifications = mapped,
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
    val type: NotificationType?
)

enum class NotificationType {
    COMMENT,
    LIKE,
    FOLLOW
}

private fun toNotificationModel(response: NotificationResponse): NotificationModel? {
    val createdAt = runCatching { Instant.parse(response.createdAt) }.getOrNull() ?: return null
    val type = response.type
        ?.uppercase()
        ?.let { raw ->
            runCatching { NotificationType.valueOf(raw) }.getOrNull()
        }

    return NotificationModel(
        title = response.title,
        body = response.body,
        isRead = response.isRead,
        createdAt = createdAt,
        type = type
    )
}
