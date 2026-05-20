package com.mobile.travelhub.ui.components.itinerary

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.mobile.travelhub.data.model.*
import com.mobile.travelhub.ui.theme.*
import java.util.Locale
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
    onVoiceInputChange: (String) -> Unit,
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
            onVoiceInputChange = onVoiceInputChange,
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
            dayOptions = state.dayOptions,
            isCreating = state.isCreatingEvent,
            onDismiss = onDismissEventEditor,
            onSave = onSaveEvent,
            onDelete = onDeleteEditingEvent
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ChatProposalSheet(
    state: ItineraryUiState,
    onDismiss: () -> Unit,
    onInputChange: (String) -> Unit,
    onVoiceInputChange: (String) -> Unit,
    onSend: () -> Unit
) {
    val context = LocalContext.current
    var isListening by remember { mutableStateOf(false) }
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            startVoiceRecognition(
                context = context,
                onResult = onVoiceInputChange,
                onListeningChange = { isListening = it }
            )
        } else {
            Toast.makeText(context, "Microphone permission is required", Toast.LENGTH_SHORT).show()
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = SurfaceContainerLowest,
        dragHandle = { BottomSheetDefaults.DragHandle(color = OutlineVariant) }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .navigationBarsPadding()
        ) {
            AiEditorHeader(
                isStreaming = state.isStreaming,
                isListening = isListening,
                onDismiss = onDismiss
            )
            Spacer(modifier = Modifier.height(18.dp))

            Column(
                modifier = Modifier
                    .weight(1f, fill = false)
                    .heightIn(min = 220.dp, max = 360.dp)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (state.chatMessages.isEmpty()) {
                    PromptSuggestions(onSelect = onInputChange)
                }
                state.chatMessages.forEach { message ->
                    ChatBubble(message = message)
                }
                if (state.thinking.isNotBlank()) {
                    WorkingRow(text = state.thinking)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            PromptComposer(
                value = state.chatInput,
                isStreaming = state.isStreaming,
                isListening = isListening,
                onValueChange = onInputChange,
                onMicClick = {
                    val hasPermission = ContextCompat.checkSelfPermission(
                        context,
                        Manifest.permission.RECORD_AUDIO
                    ) == PackageManager.PERMISSION_GRANTED
                    if (hasPermission) {
                        startVoiceRecognition(
                            context = context,
                            onResult = onVoiceInputChange,
                            onListeningChange = { isListening = it }
                        )
                    } else {
                        permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                    }
                },
                onSend = onSend
            )
            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@Composable
private fun AiEditorHeader(
    isStreaming: Boolean,
    isListening: Boolean,
    onDismiss: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .background(
                    brush = Brush.sweepGradient(
                        listOf(
                            Color(0xFF4285F4),
                            Color(0xFF34A853),
                            Color(0xFFFBBC05),
                            Color(0xFFEA4335),
                            Color(0xFFAF52DE),
                            Color(0xFF4285F4)
                        )
                    ),
                    shape = CircleShape
                )
                .padding(3.dp)
                .background(SurfaceContainerLowest, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.AutoAwesome,
                contentDescription = null,
                tint = Color(0xFF5B35F5),
                modifier = Modifier.size(24.dp)
            )
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "AI editor",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.ExtraBold,
                color = OnSurface
            )
            Text(
                text = when {
                    isListening -> "Listening"
                    isStreaming -> "Working"
                    else -> "Itinerary"
                },
                style = MaterialTheme.typography.labelMedium,
                color = if (isListening || isStreaming) PrimaryBlue else OnSurfaceVariant
            )
        }
        IconButton(onClick = onDismiss) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Close",
                tint = OnSurfaceVariant
            )
        }
    }
}

@Composable
private fun ChatBubble(message: ItineraryChatMessage) {
    val isUser = message.role == ItineraryChatRole.USER
    val bubbleColor = if (isUser) PrimaryBlue.copy(alpha = 0.14f) else SurfaceContainerLow
    val alignment = if (isUser) Alignment.CenterEnd else Alignment.CenterStart
    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = alignment) {
        Surface(
            color = bubbleColor,
            shape = RoundedCornerShape(
                topStart = 20.dp,
                topEnd = 20.dp,
                bottomStart = if (isUser) 20.dp else 6.dp,
                bottomEnd = if (isUser) 6.dp else 20.dp
            ),
            modifier = Modifier.widthIn(max = 320.dp)
        ) {
            Text(
                text = message.text,
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                color = OnSurface,
                lineHeight = 20.sp
            )
        }
    }
}

