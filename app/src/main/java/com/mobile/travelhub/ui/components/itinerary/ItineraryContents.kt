package com.mobile.travelhub.ui.components.itinerary

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mobile.travelhub.data.model.*
import com.mobile.travelhub.ui.theme.*
import kotlin.math.max
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyListState
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import com.mobile.travelhub.ui.components.modifiers.shimmerEffect
import com.mobile.travelhub.viewmodels.ItineraryUiState


@Composable
fun ItineraryOverviewContent(
    state: ItineraryUiState,
    paddingValues: PaddingValues,
    onOpenDayDetail: (Int) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(SurfaceBg)
            .padding(paddingValues),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 120.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            ItineraryTimelineHeader(
                dayCount = state.days.size,
                stopCount = state.days.sumOf { it.events.size }
            )
        }

        if (state.isLoadingActivities) {
            item {
                ItineraryOverviewSkeleton()
            }
        } else if (state.days.isEmpty()) {
            item {
                EmptyOverviewCard()
            }
        } else {
            itemsIndexed(state.days, key = { _, day -> day.dayIndex }) { index, day ->
                ItineraryTimelineDay(
                    day = day,
                    index = index,
                    isLast = index == state.days.lastIndex,
                    onOpenDayDetail = { onOpenDayDetail(day.dayIndex) }
                )
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    Surface(
                        shape = RoundedCornerShape(999.dp),
                        color = SurfaceContainerLowest.copy(alpha = 0.86f),
                        shadowElevation = 1.dp
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 9.dp),
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = null,
                                tint = OnSurfaceVariant.copy(alpha = 0.55f),
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = "Long press a place to edit",
                                color = OnSurfaceVariant.copy(alpha = 0.72f),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ItineraryTimelineHeader(
    dayCount: Int,
    stopCount: Int
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
//        Row(
//            modifier = Modifier.fillMaxWidth(),
//            horizontalArrangement = Arrangement.SpaceBetween,
//            verticalAlignment = Alignment.CenterVertically
//        ) {
//            Row(
//                modifier = Modifier
//                    .weight(1f)
//                    .horizontalScroll(rememberScrollState()),
//                horizontalArrangement = Arrangement.spacedBy(8.dp)
//            ) {
//                ItineraryFilterChip(
//                    label = "Tất cả",
//                    selected = true,
//                    icon = Icons.Default.TravelExplore
//                )
//                ItineraryFilterChip(
//                    label = "Nhóm",
//                    selected = false,
//                    icon = Icons.Default.PersonAdd
//                )
//                ItineraryFilterChip(
//                    label = "Cá nhân",
//                    selected = false,
//                    icon = Icons.Default.Person
//                )
//            }
//
//            Spacer(modifier = Modifier.width(8.dp))
//
//            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
//                Surface(
//                    shape = RoundedCornerShape(999.dp),
//                    color = SurfaceContainerLowest,
//                    shadowElevation = 1.dp
//                ) {
//                    Row(
//                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
//                        horizontalArrangement = Arrangement.spacedBy(6.dp),
//                        verticalAlignment = Alignment.CenterVertically
//                    ) {
//                        Icon(
//                            imageVector = Icons.Default.Share,
//                            contentDescription = null,
//                            tint = OnSurfaceVariant,
//                            modifier = Modifier.size(14.dp)
//                        )
//                        Text(
//                            text = "Share",
//                            color = OnSurfaceVariant,
//                            fontSize = 12.sp,
//                            fontWeight = FontWeight.SemiBold
//                        )
//                    }
//                }
//
//                Surface(
//                    shape = CircleShape,
//                    color = SurfaceContainerLowest,
//                    shadowElevation = 1.dp
//                ) {
//                    Icon(
//                        imageVector = Icons.Default.MoreVert,
//                        contentDescription = null,
//                        tint = OnSurfaceVariant,
//                        modifier = Modifier
//                            .size(32.dp)
//                            .padding(7.dp)
//                    )
//                }
//            }
//        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Tất cả lịch trình",
                color = OnSurface,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 21.sp
            )

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ItineraryStatPill(
                    label = "$dayCount ngày",
                    active = false,
                    icon = Icons.Default.CalendarMonth
                )
                ItineraryStatPill(
                    label = "$stopCount điểm",
                    active = true,
                    icon = Icons.Default.Place
                )
            }
        }
    }
}

@Composable
private fun ItineraryFilterChip(
    label: String,
    selected: Boolean,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    val background = if (selected) PrimaryBlue else SurfaceContainerLowest
    val contentColor = if (selected) Color.White else OnSurfaceVariant
    Surface(
        shape = RoundedCornerShape(999.dp),
        color = background,
        shadowElevation = if (selected) 3.dp else 1.dp
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(5.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = contentColor,
                modifier = Modifier.size(13.dp)
            )
            Text(
                text = label,
                color = contentColor,
                fontSize = 12.sp,
                fontWeight = FontWeight.ExtraBold
            )
        }
    }
}

