package com.mobile.travelhub.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
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
import com.mobile.travelhub.ui.theme.TravelHubTheme
import com.mobile.travelhub.viewmodels.ItineraryUiState
import com.mobile.travelhub.viewmodels.ItineraryViewModel
import kotlin.math.max
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyListState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ItineraryScreen(
    tripId: Long? = null,
    groupName: String,
    onBack: () -> Unit,
    onOpenDayDetail: (Int) -> Unit,
    showBackButton: Boolean = true,
    openChatOnLaunch: Boolean = false,
    viewModel: ItineraryViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(tripId, groupName, openChatOnLaunch) {
        viewModel.bindGroup(groupName = groupName, tripId = tripId, openChatOnLaunch = openChatOnLaunch)
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
            GeminiItineraryFab(onClick = viewModel::openChat)
        }
    ) { paddingValues ->
        ItineraryOverviewContent(
            state = state,
            paddingValues = paddingValues,
            onOpenDayDetail = onOpenDayDetail,
            onToggleChange = viewModel::toggleChangeSelection,
            onApplySelected = viewModel::applySelectedChanges,
            onDiscardProposal = viewModel::discardPendingProposal
        )
    }

    ItinerarySharedOverlays(
        state = state,
        onCloseChat = viewModel::closeChat,
        onChatInputChange = viewModel::updateChatInput,
        onVoiceInputChange = viewModel::updateVoiceChatInput,
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
    tripId: Long? = null,
    groupName: String,
    dayIndex: Int,
    onBack: () -> Unit,
    showBackButton: Boolean = true,
    openChatOnLaunch: Boolean = false,
    viewModel: ItineraryViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(tripId, groupName, openChatOnLaunch) {
        viewModel.bindGroup(groupName = groupName, tripId = tripId, openChatOnLaunch = openChatOnLaunch)
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
            GeminiItineraryFab(onClick = viewModel::openChat)
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
        onVoiceInputChange = viewModel::updateVoiceChatInput,
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
fun ItineraryPopupSheet(
    tripId: Long? = null,
    groupName: String,
    onDismiss: () -> Unit,
    viewModel: ItineraryViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(tripId, groupName) {
        viewModel.bindGroup(groupName = groupName, tripId = tripId)
    }

    LaunchedEffect(state.errorMessage) {
        val message = state.errorMessage ?: return@LaunchedEffect
        snackbarHostState.showSnackbar(message)
        viewModel.clearError()
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = SurfaceBg
    ) {
        ItineraryPopupContent(
            state = state,
            groupName = groupName,
            snackbarHostState = snackbarHostState,
            onDismiss = onDismiss,
            onToggleEditMode = viewModel::toggleEditMode,
            onOpenChat = viewModel::openChat,
            onOpenDayDetail = viewModel::selectDay,
            onToggleChange = viewModel::toggleChangeSelection,
            onApplySelected = viewModel::applySelectedChanges,
            onDiscardProposal = viewModel::discardPendingProposal,
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.92f)
        )
    }

    ItinerarySharedOverlays(
        state = state,
        onCloseChat = viewModel::closeChat,
        onChatInputChange = viewModel::updateChatInput,
        onVoiceInputChange = viewModel::updateVoiceChatInput,
        onSendChat = viewModel::sendChatPrompt,
        onDismissDayEditor = viewModel::cancelEditingDay,
        onSaveDay = viewModel::saveDay,
        onDeleteEditingDay = viewModel::deleteEditingDay,
        onDismissEventEditor = viewModel::cancelEditing,
        onSaveEvent = viewModel::saveEvent,
        onDeleteEditingEvent = viewModel::deleteEditingEvent
    )
}

@Composable
private fun ItineraryPopupContent(
    state: ItineraryUiState,
    groupName: String,
    snackbarHostState: SnackbarHostState,
    onDismiss: () -> Unit,
    onToggleEditMode: () -> Unit,
    onOpenChat: () -> Unit,
    onOpenDayDetail: (Int) -> Unit,
    onToggleChange: (String) -> Unit,
    onApplySelected: () -> Unit,
    onDiscardProposal: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier,
        containerColor = SurfaceBg,
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 20.dp, end = 12.dp, top = 4.dp, bottom = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (state.groupName.isBlank()) groupName else state.groupName,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 20.sp,
                        color = OnSurface
                    )
                    Text(
                        text = "Version ${state.version}",
                        fontSize = 12.sp,
                        color = OnSurfaceVariant
                    )
                }
                if (state.isLeader) {
                    ItineraryEditButton(
                        isEditMode = state.isEditMode,
                        onToggleEditMode = onToggleEditMode
                    )
                }
                IconButton(onClick = onDismiss) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close itinerary",
                        tint = OnSurface
                    )
                }
            }
        },
        floatingActionButton = {
            GeminiItineraryFab(onClick = onOpenChat)
        }
    ) { paddingValues ->
        ItineraryOverviewContent(
            state = state,
            paddingValues = paddingValues,
            onOpenDayDetail = onOpenDayDetail,
            onToggleChange = onToggleChange,
            onApplySelected = onApplySelected,
            onDiscardProposal = onDiscardProposal
        )
    }
}

