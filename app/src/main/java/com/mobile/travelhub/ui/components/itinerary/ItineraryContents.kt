package com.mobile.travelhub.ui.components.itinerary

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
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
import com.mobile.travelhub.viewmodels.ItineraryUiState


@Composable
fun ItineraryOverviewContent(
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
fun ItineraryDayDetailContent(
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

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ProposalReviewSection(
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