@Composable
private fun ItineraryStatPill(
    label: String,
    active: Boolean,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    val background = if (active) PrimaryBlue.copy(alpha = 0.16f) else SurfaceContainerLowest
    val contentColor = if (active) PrimaryBlue else OnSurfaceVariant
    Surface(
        shape = RoundedCornerShape(999.dp),
        color = background
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
            horizontalArrangement = Arrangement.spacedBy(5.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = contentColor,
                modifier = Modifier.size(13.dp)
            )
            Text(
                text = label,
                color = contentColor,
                fontSize = 12.sp,
                fontWeight = FontWeight.ExtraBold
            )
        }
    }
}

@Composable
private fun ItineraryTimelineDay(
    day: ItineraryDay,
    index: Int,
    isLast: Boolean,
    onOpenDayDetail: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.Center
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .background(PrimaryBlue, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = (index + 1).toString(),
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = day.dateLabel.ifBlank { day.label },
                        color = OnSurface,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 13.sp
                    )
                    Text(
                        text = "${day.events.size} địa điểm",
                        color = OnSurfaceVariant,
                        fontWeight = FontWeight.Medium,
                        fontSize = 12.sp
                    )
                }

                Surface(
                    modifier = Modifier.clickable(onClick = onOpenDayDetail),
                    shape = RoundedCornerShape(12.dp),
                    color = PrimaryBlue.copy(alpha = 0.14f)
                ) {
                    Icon(
                        imageVector = Icons.Default.Map,
                        contentDescription = "Open day detail",
                        tint = PrimaryBlue,
                        modifier = Modifier
                            .size(34.dp)
                            .padding(8.dp)
                    )
                }
            }

            if (day.events.isEmpty()) {
                ItineraryTimelineEventCard(
                    title = "Chưa có địa điểm",
                    timeRange = "Thêm điểm đến cho ngày này",
                    badge = null,
                    onClick = onOpenDayDetail
                )
            } else {
                day.events.forEachIndexed { eventIndex, event ->
                    ItineraryTimelineEventCard(
                        title = event.title.ifBlank { event.placeName.ifBlank { "Địa điểm ${eventIndex + 1}" } },
                        timeRange = "${event.startTime} - ${event.endTime}",
                        badge = eventIndex + 1,
                        onClick = onOpenDayDetail
                    )
                }
            }
        }
    }
}

