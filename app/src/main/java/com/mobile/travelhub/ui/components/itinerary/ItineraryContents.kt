package com.mobile.travelhub.ui.components.itinerary

import androidx.compose.ui.res.stringResource
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
import androidx.compose.ui.graphics.compositeOver
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
import com.mobile.travelhub.R


@Composable
fun ItineraryOverviewContent(
    state: ItineraryUiState,
    paddingValues: PaddingValues,
    onOpenDayDetail: (Int) -> Unit,
    onEditEvent: (ItineraryEvent) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(SurfaceBg)
            .padding(paddingValues),
        contentPadding = PaddingValues(start = 24.dp, end = 24.dp, top = 4.dp, bottom = 112.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
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
                    isCompleted = state.isCompleted,
                    onOpenDayDetail = { onOpenDayDetail(day.dayIndex) },
                    onEditEvent = onEditEvent
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
                        shadowElevation = 0.dp
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = if (state.isCompleted) Icons.Default.Lock else Icons.Default.Edit,
                                contentDescription = null,
                                tint = OnSurfaceVariant.copy(alpha = if (state.isCompleted) 0.4f else 0.55f),
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = if (state.isCompleted) "Chuyến đi đã hoàn thành (Chế độ chỉ xem)" else "Nhấn giữ để chỉnh sửa",
                                color = OnSurfaceVariant.copy(alpha = if (state.isCompleted) 0.5f else 0.72f),
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
//                    label = stringResource(R.string.ui_f7a578dcbd),
//                    selected = true,
//                    icon = Icons.Default.TravelExplore
//                )
//                ItineraryFilterChip(
//                    label = stringResource(R.string.ui_ce7eab0a1e),
//                    selected = false,
//                    icon = Icons.Default.PersonAdd
//                )
//                ItineraryFilterChip(
//                    label = stringResource(R.string.ui_b8a7951b65),
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
//                    color = SurfaceContainerLowest
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
//                            text = stringResource(R.string.ui_09ca55ca52),
//                            color = OnSurfaceVariant,
//                            fontSize = 12.sp,
//                            fontWeight = FontWeight.SemiBold
//                        )
//                    }
//                }
//
//                Surface(
//                    shape = CircleShape,
//                    color = SurfaceContainerLowest
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
                text = stringResource(R.string.ui_c0288bbf6f),
                color = OnSurface,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 21.sp
            )

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ItineraryStatPill(
                    label = stringResource(R.string.day_count, dayCount),
                    active = false,
                    icon = Icons.Default.CalendarMonth
                )
                ItineraryStatPill(
                    label = stringResource(R.string.stop_count, stopCount),
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
        shadowElevation = 0.dp
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
            modifier = Modifier.padding(horizontal = 9.dp, vertical = 6.dp),
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
    isCompleted: Boolean,
    onOpenDayDetail: () -> Unit,
    onEditEvent: (ItineraryEvent) -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        color = itineraryDayContainerColor(),
        shadowElevation = 0.dp
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
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

                Spacer(modifier = Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = day.dateLabel.ifBlank { day.label },
                        color = OnSurface,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 15.sp
                    )
                    Text(
                        text = stringResource(R.string.place_count, day.events.size),
                        color = OnSurfaceVariant,
                        fontWeight = FontWeight.Medium,
                        fontSize = 12.sp
                    )
                }

                Surface(
                    modifier = Modifier.clickable(onClick = onOpenDayDetail),
                    shape = RoundedCornerShape(14.dp),
                    color = itineraryMapButtonContainerColor()
                ) {
                    Icon(
                        imageVector = Icons.Default.Map,
                        contentDescription = stringResource(R.string.ui_dafc3f8f08),
                        tint = PrimaryBlue,
                        modifier = Modifier
                            .size(34.dp)
                            .padding(7.dp)
                    )
                }
            }

            if (day.events.isEmpty()) {
                ItineraryTimelineEventCard(
                    title = stringResource(R.string.ui_430558390f),
                    timeRange = "Thêm điểm đến cho ngày này",
                    badge = null,
                    onClick = onOpenDayDetail
                )
            } else {
                day.events.forEachIndexed { eventIndex, event ->
                    ItineraryTimelineEventCard(
                        title = event.title.ifBlank { event.placeName.ifBlank { "Địa điểm ${eventIndex + 1}" } },
                        timeRange = "${event.startTime} - ${event.endTime}",
                        placeName = event.placeName,
                        badge = null,
                        onClick = onOpenDayDetail,
                        onLongClick = if (isCompleted) null else { { onEditEvent(event) } }
                    )
                }
            }
        }
    }
}

@Composable
@OptIn(ExperimentalFoundationApi::class)
private fun ItineraryTimelineEventCard(
    title: String,
    timeRange: String,
    placeName: String = "",
    badge: Int?,
    onClick: () -> Unit,
    onLongClick: (() -> Unit)? = null
) {
    val hapticFeedback = LocalHapticFeedback.current

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
//                color = SurfaceContainerLowest
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

        Spacer(modifier = Modifier.width(8.dp))

        Surface(
            modifier = Modifier
                .weight(1f)
                .combinedClickable(
                    onClick = onClick,
                    onLongClick = onLongClick?.let { edit ->
                        {
                            hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                            edit()
                        }
                    }
                ),
            shape = RoundedCornerShape(24.dp),
            color = itineraryEventContainerColor(),
            shadowElevation = 0.dp
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 18.dp, vertical = 10.dp),
                verticalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                Text(
                    text = title,
                    color = OnSurface,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 14.sp
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    ActivityTimeChip(timeRange = timeRange)
                    ActivityPlaceChip(placeName = placeName)
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
            .background(SurfaceBg)
            .padding(paddingValues),
        state = lazyListState,
        contentPadding = PaddingValues(start = 24.dp, end = 24.dp, top = 6.dp, bottom = 112.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
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

                        Spacer(modifier = Modifier.width(8.dp))

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
                                            contentDescription = stringResource(R.string.ui_860fd0e43c),
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
                    title = stringResource(R.string.ui_982a83e6f2),
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

@Composable
private fun itineraryDayContainerColor(): Color {
    return PrimaryBlue.copy(alpha = if (isDarkTheme) 0.14f else 0.06f)
        .compositeOver(SurfaceContainerLow)
}

@Composable
private fun itineraryMapButtonContainerColor(): Color {
    return PrimaryBlue.copy(alpha = if (isDarkTheme) 0.18f else 0.10f)
        .compositeOver(SurfaceContainerLowest)
}

@Composable
private fun itineraryEventContainerColor(): Color {
    return PrimaryBlue.copy(alpha = if (isDarkTheme) 0.08f else 0.03f)
        .compositeOver(SurfaceContainerLowest)
}
