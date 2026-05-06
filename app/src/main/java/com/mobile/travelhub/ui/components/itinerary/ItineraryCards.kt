package com.mobile.travelhub.ui.components.itinerary

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
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mobile.travelhub.data.model.*
import com.mobile.travelhub.ui.theme.*
import kotlin.math.max
import sh.calvin.reorderable.ReorderableItem
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback


@Composable
fun DayContinuousTimelineCard(day: ItineraryDay) {
    val segments = remember(day.events) { buildTimelineSegments(day.events) }

    Card(
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceContainerLowest)
    ) {
        Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Text(
                text = "Continuous timeline",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.ExtraBold,
                color = OnSurface
            )
            if (segments.isEmpty()) {
                Text(
                    text = "Add stops to render the time distribution for this day.",
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
                            Text(
                                text = "${event.startTime} - ${event.endTime}",
                                style = MaterialTheme.typography.labelMedium,
                                color = OnSurfaceVariant
                            )
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
                        text = "${day.label} - ${day.dateLabel}",
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
                                    text = "${day.events.size} events",
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
                        contentDescription = "Delete",
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
                        Text(
                            text = "${event.startTime} - ${event.endTime}",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = accent
                        )
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
                                    contentDescription = "Edit stop",
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
                        contentDescription = "Delete",
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
fun EmptyOverviewCard() {
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceContainerLowest)
    ) {
        Text(
            text = "No day has been created yet. Add a day to start planning the itinerary.",
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
            text = "No events on this day yet. Add a stop or ask AI to draft one for you.",
            modifier = Modifier.padding(20.dp),
            color = OnSurfaceVariant,
            lineHeight = 20.sp
        )
    }
}

@Composable
fun ProposalChangeCard(
    change: ItineraryChange,
    selected: Boolean,
    selectable: Boolean,
    onToggle: () -> Unit
) {
    val accent = when (change) {
        is AddEventChange -> Color(0xFF0D8A4B)
        is DeleteEventChange -> Color(0xFFC44536)
        is MoveEventChange -> Color(0xFF006D77)
        is UpdateEventChange -> PrimaryBlue
    }

    Card(
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = accent.copy(alpha = 0.08f))
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Checkbox(checked = selected, enabled = selectable, onCheckedChange = { onToggle() })
                    Column {
                        Text(
                            text = changeTitle(change),
                            fontWeight = FontWeight.ExtraBold,
                            color = OnSurface
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = change.reason,
                            style = MaterialTheme.typography.bodySmall,
                            color = OnSurfaceVariant,
                            lineHeight = 18.sp
                        )
                    }
                }
                Box(
                    modifier = Modifier
                        .background(accent.copy(alpha = 0.18f), RoundedCornerShape(999.dp))
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = changeTypeLabel(change),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = accent
                    )
                }
            }

            when (change) {
                is AddEventChange -> EventPreviewCard(event = change.eventAfter, accent = accent)
                is DeleteEventChange -> EventPreviewCard(
                    event = change.eventBefore,
                    accent = accent,
                    crossedOut = true
                )
                is MoveEventChange -> {
                    Text(
                        text = "Move ${change.eventSnapshot.title} from Day ${change.fromDayIndex} #${change.fromIndex + 1} to Day ${change.toDayIndex} #${change.toIndex + 1}.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = OnSurface
                    )
                }
                is UpdateEventChange -> {
                    change.fieldDiffs.forEach { diff ->
                        DiffRow(label = diff.label, before = diff.before, after = diff.after)
                    }
                }
            }
        }
    }
}

@Composable
fun EventPreviewCard(
    event: ItineraryEvent,
    accent: Color,
    crossedOut: Boolean = false
) {
    Surface(
        color = Color.White.copy(alpha = 0.8f),
        shape = RoundedCornerShape(18.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = "${event.startTime} - ${event.endTime}",
                fontWeight = FontWeight.Bold,
                color = event.displayColor()
            )
            Text(
                text = event.title,
                fontWeight = FontWeight.ExtraBold,
                color = OnSurface,
                textDecoration = if (crossedOut) TextDecoration.LineThrough else TextDecoration.None
            )
            Text(
                text = event.placeName,
                color = OnSurfaceVariant,
                textDecoration = if (crossedOut) TextDecoration.LineThrough else TextDecoration.None
            )
            Text(
                text = colorLabel(event.colorHex),
                color = accent,
                style = MaterialTheme.typography.labelSmall
            )
        }
    }
}

@Composable
fun DiffRow(label: String, before: String?, after: String?) {
    val beforeValue = diffValueLabel(label, before)
    val afterValue = diffValueLabel(label, after)
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = OnSurfaceVariant
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Surface(
                color = Color(0xFFFCE8E6),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = beforeValue,
                    modifier = Modifier.padding(10.dp),
                    color = Color(0xFFC44536),
                    lineHeight = 18.sp
                )
            }
            Surface(
                color = Color(0xFFE8F5EE),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = afterValue,
                    modifier = Modifier.padding(10.dp),
                    color = Color(0xFF0D8A4B),
                    lineHeight = 18.sp
                )
            }
        }
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



fun colorLabel(colorHex: Long): String {
    return when (colorHex) {
        ItineraryEventColors.Palette.getOrNull(0) -> "Blue"
        ItineraryEventColors.Palette.getOrNull(1) -> "Green"
        ItineraryEventColors.Palette.getOrNull(2) -> "Amber"
        ItineraryEventColors.Palette.getOrNull(3) -> "Red"
        ItineraryEventColors.Palette.getOrNull(4) -> "Violet"
        ItineraryEventColors.Palette.getOrNull(5) -> "Teal"
        ItineraryEventColors.Palette.getOrNull(6) -> "Rose"
        ItineraryEventColors.Palette.getOrNull(7) -> "Slate"
        else -> "Custom"
    }
}



fun diffValueLabel(label: String, value: String?): String {
    val normalized = value.orEmpty().ifBlank { "Empty" }
    if (!label.equals("Color", ignoreCase = true)) return normalized
    return normalized.toLongOrNull()?.let(::colorLabel) ?: normalized
}



fun changeTitle(change: ItineraryChange): String {
    return when (change) {
        is AddEventChange -> "Add ${change.eventAfter.title}"
        is DeleteEventChange -> "Delete ${change.eventBefore.title}"
        is MoveEventChange -> "Move ${change.eventSnapshot.title}"
        is UpdateEventChange -> "Update ${change.eventBefore.title}"
    }
}



fun changeTypeLabel(change: ItineraryChange): String {
    return when (change) {
        is AddEventChange -> "Add"
        is DeleteEventChange -> "Delete"
        is MoveEventChange -> "Move"
        is UpdateEventChange -> "Edit"
    }
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