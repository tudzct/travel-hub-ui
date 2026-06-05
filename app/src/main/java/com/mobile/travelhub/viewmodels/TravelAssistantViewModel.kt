package com.mobile.travelhub.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mobile.travelhub.data.ItineraryRepository
import com.mobile.travelhub.data.TravelAssistantRepository
import com.mobile.travelhub.data.model.ItineraryWorkspace
import com.mobile.travelhub.data.model.TravelAssistantChatRequest
import com.mobile.travelhub.data.model.TravelAssistantMessageRequest
import com.mobile.travelhub.data.model.TravelAssistantPlaceReference
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class TravelAssistantRole {
    USER,
    ASSISTANT
}

data class TravelAssistantMessageUi(
    val id: Long,
    val role: TravelAssistantRole,
    val content: String,
    val places: List<TravelAssistantPlaceReference> = emptyList(),
    val isLocalIntro: Boolean = false
)

data class TravelAssistantUiState(
    val input: String = "",
    val messages: List<TravelAssistantMessageUi> = emptyList(),
    val isSending: Boolean = false,
    val errorMessage: String? = null,
    val groupName: String = "",
    val tripId: Long? = null,
    val quickPrompts: List<String> = emptyList()
)

@HiltViewModel
class TravelAssistantViewModel @Inject constructor(
    private val repository: TravelAssistantRepository,
    private val itineraryRepository: ItineraryRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(TravelAssistantUiState())
    val uiState: StateFlow<TravelAssistantUiState> = _uiState.asStateFlow()

    private var initializedKey: String? = null
    private var itineraryContext: String = ""
    private var nextMessageId = 1L

    fun initialize(tripId: Long?, groupName: String) {
        val normalizedTripId = tripId?.takeIf { it > 0L }
        val normalizedGroupName = groupName.trim()
        val key = "${normalizedTripId ?: -1L}:$normalizedGroupName"
        if (initializedKey == key) return
        initializedKey = key

        val hasItineraryContext = normalizedTripId != null && normalizedGroupName.isNotBlank()
        itineraryContext = if (hasItineraryContext) {
            "Bạn đang hỗ trợ chuyến đi \"$normalizedGroupName\"."
        } else {
            ""
        }

        _uiState.value = TravelAssistantUiState(
            groupName = normalizedGroupName,
            tripId = normalizedTripId,
            messages = listOf(
                TravelAssistantMessageUi(
                    id = nextId(),
                    role = TravelAssistantRole.ASSISTANT,
                    content = if (hasItineraryContext) {
                        "Mình có thể giúp bạn hoàn thiện lịch trình **$normalizedGroupName**, tìm địa điểm và cân đối trải nghiệm cho chuyến đi."
                    } else {
                        "Mình có thể gợi ý địa điểm, đọc dữ liệu review trong Travel Hub và cùng bạn lên kế hoạch chuyến đi."
                    },
                    isLocalIntro = true
                )
            ),
            quickPrompts = if (hasItineraryContext) {
                listOf(
                    "Gợi ý thêm địa điểm phù hợp",
                    "Sắp xếp lịch trình hợp lý hơn",
                    "Gợi ý quán ăn trong chuyến đi"
                )
            } else {
                listOf(
                    "Đi đâu cuối tuần này?",
                    "Gợi ý chuyến đi biển",
                    "Tìm địa điểm được đánh giá cao"
                )
            }
        )

        if (hasItineraryContext) {
            viewModelScope.launch {
                runCatching {
                    itineraryRepository.refreshWorkspace(
                        groupName = normalizedGroupName,
                        tripId = normalizedTripId
                    )
                    itineraryRepository.observeWorkspace(normalizedGroupName).value
                }.onSuccess { workspace ->
                    itineraryContext = buildItineraryContext(workspace)
                }
            }
        }
    }

    fun updateInput(value: String) {
        if (value.length <= 2000) {
            _uiState.update { it.copy(input = value, errorMessage = null) }
        }
    }

    fun sendMessage(message: String = _uiState.value.input) {
        val text = message.trim()
        val state = _uiState.value
        if (text.isBlank() || state.isSending) return

        val history = state.messages
            .filterNot { it.isLocalIntro }
            .takeLast(20)
            .map {
                TravelAssistantMessageRequest(
                    role = if (it.role == TravelAssistantRole.USER) "user" else "assistant",
                    content = it.content
                )
            }
            .toList()

        _uiState.update {
            it.copy(
                input = "",
                isSending = true,
                errorMessage = null,
                messages = it.messages + TravelAssistantMessageUi(
                    id = nextId(),
                    role = TravelAssistantRole.USER,
                    content = text
                )
            )
        }

        viewModelScope.launch {
            runCatching {
                repository.chat(
                    TravelAssistantChatRequest(
                        message = contextualMessage(text),
                        history = history
                    )
                )
            }.onSuccess { response ->
                _uiState.update {
                    it.copy(
                        isSending = false,
                        messages = it.messages + TravelAssistantMessageUi(
                            id = nextId(),
                            role = TravelAssistantRole.ASSISTANT,
                            content = response.answer.ifBlank {
                                "Mình chưa nhận được nội dung trả lời. Bạn thử hỏi lại cụ thể hơn nhé."
                            },
                            places = response.places
                        )
                    )
                }
            }.onFailure { throwable ->
                _uiState.update {
                    it.copy(
                        isSending = false,
                        errorMessage = throwable.message
                            ?: "Không thể kết nối trợ lý du lịch. Vui lòng thử lại."
                    )
                }
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    private fun contextualMessage(message: String): String {
        if (itineraryContext.isBlank()) return message
        val question = message.take(1800)
        val questionLabel = "\n\nCâu hỏi của người dùng: "
        val contextBudget = (2000 - question.length - questionLabel.length).coerceAtLeast(0)
        return itineraryContext.take(contextBudget) + questionLabel + question
    }

    private fun buildItineraryContext(workspace: ItineraryWorkspace): String {
        if (workspace.days.isEmpty()) {
            return "Bạn đang hỗ trợ chuyến đi \"${workspace.groupName}\". Lịch trình hiện chưa có hoạt động."
        }

        val daySummaries = workspace.days.joinToString(separator = "\n") { day ->
            val events = day.events.joinToString(separator = "; ") { event ->
                "${event.startTime}-${event.endTime} ${event.title} tại ${event.placeName}".trim()
            }.ifBlank { "chưa có hoạt động" }
            "${day.label} (${day.dateLabel}): $events"
        }
        return """
            Bạn đang hỗ trợ chuyến đi "${workspace.groupName}".
            Lịch trình hiện tại:
            $daySummaries
        """.trimIndent()
    }

    private fun nextId(): Long = nextMessageId++
}
