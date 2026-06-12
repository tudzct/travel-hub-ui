package com.mobile.travelhub.ui.screens

import androidx.compose.ui.res.stringResource
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.PersonAdd
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.compose.ui.tooling.preview.Preview
import com.mobile.travelhub.ui.theme.TravelHubTheme
import com.mobile.travelhub.ui.components.SkeletonBlock
import com.mobile.travelhub.ui.components.PillFilterChip
import java.time.Instant
import java.time.Duration
import com.mobile.travelhub.viewmodels.NotificationFilter
import com.mobile.travelhub.viewmodels.NotificationModel
import com.mobile.travelhub.viewmodels.NotificationType
import com.mobile.travelhub.viewmodels.NotificationsViewModel
import com.mobile.travelhub.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationsPopup(
    onDismiss: () -> Unit,
    onPostNotificationClick: (Long) -> Unit = {},
    onFollowNotificationClick: (Long) -> Unit = {},
    viewModel: NotificationsViewModel = hiltViewModel()
) {
    LaunchedEffect(Unit) {
        viewModel.refreshNotifications()
    }

    val uiState by viewModel.uiState.collectAsState()
    NotificationsPopupContent(
        activeFilter = uiState.activeFilter,
        notifications = uiState.notifications,
        isLoading = uiState.isLoading,
        isLoadingMore = uiState.isLoadingMore,
        isMarkingAllRead = uiState.isMarkingAllRead,
        hasMore = uiState.hasMore,
        loadMoreErrorMessage = uiState.loadMoreErrorMessage,
        onDismiss = onDismiss,
        onPostNotificationClick = onPostNotificationClick,
        onFollowNotificationClick = onFollowNotificationClick,
        onFilterSelected = viewModel::setFilter,
        onMarkAllRead = viewModel::markAllRead,
        onLoadMore = viewModel::loadMoreNotifications
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NotificationsPopupContent(
    activeFilter: NotificationFilter,
    notifications: List<NotificationModel>,
    isLoading: Boolean,
    isLoadingMore: Boolean,
    isMarkingAllRead: Boolean,
    hasMore: Boolean,
    loadMoreErrorMessage: String?,
    onDismiss: () -> Unit,
    onPostNotificationClick: (Long) -> Unit,
    onFollowNotificationClick: (Long) -> Unit,
    onFilterSelected: (NotificationFilter) -> Unit,
    onMarkAllRead: () -> Unit,
    onLoadMore: () -> Unit
) {
    val filteredNotifications = notifications
        .filter { notification ->
            when (activeFilter) {
                NotificationFilter.All -> true
                NotificationFilter.Unread -> !notification.isRead
            }
        }
    var isPopupVisible by remember { mutableStateOf(true) }

    val unreadNotifications = filteredNotifications.filter { !it.isRead }
    val readNotifications = filteredNotifications.filter { it.isRead }
    val screenHeight = LocalConfiguration.current.screenHeightDp.dp

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = screenHeight * 0.85f)
        ) {
            PopupTopBar(
                onClose = onDismiss,
                hasUnreadNotifications = notifications.any { !it.isRead },
                isMarkingAllRead = isMarkingAllRead,
                onMarkAllRead = onMarkAllRead
            )
            NotificationFilters(
                activeFilter = activeFilter,
                onFilterSelected = onFilterSelected
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                if (isLoading) {
                    NotificationsListSkeleton()
                } else if (filteredNotifications.isEmpty()) {
                    if (isPopupVisible) {
                        EmptyNotificationsPopup(onDismiss = { isPopupVisible = false })
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        if (unreadNotifications.isNotEmpty()) {
                            items(unreadNotifications, key = { "${it.createdAt}-${it.title}" }) { notification ->
                                NotificationCard(
                                    notification = notification,
                                    onPostNotificationClick = onPostNotificationClick,
                                    onFollowNotificationClick = onFollowNotificationClick
                                )
                            }
                        }
                        if (unreadNotifications.isNotEmpty() && readNotifications.isNotEmpty()) {
                            item {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    HorizontalDivider(
                                        modifier = Modifier.weight(1f),
                                        color = MaterialTheme.colorScheme.outlineVariant
                                    )
                                    Text(
                                        text = stringResource(R.string.ui_19469f4569),
                                        fontSize = 13.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.padding(horizontal = 12.dp)
                                    )
                                    HorizontalDivider(
                                        modifier = Modifier.weight(1f),
                                        color = MaterialTheme.colorScheme.outlineVariant
                                    )
                                }
                            }
                        }
                        if (readNotifications.isNotEmpty()) {
                            items(readNotifications, key = { "${it.createdAt}-${it.title}" }) { notification ->
                                NotificationCard(
                                    notification = notification,
                                    onPostNotificationClick = onPostNotificationClick,
                                    onFollowNotificationClick = onFollowNotificationClick
                                )
                            }
                        }
                        if (!loadMoreErrorMessage.isNullOrBlank()) {
                            item(key = "notification-load-more-error") {
                                Text(
                                    text = loadMoreErrorMessage,
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 4.dp),
                                    color = MaterialTheme.colorScheme.error,
                                    fontSize = 12.sp
                                )
                            }
                        }
                        if (hasMore) {
                            item(key = "notification-load-more") {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (isLoadingMore) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(24.dp),
                                            strokeWidth = 2.dp
                                        )
                                    } else {
                                        TextButton(onClick = onLoadMore) {
                                            Text(stringResource(R.string.ui_dfe60ca92e))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun NotificationsScreenPreview() {
    TravelHubTheme {
        NotificationsPopupContent(
            activeFilter = NotificationFilter.All,
            notifications = listOf(
                NotificationModel(
                    title = stringResource(R.string.ui_0d7f061615),
                    body = "Your Hanoi weekend trip starts in 2 days.",
                    isRead = false,
                    createdAt = Instant.parse("2026-05-12T09:15:00Z"),
                    type = NotificationType.COMMENT,
                    targetId = 10
                ),
                NotificationModel(
                    title = stringResource(R.string.ui_8021d65119),
                    body = "Linh Nguyen started following you.",
                    isRead = false,
                    createdAt = Instant.parse("2026-05-12T08:40:00Z"),
                    type = NotificationType.FOLLOW,
                    targetId = null
                ),
                NotificationModel(
                    title = stringResource(R.string.ui_bc0792d8dc),
                    body = "Your profile is 90% complete. Add a bio to finish it.",
                    isRead = true,
                    createdAt = Instant.parse("2026-05-11T15:30:00Z"),
                    type = NotificationType.LIKE,
                    targetId = 11
                ),
                NotificationModel(
                    title = stringResource(R.string.ui_abb47e9d75),
                    body = "Your profile is 90% complete. Add a bio to finish it.",
                    isRead = true,
                    createdAt = Instant.parse("2026-05-11T15:30:00Z"),
                    type = NotificationType.FOLLOW,
                    targetId = null
                ),
                NotificationModel(
                    title = stringResource(R.string.ui_1d6aac0974),
                    body = "Your profile is 90% complete. Add a bio to finish it.",
                    isRead = true,
                    createdAt = Instant.parse("2026-05-11T15:30:00Z"),
                    type = NotificationType.FOLLOW,
                    targetId = null
                ),
                NotificationModel(
                    title = stringResource(R.string.ui_2d1c64fe48),
                    body = "Your profile is 90% complete. Add a bio to finish it.",
                    isRead = true,
                    createdAt = Instant.parse("2026-05-11T15:30:00Z"),
                    type = NotificationType.FOLLOW,
                    targetId = null
                ),
                NotificationModel(
                    title = stringResource(R.string.ui_c22cd9d2ce),
                    body = "Your profile is 90% complete. Add a bio to finish it.",
                    isRead = true,
                    createdAt = Instant.parse("2026-05-11T15:30:00Z"),
                    type = NotificationType.FOLLOW,
                    targetId = null
                ),
                NotificationModel(
                    title = stringResource(R.string.ui_0a409dce1f),
                    body = "Your profile is 90% complete. Add a bio to finish it.",
                    isRead = true,
                    createdAt = Instant.parse("2026-05-11T15:30:00Z"),
                    type = NotificationType.FOLLOW,
                    targetId = null
                ),
            ),
            isLoading = false,
            isLoadingMore = false,
            hasMore = false,
            loadMoreErrorMessage = null,
            onDismiss = {},
            onPostNotificationClick = {},
            onFollowNotificationClick = {},
            onFilterSelected = {},
            isMarkingAllRead = false,
            onMarkAllRead = {},
            onLoadMore = {}
        )
    }
}

@Composable
private fun NotificationsListSkeleton() {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(6, contentType = { "notification-skeleton" }) { index ->
            NotificationCardSkeleton(isHighlighted = index < 3)
        }
    }
}

@Composable
private fun NotificationCardSkeleton(
    isHighlighted: Boolean
) {
    Surface(
        shape = RoundedCornerShape(18.dp),
        color = if (isHighlighted) {
            MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
        } else {
            MaterialTheme.colorScheme.surface
        },
        tonalElevation = if (isHighlighted) 2.dp else 0.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            SkeletonBlock(
                modifier = Modifier.size(44.dp),
                shape = CircleShape
            )

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(7.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    SkeletonBlock(
                        modifier = Modifier
                            .fillMaxWidth(0.5f)
                            .height(15.dp),
                        shape = RoundedCornerShape(5.dp)
                    )
                    if (isHighlighted) {
                        SkeletonBlock(
                            modifier = Modifier.size(7.dp),
                            shape = CircleShape
                        )
                    }
                }
                SkeletonBlock(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(12.dp),
                    shape = RoundedCornerShape(4.dp)
                )
                SkeletonBlock(
                    modifier = Modifier
                        .fillMaxWidth(0.72f)
                        .height(12.dp),
                    shape = RoundedCornerShape(4.dp)
                )
            }

            SkeletonBlock(
                modifier = Modifier
                    .size(width = 36.dp, height = 11.dp),
                shape = RoundedCornerShape(4.dp)
            )
        }
    }
}

@Composable
private fun PopupTopBar(
    onClose: () -> Unit,
    hasUnreadNotifications: Boolean,
    isMarkingAllRead: Boolean,
    onMarkAllRead: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = stringResource(R.string.ui_753a22b2eb),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            TextButton(
                onClick = onMarkAllRead,
                enabled = hasUnreadNotifications && !isMarkingAllRead
            ) {
                Text(text = stringResource(R.string.ui_8958e22c23))
            }
        }
    }
}

