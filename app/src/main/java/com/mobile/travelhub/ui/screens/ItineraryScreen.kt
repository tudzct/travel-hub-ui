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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DragIndicator
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Remove
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
import com.mobile.travelhub.data.model.*

import com.mobile.travelhub.ui.components.itinerary.*
import com.mobile.travelhub.ui.components.itinerary.ItineraryDayEditorDialog
import com.mobile.travelhub.ui.components.itinerary.ItineraryEditButton
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
                isEditMode = state.isEditMode,
                onToggleEditMode = viewModel::toggleEditMode,
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
                isEditMode = state.isEditMode,
                onToggleEditMode = viewModel::toggleEditMode,
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
            isEditMode = state.isEditMode,
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
    isEditMode: Boolean = false,
    onToggleEditMode: (() -> Unit)? = null,
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
            if (isLeader && onToggleEditMode != null) {
                ItineraryEditButton(
                    isEditMode = isEditMode,
                    onToggleEditMode = onToggleEditMode
                )
            }
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
                    isEditMode = state.isEditMode,
                    onClick = { onOpenDayDetail(day.dayIndex) },
                    onEdit = { onEditDay(day) },
                    onDelete = { onDeleteDay(day.dayIndex) }
                )
            }
        }
        if (state.isEditMode) {
            item {
                AddActionCard(
                    title = "Add day",
                    onClick = onAddDay
                )
            }
        }
    }
}

@Composable
private fun ItineraryDayDetailContent(
    day: ItineraryDay?,
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
        if (day != null && isEditMode) {
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




