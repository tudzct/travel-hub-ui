package com.mobile.travelhub.ui.components.itinerary

import androidx.compose.ui.res.stringResource
import androidx.compose.animation.*
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mobile.travelhub.data.model.*
import com.mobile.travelhub.ui.theme.*
import kotlin.math.max
import sh.calvin.reorderable.ReorderableItem
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import com.mobile.travelhub.R


@Composable
fun DayContinuousTimelineCard(day: ItineraryDay) {
    val segments = remember(day.events) { buildTimelineSegments(day.events) }

    Card(
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceContainerLowest)
    ) {
        Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Text(
                text = stringResource(R.string.ui_dcc8e9cc22),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.ExtraBold,
                color = OnSurface
            )
            if (segments.isEmpty()) {
                Text(
                    text = stringResource(R.string.ui_fa6b5c7a6f),
                    style = MaterialTheme.typography.bodyMedium,
                    color = OnSurfaceVariant
                )
            } else {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(22.dp)
                        .border(1.dp, PrimaryBlue.copy(alpha = 0.10f), RoundedCornerShape(999.dp))
                        .padding(3.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    segments.forEach { segment ->
                        Box(
                            modifier = Modifier
                                .weight(segment.durationMinutes.toFloat())
                                .fillMaxHeight()
                                .background(
                                    color = segment.color,
                                    shape = RoundedCornerShape(999.dp)
                                )
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = segments.first().startTime,
                        style = MaterialTheme.typography.labelMedium,
                        color = OnSurfaceVariant
                    )
                    Text(
                        text = segments.last().endTime,
                        style = MaterialTheme.typography.labelMedium,
                        color = OnSurfaceVariant
                    )
                }

                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    day.events.sortedBy { parseTimeMinutes(it.startTime) }.forEach { event ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .background(event.displayColor(), RoundedCornerShape(999.dp))
                            )
                            ActivityTimeChip(timeRange = "${event.startTime} - ${event.endTime}")
                            ActivityPlaceChip(placeName = event.placeName)
                            Text(
                                text = event.title,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = OnSurface
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SummaryChip(label: String) {
    Box(
        modifier = Modifier
            .background(
                color = PrimaryBlue.copy(alpha = 0.1f),
                shape = RoundedCornerShape(999.dp)
            )
            .padding(horizontal = 12.dp, vertical = 7.dp)
    ) {
        Text(
            text = label,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = PrimaryBlue
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ItineraryDayCard(
    day: ItineraryDay,
    isEditMode: Boolean,
    onClick: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    var showEvents by remember(day.dayIndex) { mutableStateOf(false) }
    val sortedEvents = remember(day.events) { day.events.sortedBy { parseTimeMinutes(it.startTime) } }
    val timeRange = remember(sortedEvents) {
        if (sortedEvents.isEmpty()) null
        else "${formatDisplayTime(sortedEvents.first().startTime)} — ${formatDisplayTime(sortedEvents.last().endTime)}"
    }

    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = {
            it == SwipeToDismissBoxValue.EndToStart
        }
    )

    LaunchedEffect(dismissState.currentValue) {
        if (dismissState.currentValue == SwipeToDismissBoxValue.EndToStart) {
            onDelete()
            dismissState.snapTo(SwipeToDismissBoxValue.Settled)
        }
    }

    val cardContent = @Composable {
        Card(
            modifier = Modifier
                .fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceContainerLowest),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Column(
                modifier = Modifier
                    .clickable(onClick = onClick)
                    .padding(20.dp)
            ) {
                // Header: "Day X - Date" + events chip
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(
                            R.string.day_date_format,
                            day.label,
                            day.dateLabel
                        ),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = OnSurface,
                        modifier = Modifier.weight(1f)
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (day.events.isNotEmpty()) {
                            Spacer(modifier = Modifier.width(8.dp))
                            Row(
                                modifier = Modifier
                                    .border(
                                        width = 1.dp,
                                        color = OnSurfaceVariant.copy(alpha = 0.3f),
                                        shape = RoundedCornerShape(999.dp)
                                    )
                                    .clickable(
                                        interactionSource = remember { MutableInteractionSource() },
                                        indication = null
                                    ) { showEvents = !showEvents }
                                    .padding(horizontal = 12.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = stringResource(
                                        R.string.event_count,
                                        day.events.size
                                    ),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = OnSurfaceVariant
                                )
                                Icon(
                                    imageVector = if (showEvents) Icons.Default.KeyboardArrowUp
                                    else Icons.Default.KeyboardArrowDown,
                                    contentDescription = if (showEvents) "Collapse" else "Expand",
                                    modifier = Modifier.size(16.dp),
                                    tint = OnSurfaceVariant
                                )
                            }
                        }
                    }
                }

                if (timeRange != null) {
                    Text(
                        text = timeRange,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = PrimaryBlue,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Description — auto-generated from events
                Text(
                    text = if (day.events.isEmpty()) {
                        "No events yet. Tap to start planning this day."
                    } else {
                        day.events.joinToString(", ") { it.title } + "."
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = OnSurfaceVariant,
                    lineHeight = 20.sp
                )

                // Expandable events timeline
                AnimatedVisibility(
                    visible = showEvents && day.events.isNotEmpty(),
                    enter = expandVertically() + fadeIn(),
                    exit = shrinkVertically() + fadeOut()
                ) {
                    Column(modifier = Modifier.padding(top = 16.dp)) {
                        sortedEvents.forEachIndexed { index, event ->
                            Row(modifier = Modifier.height(IntrinsicSize.Min)) {
                                // Left time column
                                Column(
                                    horizontalAlignment = Alignment.Start,
                                    modifier = Modifier.width(80.dp)
                                ) {
                                    Text(
                                        text = formatDisplayTime(event.startTime),
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Normal,
                                        color = OnSurfaceVariant
                                    )
                                }
                                // Vertical line + dot
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier
                                        .fillMaxHeight()
                                        .width(24.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .background(
                                                color = OnSurfaceVariant.copy(alpha = 0.4f),
                                                shape = RoundedCornerShape(999.dp)
                                            )
                                    )
                                    if (index < sortedEvents.size - 1) {
                                        Box(
                                            modifier = Modifier
                                                .width(1.5.dp)
                                                .weight(1f)
                                                .background(OnSurfaceVariant.copy(alpha = 0.2f))
                                        )
                                    }
                                }
                                // Event info
                                Column(
                                    modifier = Modifier
                                        .weight(1f)
                                        .padding(bottom = if (index < sortedEvents.size - 1) 16.dp else 0.dp)
                                ) {
                                    Text(
                                        text = event.title,
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = FontWeight.SemiBold,
                                        color = OnSurface
                                    )
                                    val subtitle = buildString {
                                        if (event.placeName.isNotBlank()) append(event.placeName)
                                        val duration = computeDuration(event.startTime, event.endTime)
                                        if (duration.isNotBlank()) {
                                            if (isNotEmpty()) append(" · ")
                                            append(duration)
                                        }
                                    }
                                    if (subtitle.isNotBlank()) {
                                        Text(
                                            text = subtitle,
                                            fontSize = 12.sp,
                                            color = OnSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (isEditMode) {
        SwipeToDismissBox(
            state = dismissState,
            enableDismissFromStartToEnd = false,
            backgroundContent = {
                val color = when (dismissState.dismissDirection) {
                    SwipeToDismissBoxValue.EndToStart -> SunsetOrange.copy(alpha = 0.8f)
                    else -> Color.Transparent
                }
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(color, RoundedCornerShape(20.dp)),
                    contentAlignment = Alignment.CenterEnd
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = stringResource(R.string.ui_f6fdbe48dc),
                        tint = Color.White,
                        modifier = Modifier.padding(end = 24.dp)
                    )
                }
            },
            content = { cardContent() }
        )
    } else {
        cardContent()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DayEventCard(
    event: ItineraryEvent,
    isDragging: Boolean,
    isEditMode: Boolean,
    dragHandle: @Composable (() -> Unit),
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    var isMenuExpanded by remember(event.eventId) { mutableStateOf(false) }
    var isDetailExpanded by remember(event.eventId) { mutableStateOf(false) }
    val elevation by animateDpAsState(targetValue = if (isDragging) 10.dp else 0.dp, label = "eventCardElevation")
    val accent = event.displayColor()

    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = {
            it == SwipeToDismissBoxValue.EndToStart
        }
    )

    LaunchedEffect(dismissState.currentValue) {
        if (dismissState.currentValue == SwipeToDismissBoxValue.EndToStart) {
            onDelete()
            dismissState.snapTo(SwipeToDismissBoxValue.Settled)
        }
    }

    val cardContent = @Composable {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { isDetailExpanded = !isDetailExpanded },
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceContainerLowest),
            elevation = CardDefaults.cardElevation(defaultElevation = elevation)
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            ActivityTimeChip(
                                timeRange = "${event.startTime} - ${event.endTime}",
                                contentColor = accent
                            )
                            ActivityPlaceChip(placeName = event.placeName)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = event.title,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = OnSurface
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = event.placeName,
                            style = MaterialTheme.typography.bodyMedium,
                            color = OnSurfaceVariant
                        )
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (isEditMode) {
                            IconButton(onClick = onEdit) {
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = stringResource(R.string.ui_8dcd4df0ae),
                                    tint = OnSurfaceVariant,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            dragHandle()
                        }
                    }
                }

                AnimatedVisibility(
                    visible = isDetailExpanded,
                    enter = expandVertically() + fadeIn(),
                    exit = shrinkVertically() + fadeOut()
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            MetaPill(
                                icon = {
                                    Icon(
                                        Icons.Default.Schedule,
                                        null,
                                        tint = OnSurfaceVariant,
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                            ) {
                                Text(
                                    event.estimatedCost.ifBlank { "No cost" },
                                    fontSize = 12.sp,
                                    color = OnSurfaceVariant
                                )
                            }
                            if (event.transportToNext.isNotBlank()) {
                                MetaPill {
                                    Text(event.transportToNext, fontSize = 12.sp, color = OnSurfaceVariant)
                                }
                            }
                        }

                        if (event.note.isNotBlank()) {
                            Text(
                                text = event.note,
                                style = MaterialTheme.typography.bodyMedium,
                                color = OnSurface,
                                lineHeight = 20.sp
                            )
                        }
                    }
                }
            }
        }
    }

    if (isEditMode) {
        SwipeToDismissBox(
            state = dismissState,
            enableDismissFromStartToEnd = false,
            backgroundContent = {
                val color = when (dismissState.dismissDirection) {
                    SwipeToDismissBoxValue.EndToStart -> SunsetOrange.copy(alpha = 0.8f)
                    else -> Color.Transparent
                }
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(color, RoundedCornerShape(28.dp)),
                    contentAlignment = Alignment.CenterEnd
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = stringResource(R.string.ui_f6fdbe48dc),
                        tint = Color.White,
                        modifier = Modifier.padding(end = 20.dp)
                    )
                }
            },
            content = { cardContent() }
        )
    } else {
        cardContent()
    }
}

@Composable
fun MetaPill(
    icon: @Composable (() -> Unit)? = null,
    content: @Composable () -> Unit
) {
    Row(
        modifier = Modifier
            .background(color = SurfaceContainerLow, shape = RoundedCornerShape(999.dp))
            .padding(horizontal = 10.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        icon?.invoke()
        content()
    }
}

@Composable
fun ActivityTimeChip(
    timeRange: String,
    modifier: Modifier = Modifier,
    contentColor: Color = PrimaryBlue
) {
    Row(
        modifier = modifier
            .background(contentColor.copy(alpha = 0.12f), RoundedCornerShape(999.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(
            imageVector = Icons.Default.Schedule,
            contentDescription = null,
            tint = contentColor,
            modifier = Modifier.size(12.dp)
        )
        Text(
            text = timeRange,
            color = contentColor,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            maxLines = 1
        )
    }
}

@Composable
fun ActivityPlaceChip(
    placeName: String,
    modifier: Modifier = Modifier
) {
    if (placeName.isBlank()) return

    Row(
        modifier = modifier
            .widthIn(max = 150.dp)
            .background(PrimaryBlue.copy(alpha = 0.10f), RoundedCornerShape(999.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(
            imageVector = Icons.Outlined.LocationOn,
            contentDescription = null,
            tint = PrimaryBlue,
            modifier = Modifier.size(12.dp)
        )
        Text(
            text = placeName,
            color = OnSurfaceVariant,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
fun EmptyOverviewCard() {
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceContainerLowest)
    ) {
        Text(
            text = stringResource(R.string.ui_9e505d31d1),
            modifier = Modifier.padding(20.dp),
            color = OnSurfaceVariant,
            lineHeight = 20.sp
        )
    }
}

@Composable
fun AddActionCard(
    title: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = PrimaryBlue.copy(alpha = 0.08f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 18.dp, vertical = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .background(Color.White.copy(alpha = 0.85f), RoundedCornerShape(16.dp))
                    .padding(10.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = null, tint = PrimaryBlue)
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = OnSurface
                )
            }
        }
    }
}

@Composable
fun EmptyDayCard() {
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceContainerLowest)
    ) {
        Text(
            text = stringResource(R.string.ui_c687c93a2b),
            modifier = Modifier.padding(20.dp),
            color = OnSurfaceVariant,
            lineHeight = 20.sp
        )
    }
}



data class TimelineSegmentUi(
    val startTime: String,
    val endTime: String,
    val durationMinutes: Int,
    val color: Color
)

private fun buildTimelineSegments(events: List<ItineraryEvent>): List<TimelineSegmentUi> {
    if (events.isEmpty()) return emptyList()
    val sorted = events.sortedBy { parseTimeMinutes(it.startTime) }
    val segments = mutableListOf<TimelineSegmentUi>()
    var cursor = parseTimeMinutes(sorted.first().startTime)

    sorted.forEach { event ->
        val eventStart = parseTimeMinutes(event.startTime)
        val eventEnd = parseTimeMinutes(event.endTime)
        if (eventStart > cursor) {
            segments += TimelineSegmentUi(
                startTime = minutesToTime(cursor),
                endTime = minutesToTime(eventStart),
                durationMinutes = max(15, eventStart - cursor),
                color = SurfaceContainerLow
            )
        }
        segments += TimelineSegmentUi(
            startTime = event.startTime,
            endTime = event.endTime,
            durationMinutes = max(15, eventEnd - eventStart),
            color = event.displayColor()
        )
        cursor = max(cursor, eventEnd)
    }
    return segments
}



fun parseTimeMinutes(value: String): Int {
    val parts = value.split(":")
    if (parts.size != 2) return 0
    val hour = parts[0].toIntOrNull() ?: return 0
    val minute = parts[1].toIntOrNull() ?: return 0
    return (hour * 60 + minute).coerceIn(0, 23 * 60 + 59)
}



fun minutesToTime(value: Int): String {
    val hour = (value / 60).coerceIn(0, 23)
    val minute = (value % 60).coerceIn(0, 59)
    return "%02d:%02d".format(hour, minute)
}



fun formatDisplayTime(time: String): String {
    val parts = time.split(":")
    if (parts.size != 2) return time
    val hour = parts[0].toIntOrNull() ?: return time
    val minute = parts[1].toIntOrNull() ?: return time
    val amPm = if (hour < 12) "AM" else "PM"
    val displayHour = when {
        hour == 0 -> 12
        hour > 12 -> hour - 12
        else -> hour
    }
    return if (minute == 0) "$displayHour:00 $amPm" else "$displayHour:%02d $amPm".format(minute)
}



fun computeDuration(startTime: String, endTime: String): String {
    val startMinutes = parseTimeMinutes(startTime)
    val endMinutes = parseTimeMinutes(endTime)
    val diff = (endMinutes - startMinutes).coerceAtLeast(0)
    if (diff == 0) return ""
    val hours = diff / 60
    val mins = diff % 60
    return when {
        hours == 0 -> "${mins}m"
        mins == 0 -> "${hours}h"
        mins == 30 -> "${hours}.5h"
        else -> "${hours}h${mins}m"
    }
}

fun ItineraryEvent.displayColor(): Color {
    return colorHex.toItineraryColor()
}

fun ItineraryDay.timeRangeLabel(): String? {
    if (events.isEmpty()) return null
    val sorted = events.sortedBy { it.startTime }
    return "${sorted.first().startTime} - ${sorted.last().endTime}"
}