@Composable
private fun NotificationFilters(
    activeFilter: NotificationFilter,
    onFilterSelected: (NotificationFilter) -> Unit
) {
    val filters = listOf(
        NotificationFilter.All,
        NotificationFilter.Unread
    )

    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(filters, key = { it.name }) { filter ->
            PillFilterChip(
                selected = filter == activeFilter,
                onClick = { onFilterSelected(filter) },
                label = filter.label
            )
        }
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier.padding(horizontal = 4.dp, vertical = 4.dp)
    )
}

@Composable
private fun NotificationCard(
    notification: NotificationModel,
    onPostNotificationClick: (Long) -> Unit,
    onFollowNotificationClick: (Long) -> Unit
) {
    val onNotificationClick: (() -> Unit)? = notification.targetId?.let { targetId ->
        when (notification.type) {
            NotificationType.COMMENT,
            NotificationType.LIKE -> {
                { onPostNotificationClick(targetId) }
            }
            NotificationType.FOLLOW -> {
                { onFollowNotificationClick(targetId) }
            }
            null -> null
        }
    }
    val containerColor = if (!notification.isRead) {
        MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
    } else {
        MaterialTheme.colorScheme.surface
    }
    val indicatorColor = if (!notification.isRead) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.outlineVariant
    }

    val icon = when (notification.type) {
        NotificationType.COMMENT -> Icons.Outlined.ChatBubbleOutline
        NotificationType.LIKE -> Icons.Outlined.FavoriteBorder
        NotificationType.FOLLOW -> Icons.Outlined.PersonAdd
        null -> Icons.Outlined.Notifications
    }

    Surface(
        shape = RoundedCornerShape(18.dp),
        color = containerColor,
        tonalElevation = if (!notification.isRead) 2.dp else 0.dp,
        modifier = Modifier
            .fillMaxWidth()
            .then(
                onNotificationClick?.let { Modifier.clickable(onClick = it) } ?: Modifier
            )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = notification.title,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    if (!notification.isRead) {
                        Box(
                            modifier = Modifier
                                .size(7.dp)
                                .clip(CircleShape)
                                .background(indicatorColor)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = notification.body,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Text(
                text = formatNotificationTime(notification.createdAt),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun EmptyNotificationsPopup(onDismiss: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 28.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
//        Box(
//            modifier = Modifier
//                .padding(bottom = 18.dp)
//                .size(width = 48.dp, height = 4.dp)
//                .clip(RoundedCornerShape(999.dp))
//                .background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.45f))
//                .clickable(onClick = onDismiss)
//        )
        Box(
            modifier = Modifier
                .size(92.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Outlined.Notifications,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(38.dp)
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = stringResource(R.string.ui_b08626f186),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = stringResource(R.string.ui_c1a4aa2a15),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

private fun formatNotificationTime(createdAt: Instant): String {
    val now = Instant.now()
    val minutes = Duration.between(createdAt, now).toMinutes()
    if (minutes <= 0) {
        return "Just now"
    }
    if (minutes < 60) {
        return "${minutes} ${pluralize(minutes, "min", "mins")} ago"
    }
    val hours = minutes / 60
    if (hours < 24) {
        return "${hours} ${pluralize(hours, "hour", "hours")} ago"
    }
    val days = hours / 24
    return "${days} ${pluralize(days, "day", "days")} ago"
}

private fun pluralize(value: Long, singular: String, plural: String): String {
    return if (value == 1L) singular else plural
}