@Composable
private fun WorkingRow(text: String) {
    Surface(
        color = SurfaceContainerLow,
        shape = RoundedCornerShape(18.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = text,
                style = MaterialTheme.typography.bodySmall,
                color = OnSurfaceVariant
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun PromptSuggestions(onSelect: (String) -> Unit) {
    val suggestions = listOf(
        "Tạo lịch 3 ngày nhẹ nhàng",
        "Thêm cafe vào ngày 2",
        "Giãn lịch buổi sáng",
        "Chuyển điểm cuối sang ngày sau"
    )
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        suggestions.forEach { prompt ->
            AssistChip(
                onClick = { onSelect(prompt) },
                label = { Text(prompt) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                },
                colors = AssistChipDefaults.assistChipColors(
                    containerColor = PrimaryBlue.copy(alpha = 0.08f),
                    labelColor = OnSurface
                ),
                border = BorderStroke(1.dp, PrimaryBlue.copy(alpha = 0.18f))
            )
        }
    }
}

@Composable
private fun PromptComposer(
    value: String,
    isStreaming: Boolean,
    isListening: Boolean,
    onValueChange: (String) -> Unit,
    onMicClick: () -> Unit,
    onSend: () -> Unit
) {
    Surface(
        color = SurfaceContainerLow,
        shape = RoundedCornerShape(26.dp),
        border = BorderStroke(1.dp, OutlineVariant.copy(alpha = 0.55f))
    ) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            OutlinedTextField(
                value = value,
                onValueChange = onValueChange,
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Ask for a change or a new trip") },
                minLines = 2,
                maxLines = 4,
                enabled = !isStreaming,
                shape = RoundedCornerShape(18.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = SurfaceContainerLowest,
                    unfocusedContainerColor = SurfaceContainerLowest,
                    focusedBorderColor = Color.Transparent,
                    unfocusedBorderColor = Color.Transparent,
                    disabledBorderColor = Color.Transparent
                )
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                FilledTonalIconButton(
                    onClick = onMicClick,
                    enabled = !isStreaming && !isListening,
                    colors = IconButtonDefaults.filledTonalIconButtonColors(
                        containerColor = if (isListening) PrimaryBlue.copy(alpha = 0.18f) else SurfaceContainerLowest,
                        contentColor = if (isListening) PrimaryBlue else OnSurfaceVariant
                    )
                ) {
                    Icon(
                        imageVector = if (isListening) Icons.Default.GraphicEq else Icons.Default.Mic,
                        contentDescription = "Voice input"
                    )
                }
                Spacer(modifier = Modifier.weight(1f))
                Button(
                    onClick = onSend,
                    enabled = !isStreaming && value.isNotBlank(),
                    shape = RoundedCornerShape(999.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PrimaryBlue,
                        contentColor = Color.White
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Generate")
                }
            }
        }
    }
}

private fun startVoiceRecognition(
    context: Context,
    onResult: (String) -> Unit,
    onListeningChange: (Boolean) -> Unit
) {
    if (!SpeechRecognizer.isRecognitionAvailable(context)) {
        Toast.makeText(context, "Speech recognition is not available", Toast.LENGTH_SHORT).show()
        return
    }

    val recognizer = SpeechRecognizer.createSpeechRecognizer(context)
    fun finish() {
        onListeningChange(false)
        recognizer.destroy()
    }

    recognizer.setRecognitionListener(object : RecognitionListener {
        override fun onReadyForSpeech(params: Bundle?) = onListeningChange(true)
        override fun onBeginningOfSpeech() = Unit
        override fun onRmsChanged(rmsdB: Float) = Unit
        override fun onBufferReceived(buffer: ByteArray?) = Unit
        override fun onEndOfSpeech() = onListeningChange(false)
        override fun onError(error: Int) = finish()
        override fun onPartialResults(partialResults: Bundle?) = Unit
        override fun onEvent(eventType: Int, params: Bundle?) = Unit

        override fun onResults(results: Bundle?) {
            val transcript = results
                ?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                ?.firstOrNull()
                .orEmpty()
            if (transcript.isNotBlank()) {
                onResult(transcript)
            }
            finish()
        }
    })

    val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
        putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
        putExtra(RecognizerIntent.EXTRA_LANGUAGE, "vi-VN")
        putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, Locale.forLanguageTag("vi-VN"))
    }
    recognizer.startListening(intent)
}