@Composable
private fun ItineraryTimelineEventCard(
    title: String,
    timeRange: String,
    badge: Int?,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier.width(28.dp),
            contentAlignment = Alignment.Center
        ) {
//            Surface(
//                shape = CircleShape,
//                color = SurfaceContainerLowest,
//                shadowElevation = 3.dp
//            ) {
//                Box(
//                    modifier = Modifier
//                        .size(28.dp)
//                        .padding(6.dp)
//                        .background(OnSurfaceVariant.copy(alpha = 0.45f), CircleShape)
//                )
//            }
            if (badge != null) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .size(22.dp)
                        .background(PrimaryBlue, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = badge.toString(),
                        color = Color.White,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.width(10.dp))

        Surface(
            modifier = Modifier
                .weight(1f)
                .clickable(onClick = onClick),
            shape = RoundedCornerShape(24.dp),
            color = SurfaceContainerLowest,
            shadowElevation = 6.dp
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 13.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = title,
                    color = OnSurface,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 14.sp
                )
                Surface(
                    shape = RoundedCornerShape(999.dp),
                    color = PrimaryBlue.copy(alpha = 0.18f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 9.dp),
                        horizontalArrangement = Arrangement.spacedBy(5.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Schedule,
                            contentDescription = null,
                            tint = PrimaryBlue,
                            modifier = Modifier.size(12.dp)
                        )
                        Text(
                            text = timeRange,
                            color = PrimaryBlue,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Row(
                        modifier = Modifier.padding(horizontal = 9.dp),
                        horizontalArrangement = Arrangement.spacedBy(5.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Schedule,
                            contentDescription = null,
                            tint = PrimaryBlue,
                            modifier = Modifier.size(12.dp)
                        )
                        Text(
                            text = timeRange,
                            color = PrimaryBlue,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ItineraryDayDetailContent(
    day: ItineraryDay?,
    isLoading: Boolean,
    isEditMode: Boolean,
    paddingValues: PaddingValues,
    onAddStop: () -> Unit,
    onReorderEvents: (Int, Int) -> Unit,
    onEditEvent: (ItineraryEvent) -> Unit,
    onDeleteEvent: (String) -> Unit
) {
    val lazyListState = rememberLazyListState()
    val hapticFeedback = LocalHapticFeedback.current
    val eventCount = day?.events?.size ?: 0
    val headerItemCount = if (day == null) 0 else 1
    val reorderableState = rememberReorderableLazyListState(lazyListState) { from, to ->
        val fromIndex = from.index - headerItemCount
        val toIndex = (to.index - headerItemCount).coerceIn(0, max(0, eventCount - 1))
        if (fromIndex in 0 until eventCount && toIndex in 0 until eventCount) {
            onReorderEvents(fromIndex, toIndex)
            hapticFeedback.performHapticFeedback(HapticFeedbackType.SegmentFrequentTick)
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues),
        state = lazyListState,
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 12.dp, bottom = 120.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        if (isLoading) {
            item {
                ItineraryDayDetailSkeleton()
            }
        } else if (day != null) {
            item {
                DayContinuousTimelineCard(day = day)
            }
        }
        if (!isLoading && (day == null || day.events.isEmpty())) {
            item {
                EmptyDayCard()
            }
        } else if (!isLoading && day != null) {
            itemsIndexed(day.events, key = { _, it -> it.eventId }) { index, event ->
                ReorderableItem(reorderableState, key = event.eventId) { isDragging ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(IntrinsicSize.Min)
                    ) {
                        // Timeline decoration
                        Column(
                            modifier = Modifier
                                .width(36.dp)
                                .fillMaxHeight(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            // Top line segment (except for first item)
                            Box(
                                modifier = Modifier
                                    .width(2.dp)
                                    .height(12.dp)
                                    .background(if (index == 0) Color.Transparent else OnSurfaceVariant.copy(alpha = 0.15f))
                            )
                            
                            // Icon dot
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .background(event.colorHex.toItineraryColor().copy(alpha = 0.12f), CircleShape)
                                    .border(1.5.dp, event.colorHex.toItineraryColor().copy(alpha = 0.3f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = getItineraryIcon(event.iconName),
                                    contentDescription = null,
                                    tint = event.colorHex.toItineraryColor(),
                                    modifier = Modifier.size(16.dp)
                                )
                            }

                            // Bottom line segment (except for last item)
                            Box(
                                modifier = Modifier
                                    .width(2.dp)
                                    .fillMaxHeight()
                                    .background(if (index == day.events.size - 1) Color.Transparent else OnSurfaceVariant.copy(alpha = 0.15f))
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        DayEventCard(
                            event = event,
                            isDragging = isDragging,
                            isEditMode = isEditMode,
                            dragHandle = {
                                if (isEditMode) {
                                    IconButton(
                                        onClick = {},
                                        modifier = with(this) {
                                            Modifier.draggableHandle(
                                                onDragStarted = {
                                                    hapticFeedback.performHapticFeedback(
                                                        HapticFeedbackType.GestureThresholdActivate
                                                    )
                                                },
                                                onDragStopped = {
                                                    hapticFeedback.performHapticFeedback(HapticFeedbackType.GestureEnd)
                                                }
                                            )
                                        }
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.DragIndicator,
                                            contentDescription = "Reorder event",
                                            tint = OnSurfaceVariant
                                        )
                                    }
                                }
                            },
                            onEdit = { onEditEvent(event) },
                            onDelete = { onDeleteEvent(event.eventId) }
                        )
                    }
                }
            }
        }
        if (!isLoading && day != null && isEditMode) {
            item {
                AddActionCard(
                    title = "Add stop",
                    onClick = onAddStop
                )
            }
        }
    }
}

@Composable
private fun ItineraryOverviewSkeleton() {
    Column(verticalArrangement = Arrangement.spacedBy(18.dp)) {
        repeat(3) { index ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .background(SurfaceContainerLow, CircleShape)
                        .shimmerEffect()
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(if (index == 0) 0.55f else 0.42f)
                            .height(16.dp)
                            .background(SurfaceContainerLow, RoundedCornerShape(6.dp))
                            .shimmerEffect()
                    )
                    repeat(2) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(58.dp)
                                .background(SurfaceContainerLowest, RoundedCornerShape(20.dp))
                                .shimmerEffect()
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ItineraryDayDetailSkeleton() {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(150.dp)
                .background(SurfaceContainerLowest, RoundedCornerShape(24.dp))
                .shimmerEffect()
        )
        repeat(4) {
            Row(modifier = Modifier.fillMaxWidth()) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .background(SurfaceContainerLow, CircleShape)
                        .shimmerEffect()
                )
                Spacer(modifier = Modifier.width(12.dp))
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(92.dp)
                        .background(SurfaceContainerLowest, RoundedCornerShape(22.dp))
                        .shimmerEffect()
                )
            }
        }
    }
}
