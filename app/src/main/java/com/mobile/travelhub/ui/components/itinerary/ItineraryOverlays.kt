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
fun ItinerarySharedOverlays(
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatProposalSheet(
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
fun SuggestionCard() {
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