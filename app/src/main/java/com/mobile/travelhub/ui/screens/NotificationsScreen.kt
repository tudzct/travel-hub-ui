package com.mobile.travelhub.ui.screens

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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
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
import com.mobile.travelhub.ui.theme.SurfaceBg
import com.mobile.travelhub.ui.theme.TravelHubTheme
import com.mobile.travelhub.ui.components.SkeletonBlock
import java.time.Instant
import java.time.Duration
import com.mobile.travelhub.viewmodels.NotificationFilter
import com.mobile.travelhub.viewmodels.NotificationModel
import com.mobile.travelhub.viewmodels.NotificationType
import com.mobile.travelhub.viewmodels.NotificationsViewModel

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
        isMarkingAllRead = uiState.isMarkingAllRead,
        onDismiss = onDismiss,
        onPostNotificationClick = onPostNotificationClick,
        onFollowNotificationClick = onFollowNotificationClick,
        onFilterSelected = viewModel::setFilter,
        onMarkAllRead = viewModel::markAllRead
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NotificationsPopupContent(
    activeFilter: NotificationFilter,
    notifications: List<NotificationModel>,
    isLoading: Boolean,
    isMarkingAllRead: Boolean,
    onDismiss: () -> Unit,
    onPostNotificationClick: (Long) -> Unit,
    onFollowNotificationClick: (Long) -> Unit,
    onFilterSelected: (NotificationFilter) -> Unit,
    onMarkAllRead: () -> Unit
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
        containerColor = SurfaceBg,
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
                                        color = Color.LightGray
                                    )
                                    Text(
                                        text = "New notifications",
                                        fontSize = 13.sp,
                                        color = Color.Gray,
                                        modifier = Modifier.padding(horizontal = 12.dp)
                                    )
                                    HorizontalDivider(
                                        modifier = Modifier.weight(1f),
                                        color = Color.LightGray
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
                    title = "Trip reminder",
                    body = "Your Hanoi weekend trip starts in 2 days.",
                    isRead = false,
                    createdAt = Instant.parse("2026-05-12T09:15:00Z"),
                    type = NotificationType.COMMENT,
                    targetId = 10
                ),
                NotificationModel(
                    title = "New follower",
                    body = "Linh Nguyen started following you.",
                    isRead = false,
                    createdAt = Instant.parse("2026-05-12T08:40:00Z"),
                    type = NotificationType.FOLLOW,
                    targetId = null
                ),
                NotificationModel(
                    title = "System",
                    body = "Your profile is 90% complete. Add a bio to finish it.",
                    isRead = true,
                    createdAt = Instant.parse("2026-05-11T15:30:00Z"),
                    type = NotificationType.LIKE,
                    targetId = 11
                ),
                NotificationModel(
                    title = "New follower 2",
                    body = "Your profile is 90% complete. Add a bio to finish it.",
                    isRead = true,
                    createdAt = Instant.parse("2026-05-11T15:30:00Z"),
                    type = NotificationType.FOLLOW,
                    targetId = null
                ),
                NotificationModel(
                    title = "New follower 3",
                    body = "Your profile is 90% complete. Add a bio to finish it.",
                    isRead = true,
                    createdAt = Instant.parse("2026-05-11T15:30:00Z"),
                    type = NotificationType.FOLLOW,
                    targetId = null
                ),
                NotificationModel(
                    title = "New follower 4",
                    body = "Your profile is 90% complete. Add a bio to finish it.",
                    isRead = true,
                    createdAt = Instant.parse("2026-05-11T15:30:00Z"),
                    type = NotificationType.FOLLOW,
                    targetId = null
                ),
                NotificationModel(
                    title = "New follower 5",
                    body = "Your profile is 90% complete. Add a bio to finish it.",
                    isRead = true,
                    createdAt = Instant.parse("2026-05-11T15:30:00Z"),
                    type = NotificationType.FOLLOW,
                    targetId = null
                ),
                NotificationModel(
                    title = "New follower 6",
                    body = "Your profile is 90% complete. Add a bio to finish it.",
                    isRead = true,
                    createdAt = Instant.parse("2026-05-11T15:30:00Z"),
                    type = NotificationType.FOLLOW,
                    targetId = null
                ),
            ),
            isLoading = false,
            onDismiss = {},
            onPostNotificationClick = {},
            onFollowNotificationClick = {},
            onFilterSelected = {},
            isMarkingAllRead = false,
            onMarkAllRead = {}
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
            text = "Notifications",
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
                Text(text = "Mark all read")
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
            FilterChip(
                selected = filter == activeFilter,
                onClick = { onFilterSelected(filter) },
                shape = RoundedCornerShape(50),
                label = {
                    Text(
                        text = filter.label,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                    selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
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
                .background(Color.LightGray),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Outlined.Notifications,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(38.dp)
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "No notifications",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "You'll be notified when there's activity in the group",
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
