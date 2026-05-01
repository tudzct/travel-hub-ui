package com.mobile.travelhub.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DragIndicator
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.mobile.travelhub.data.model.AddEventChange
import com.mobile.travelhub.data.model.DeleteEventChange
import com.mobile.travelhub.data.model.ItineraryChange
import com.mobile.travelhub.data.model.ItineraryChatRole
import com.mobile.travelhub.data.model.ItineraryDay
import com.mobile.travelhub.data.model.ItineraryEvent
import com.mobile.travelhub.data.model.ItineraryEventColors
import com.mobile.travelhub.data.model.ItineraryProposal
import com.mobile.travelhub.data.model.MoveEventChange
import com.mobile.travelhub.data.model.UpdateEventChange
import com.mobile.travelhub.ui.components.itinerary.ItineraryDayEditorDialog
import com.mobile.travelhub.ui.components.itinerary.ItineraryEventEditorDialog
import com.mobile.travelhub.ui.components.itinerary.toItineraryColor
import com.mobile.travelhub.ui.theme.OnSurface
import com.mobile.travelhub.ui.theme.OnSurfaceVariant
import com.mobile.travelhub.ui.theme.PrimaryBlue
import com.mobile.travelhub.ui.theme.SurfaceBg
import com.mobile.travelhub.ui.theme.SurfaceContainerLow
import com.mobile.travelhub.ui.theme.SurfaceContainerLowest
import com.mobile.travelhub.ui.theme.SunsetOrange
import com.mobile.travelhub.viewmodels.ItineraryUiState
import com.mobile.travelhub.viewmodels.ItineraryViewModel
import kotlin.math.max
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyListState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ItineraryScreen(
    groupName: String,
    onBack: () -> Unit,
    onOpenDayDetail: (Int) -> Unit,
    showBackButton: Boolean = true,
    openChatOnLaunch: Boolean = false,
    viewModel: ItineraryViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(groupName, openChatOnLaunch) {
        viewModel.bindGroup(groupName = groupName, openChatOnLaunch = openChatOnLaunch)
    }

    LaunchedEffect(state.errorMessage) {
        val message = state.errorMessage ?: return@LaunchedEffect
        snackbarHostState.showSnackbar(message)
        viewModel.clearError()
    }

    Scaffold(
        containerColor = SurfaceBg,
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            ItineraryTopBar(
                title = if (state.groupName.isBlank()) "Itinerary" else state.groupName,
                subtitle = "Version ${state.version}",
                isLeader = state.isLeader,
                showBackButton = showBackButton,
                onBack = onBack
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = viewModel::openChat,
                containerColor = PrimaryBlue,
                contentColor = Color.White
            ) {
                Icon(Icons.Default.AutoAwesome, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("AI edits")
            }
        }
    ) { paddingValues ->
        ItineraryOverviewContent(
            state = state,
            paddingValues = paddingValues,
            onOpenDayDetail = onOpenDayDetail,
            onAddDay = viewModel::addDay,
            onEditDay = viewModel::startEditingDay,
            onDeleteDay = viewModel::deleteDay,
            onToggleChange = viewModel::toggleChangeSelection,
            onApplySelected = viewModel::applySelectedChanges,
            onDiscardProposal = viewModel::discardPendingProposal
        )
    }

    ItinerarySharedOverlays(
        state = state,
        onCloseChat = viewModel::closeChat,
        onChatInputChange = viewModel::updateChatInput,
        onSendChat = viewModel::sendChatPrompt,
        onDismissDayEditor = viewModel::cancelEditingDay,
        onSaveDay = viewModel::saveDay,
        onDeleteEditingDay = viewModel::deleteEditingDay,
        onDismissEventEditor = viewModel::cancelEditing,
        onSaveEvent = viewModel::saveEvent,
        onDeleteEditingEvent = viewModel::deleteEditingEvent
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ItineraryDayDetailScreen(
    groupName: String,
    dayIndex: Int,
    onBack: () -> Unit,
    showBackButton: Boolean = true,
    openChatOnLaunch: Boolean = false,
    viewModel: ItineraryViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(groupName, openChatOnLaunch) {
        viewModel.bindGroup(groupName = groupName, openChatOnLaunch = openChatOnLaunch)
    }

    LaunchedEffect(dayIndex) {
        viewModel.selectDay(dayIndex)
    }

    LaunchedEffect(state.errorMessage) {
        val message = state.errorMessage ?: return@LaunchedEffect
        snackbarHostState.showSnackbar(message)
        viewModel.clearError()
    }

    val selectedDay = state.days.firstOrNull { it.dayIndex == dayIndex } ?: state.selectedDay

    Scaffold(
        containerColor = SurfaceBg,
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            ItineraryTopBar(
                title = selectedDay?.label ?: "Day detail",
                subtitle = selectedDay?.dateLabel ?: state.groupName,
                isLeader = state.isLeader,
                showBackButton = showBackButton,
                onBack = onBack
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = viewModel::openChat,
                containerColor = PrimaryBlue,
                contentColor = Color.White
            ) {
                Icon(Icons.Default.AutoAwesome, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("AI edits")
            }
        }
    ) { paddingValues ->
        ItineraryDayDetailContent(
            day = selectedDay,
            paddingValues = paddingValues,
            onAddStop = viewModel::startAddingStop,
            onReorderEvents = viewModel::reorderDayEvents,
            onEditEvent = viewModel::startEditing,
            onDeleteEvent = viewModel::deleteEvent
        )
    }

    ItinerarySharedOverlays(
        state = state,
        onCloseChat = viewModel::closeChat,
        onChatInputChange = viewModel::updateChatInput,
        onSendChat = viewModel::sendChatPrompt,
        onDismissDayEditor = viewModel::cancelEditingDay,
        onSaveDay = viewModel::saveDay,
        onDeleteEditingDay = viewModel::deleteEditingDay,
        onDismissEventEditor = viewModel::cancelEditing,
        onSaveEvent = viewModel::saveEvent,
        onDeleteEditingEvent = viewModel::deleteEditingEvent
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ItineraryTopBar(
    title: String,
    subtitle: String,
    isLeader: Boolean,
    showBackButton: Boolean,
    onBack: () -> Unit
) {
    TopAppBar(
        title = {
            Column {
                Text(
                    text = title,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 18.sp,
                    color = OnSurface
                )
                Text(
                    text = subtitle,
                    fontSize = 12.sp,
                    color = OnSurfaceVariant
                )
            }
        },
        navigationIcon = {
            if (showBackButton) {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = OnSurface
                    )
                }
            }
        },
        actions = {
            AssistChip(
                onClick = {},
                label = { Text(if (isLeader) "Leader" else "Member") },
                leadingIcon = {
                    Icon(
                        imageVector = if (isLeader) Icons.Default.Check else Icons.Default.Close,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                }
            )
            Spacer(modifier = Modifier.width(8.dp))
        },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = SurfaceBg)
    )
}

@Composable
private fun ItineraryOverviewContent(
    state: ItineraryUiState,
    paddingValues: PaddingValues,
    onOpenDayDetail: (Int) -> Unit,
    onAddDay: () -> Unit,
    onEditDay: (ItineraryDay) -> Unit,
    onDeleteDay: (Int) -> Unit,
    onToggleChange: (String) -> Unit,
    onApplySelected: () -> Unit,
    onDiscardProposal: () -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 12.dp, bottom = 120.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        state.pendingProposal?.let { proposal ->
            item {
                ProposalReviewSection(
                    proposal = proposal,
                    version = state.version,
                    selectedChangeIds = state.selectedChangeIds,
                    isLeader = state.isLeader,
                    isStale = state.isProposalStale,
                    onToggleChange = onToggleChange,
                    onApplySelected = onApplySelected,
                    onDiscardProposal = onDiscardProposal
                )
            }
        }
        if (state.days.isEmpty()) {
            item {
                EmptyOverviewCard()
            }
        } else {
            items(state.days, key = { it.dayIndex }) { day ->
                ItineraryDayCard(
                    day = day,
                    onClick = { onOpenDayDetail(day.dayIndex) },
                    onEdit = { onEditDay(day) },
                    onDelete = { onDeleteDay(day.dayIndex) }
                )
            }
        }
        item {
            AddActionCard(
                title = "Add day",
                description = "Create a new day card at the end of the itinerary.",
                onClick = onAddDay
            )
        }
    }
}

@Composable
private fun ItineraryDayDetailContent(
    day: ItineraryDay?,
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
        if (day != null) {
            item {
                DayContinuousTimelineCard(day = day)
            }
        }
        if (day == null || day.events.isEmpty()) {
            item {
                EmptyDayCard()
            }
        } else {
            items(day.events, key = { it.eventId }) { event ->
                ReorderableItem(reorderableState, key = event.eventId) { isDragging ->
                    DayEventCard(
                        event = event,
                        isDragging = isDragging,
                        dragHandle = {
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
                        },
                        onEdit = { onEditEvent(event) },
                        onDelete = { onDeleteEvent(event.eventId) }
                    )
                }
            }
        }
        if (day != null) {
            item {
                AddActionCard(
                    title = "Add stop",
                    description = "Create a new stop at the end of this day.",
                    onClick = onAddStop
                )
            }
        }
    }
}