@Preview(showBackground = true, widthDp = 390, heightDp = 720)
@Composable
private fun ItineraryPopupPreview() {
    TravelHubTheme {
        Surface(color = SurfaceBg) {
            ItineraryPopupContent(
                state = previewItineraryState(),
                groupName = "New test trip",
                snackbarHostState = remember { SnackbarHostState() },
                onDismiss = {},
                onToggleEditMode = {},
                onOpenChat = {},
                onOpenDayDetail = {},
                onToggleChange = {},
                onApplySelected = {},
                onDiscardProposal = {},
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

private fun previewItineraryState(): ItineraryUiState {
    return ItineraryUiState(
        groupName = "New test trip",
        version = 2,
        role = ItineraryUserRole.LEADER,
        days = listOf(
            ItineraryDay(
                dayIndex = 1,
                label = "Day 1",
                dateLabel = "Tuesday, 05/05",
                events = listOf(
                    ItineraryEvent(
                        eventId = "preview-1",
                        dayIndex = 1,
                        startTime = "09:00",
                        endTime = "10:00",
                        title = "Gh",
                        placeName = "Ha Noi",
                        note = "",
                        transportToNext = "",
                        estimatedCost = "",
                        colorHex = ItineraryEventColors.Default
                    )
                )
            ),
            ItineraryDay(
                dayIndex = 2,
                label = "Day 2",
                dateLabel = "Wednesday, 06/05",
                events = listOf(
                    ItineraryEvent(
                        eventId = "preview-2",
                        dayIndex = 2,
                        startTime = "09:00",
                        endTime = "10:00",
                        title = "Cà phê",
                        placeName = "Old Quarter",
                        note = "",
                        transportToNext = "",
                        estimatedCost = "",
                        colorHex = ItineraryEventColors.Default
                    )
                )
            )
        )
    )
}

@Composable
private fun GeminiItineraryFab(onClick: () -> Unit) {
    val infiniteTransition = rememberInfiniteTransition(label = "ai-fab-rainbow")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2400, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "ai-fab-border-rotation"
    )
    val rainbow = listOf(
        Color(0xFFFF3B30),
        Color(0xFFFF9500),
        Color(0xFFFFCC00),
        Color(0xFF34C759),
        Color(0xFF00C7BE),
        Color(0xFF007AFF),
        Color(0xFFAF52DE),
        Color(0xFFFF3B30)
    )

    Box(
        modifier = Modifier
            .size(64.dp)
            .shadow(elevation = 10.dp, shape = CircleShape, clip = false)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .matchParentSize()
                .graphicsLayer { rotationZ = rotation }
                .background(Brush.sweepGradient(rainbow), CircleShape)
        )
        Box(
            modifier = Modifier
                .size(56.dp)
                .background(SurfaceContainerLowest, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.AutoAwesome,
                contentDescription = "Open AI itinerary editor",
                tint = Color(0xFF5B35F5),
                modifier = Modifier.size(28.dp)
            )
        }
    }
}


