@Composable
private fun DayContinuousTimelineCard(day: ItineraryDay) {
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
private fun SummaryChip(label: String) {
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

@Composable
private fun ItineraryDayCard(
    day: ItineraryDay,
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

    Card(
        modifier = Modifier.fillMaxWidth(),
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

@Composable
private fun DayEventCard(
    event: ItineraryEvent,
    isDragging: Boolean,
    dragHandle: @Composable (() -> Unit),
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    var expanded by remember(event.eventId) { mutableStateOf(false) }
    val elevation by animateDpAsState(targetValue = if (isDragging) 10.dp else 0.dp, label = "eventCardElevation")
    val accent = event.displayColor()

    Card(
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceContainerLowest),
        elevation = CardDefaults.cardElevation(defaultElevation = elevation)
    ) {
        Row {
            Box(
                modifier = Modifier
                    .width(10.dp)
                    .fillMaxHeight()
                    .background(accent)
            )
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
                            if (event.isHighlighted) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Icon(
                                    imageVector = Icons.Default.Star,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp),
                                    tint = SunsetOrange
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = event.placeName,
                            style = MaterialTheme.typography.bodyMedium,
                            color = OnSurfaceVariant
                        )
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        dragHandle()
                        Box {
                            IconButton(onClick = { expanded = true }) {
                                Icon(
                                    Icons.Default.MoreVert,
                                    contentDescription = "Event actions",
                                    tint = OnSurfaceVariant
                                )
                            }
                            DropdownMenu(
                                expanded = expanded,
                                onDismissRequest = { expanded = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text("Cập nhật") },
                                    onClick = {
                                        expanded = false
                                        onEdit()
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Xóa") },
                                    onClick = {
                                        expanded = false
                                        onDelete()
                                    }
                                )
                            }
                        }
                    }
                }

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
                        Text(event.estimatedCost.ifBlank { "No cost" }, fontSize = 12.sp, color = OnSurfaceVariant)
                    }
                    MetaPill {
                        Text(colorLabel(event.colorHex), fontSize = 12.sp, color = accent)
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

@Composable
private fun MetaPill(
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
private fun EmptyOverviewCard() {
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
private fun AddActionCard(
    title: String,
    description: String,
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
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = OnSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun EmptyDayCard() {
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
private fun ItinerarySharedOverlays(
    state: ItineraryUiState,
    onCloseChat: () -> Unit,
    onChatInputChange: (String) -> Unit,
    onSendChat: () -> Unit,
    onDismissDayEditor: () -> Unit,
    onSaveDay: (ItineraryDay) -> Unit,
    onDeleteEditingDay: () -> Unit,
    onDismissEventEditor: () -> Unit,
    onSaveEvent: (ItineraryEvent) -> Unit,
    onDeleteEditingEvent: () -> Unit
) {
    if (state.isChatSheetOpen) {
        ChatProposalSheet(
            state = state,
            onDismiss = onCloseChat,
            onInputChange = onChatInputChange,
            onSend = onSendChat
        )
    }

    state.editingDay?.let { day ->
        ItineraryDayEditorDialog(
            day = day,
            onDismiss = onDismissDayEditor,
            onSave = onSaveDay,
            onDelete = onDeleteEditingDay
        )
    }

    state.editingEvent?.let { event ->
        ItineraryEventEditorDialog(
            event = event,
            dayCount = state.days.size,
            isCreating = state.isCreatingEvent,
            onDismiss = onDismissEventEditor,
            onSave = onSaveEvent,
            onDelete = onDeleteEditingEvent
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ProposalReviewSection(
    proposal: ItineraryProposal,
    version: Int,
    selectedChangeIds: Set<String>,
    isLeader: Boolean,
    isStale: Boolean,
    onToggleChange: (String) -> Unit,
    onApplySelected: () -> Unit,
    onDiscardProposal: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceContainerLowest)
    ) {
        Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Pending AI changes",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = OnSurface
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = proposal.summary,
                        style = MaterialTheme.typography.bodyMedium,
                        color = OnSurfaceVariant,
                        lineHeight = 20.sp
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Box(
                    modifier = Modifier
                        .background(
                            color = if (isStale) SunsetOrange.copy(alpha = 0.12f) else PrimaryBlue.copy(alpha = 0.12f),
                            shape = RoundedCornerShape(999.dp)
                        )
                        .padding(horizontal = 12.dp, vertical = 7.dp)
                ) {
                    Text(
                        text = if (isStale) "Stale vs v$version" else "Base v${proposal.baseVersion}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = if (isStale) SunsetOrange else PrimaryBlue
                    )
                }
            }

            if (isStale) {
                Text(
                    text = "This proposal was generated against an older itinerary version. Review is still available, but apply is disabled until you regenerate.",
                    style = MaterialTheme.typography.bodySmall,
                    color = SunsetOrange,
                    lineHeight = 18.sp
                )
            }

            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                SummaryChip(label = "${selectedChangeIds.size} selected")
                SummaryChip(label = "${proposal.changes.size} total changes")
                SummaryChip(label = if (isLeader) "Leader approval" else "Member preview")
            }

            proposal.changes.forEach { change ->
                ProposalChangeCard(
                    change = change,
                    selected = change.changeId in selectedChangeIds,
                    selectable = isLeader && !isStale,
                    onToggle = { onToggleChange(change.changeId) }
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedButton(
                    onClick = onDiscardProposal,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Discard")
                }
                TextButton(
                    onClick = onApplySelected,
                    enabled = isLeader && !isStale && selectedChangeIds.isNotEmpty(),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Apply selected")
                }
            }
        }
    }
}

@Composable
private fun ProposalChangeCard(
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
private fun EventPreviewCard(
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
private fun DiffRow(label: String, before: String?, after: String?) {
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ChatProposalSheet(
    state: ItineraryUiState,
    onDismiss: () -> Unit,
    onInputChange: (String) -> Unit,
    onSend: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = SurfaceContainerLowest
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp)
        ) {
            Text(
                text = "AI itinerary editor",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.ExtraBold,
                color = OnSurface
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Describe what to add, remove, move, or reschedule. The assistant will prepare a reviewable diff instead of editing blindly.",
                style = MaterialTheme.typography.bodyMedium,
                color = OnSurfaceVariant,
                lineHeight = 20.sp
            )
            Spacer(modifier = Modifier.height(16.dp))

            Column(
                modifier = Modifier
                    .weight(1f, fill = false)
                    .height(320.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (state.chatMessages.isEmpty()) {
                    SuggestionCard()
                }
                state.chatMessages.forEach { message ->
                    val bubbleColor = if (message.role == ItineraryChatRole.USER) {
                        PrimaryBlue.copy(alpha = 0.12f)
                    } else {
                        SurfaceContainerLow
                    }
                    val alignment = if (message.role == ItineraryChatRole.USER) {
                        Alignment.CenterEnd
                    } else {
                        Alignment.CenterStart
                    }
                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = alignment) {
                        Surface(
                            color = bubbleColor,
                            shape = RoundedCornerShape(20.dp)
                        ) {
                            Text(
                                text = message.text,
                                modifier = Modifier.padding(14.dp),
                                color = OnSurface,
                                lineHeight = 20.sp
                            )
                        }
                    }
                }
                if (state.thinking.isNotBlank()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = state.thinking,
                            style = MaterialTheme.typography.bodySmall,
                            color = OnSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(
                value = state.chatInput,
                onValueChange = onInputChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Describe a change request") },
                minLines = 3,
                maxLines = 5
            )
            Spacer(modifier = Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Close")
                }
                TextButton(
                    onClick = onSend,
                    enabled = !state.isStreaming && state.chatInput.isNotBlank(),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Generate proposal")
                }
            }
            Spacer(modifier = Modifier.height(18.dp))
        }
    }
}

@Composable
private fun SuggestionCard() {
    Card(
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = PrimaryBlue.copy(alpha = 0.08f))
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = "Try prompts like:",
                fontWeight = FontWeight.Bold,
                color = OnSurface
            )
            Text(
                text = "“Thêm một quán cafe đệm giữa 2 điểm buổi sáng”\n“Sửa giờ Shibuya Sky muộn hơn 1 tiếng”\n“Chuyển event cuối ngày 1 sang ngày 2”",
                color = OnSurfaceVariant,
                lineHeight = 20.sp
            )
        }
    }
}

private fun ItineraryDay.timeRangeLabel(): String? {
    if (events.isEmpty()) return null
    val sorted = events.sortedBy { it.startTime }
    return "${sorted.first().startTime} - ${sorted.last().endTime}"
}

private data class TimelineSegmentUi(
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

private fun ItineraryEvent.displayColor(): Color {
    return colorHex.toItineraryColor()
}

private fun parseTimeMinutes(value: String): Int {
    val parts = value.split(":")
    if (parts.size != 2) return 0
    val hour = parts[0].toIntOrNull() ?: return 0
    val minute = parts[1].toIntOrNull() ?: return 0
    return (hour * 60 + minute).coerceIn(0, 23 * 60 + 59)
}

private fun minutesToTime(value: Int): String {
    val hour = (value / 60).coerceIn(0, 23)
    val minute = (value % 60).coerceIn(0, 59)
    return "%02d:%02d".format(hour, minute)
}

private fun colorLabel(colorHex: Long): String {
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

private fun diffValueLabel(label: String, value: String?): String {
    val normalized = value.orEmpty().ifBlank { "Empty" }
    if (!label.equals("Color", ignoreCase = true)) return normalized
    return normalized.toLongOrNull()?.let(::colorLabel) ?: normalized
}

private fun changeTitle(change: ItineraryChange): String {
    return when (change) {
        is AddEventChange -> "Add ${change.eventAfter.title}"
        is DeleteEventChange -> "Delete ${change.eventBefore.title}"
        is MoveEventChange -> "Move ${change.eventSnapshot.title}"
        is UpdateEventChange -> "Update ${change.eventBefore.title}"
    }
}

private fun changeTypeLabel(change: ItineraryChange): String {
    return when (change) {
        is AddEventChange -> "Add"
        is DeleteEventChange -> "Delete"
        is MoveEventChange -> "Move"
        is UpdateEventChange -> "Edit"
    }
}

private fun formatDisplayTime(time: String): String {
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

private fun computeDuration(startTime: String, endTime: String): String {
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
