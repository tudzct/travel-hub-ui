package com.mobile.travelhub.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mobile.travelhub.data.ItineraryRepository
import com.mobile.travelhub.data.model.ItineraryAssistantEvent
import com.mobile.travelhub.data.model.ItineraryChatMessage
import com.mobile.travelhub.data.model.ItineraryChatRole
import com.mobile.travelhub.data.model.ItineraryDay
import com.mobile.travelhub.data.model.ItineraryEvent
import com.mobile.travelhub.data.model.ItineraryEventColors
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class ItineraryViewModel @Inject constructor(
    private val repository: ItineraryRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ItineraryUiState())
    val uiState: StateFlow<ItineraryUiState> = _uiState.asStateFlow()

    private var workspaceJob: Job? = null
    private var boundGroupName: String? = null
    private var lastProposalId: String? = null

    fun bindGroup(groupName: String, openChatOnLaunch: Boolean = false) {
        if (boundGroupName == groupName && !openChatOnLaunch) return
        boundGroupName = groupName
        workspaceJob?.cancel()
        _uiState.update {
            it.copy(
                groupName = groupName,
                isChatSheetOpen = it.isChatSheetOpen || openChatOnLaunch,
                errorMessage = null
            )
        }
        workspaceJob = viewModelScope.launch {
            repository.observeWorkspace(groupName).collect { workspace ->
                val selectedDayIndex = _uiState.value.selectedDayIndex
                    .takeIf { value -> workspace.days.any { it.dayIndex == value } }
                    ?: workspace.days.firstOrNull()?.dayIndex
                    ?: 1

                val nextSelectedIds = if (workspace.pendingProposal?.proposalId != lastProposalId) {
                    workspace.pendingProposal?.changes?.map { it.changeId }?.toSet().orEmpty()
                } else {
                    _uiState.value.selectedChangeIds
                }

                lastProposalId = workspace.pendingProposal?.proposalId
                _uiState.update {
                    it.copy(
                        groupName = workspace.groupName,
                        version = workspace.version,
                        role = workspace.role,
                        days = workspace.days,
                        selectedDayIndex = selectedDayIndex,
                        pendingProposal = workspace.pendingProposal,
                        selectedChangeIds = nextSelectedIds
                    )
                }
            }
        }
    }

    fun selectDay(dayIndex: Int) {
        _uiState.update { it.copy(selectedDayIndex = dayIndex) }
    }

    fun openChat() {
        _uiState.update { it.copy(isChatSheetOpen = true) }
    }

    fun closeChat() {
        _uiState.update { it.copy(isChatSheetOpen = false, thinking = "") }
    }

    fun updateChatInput(value: String) {
        _uiState.update { it.copy(chatInput = value) }
    }

    fun sendChatPrompt() {
        val groupName = boundGroupName ?: return
        val prompt = _uiState.value.chatInput.trim()
        if (prompt.isEmpty()) return

        val userMessage = ItineraryChatMessage(
            id = "user-${System.currentTimeMillis()}",
            role = ItineraryChatRole.USER,
            text = prompt
        )

        _uiState.update {
            it.copy(
                chatMessages = it.chatMessages + userMessage,
                chatInput = "",
                thinking = "",
                isStreaming = true,
                errorMessage = null
            )
        }

        viewModelScope.launch {
            var assistantMessageId: String? = null
            repository.streamProposal(
                groupName = groupName,
                prompt = prompt,
                selectedDayIndex = _uiState.value.selectedDayIndex
            ).collect { event ->
                when (event) {
                    is ItineraryAssistantEvent.Thinking -> {
                        _uiState.update { it.copy(thinking = event.text) }
                    }

                    is ItineraryAssistantEvent.MessageChunk -> {
                        val nextAssistantId = assistantMessageId ?: "assistant-${System.currentTimeMillis()}"
                            .also { assistantMessageId = it }
                        _uiState.update { state ->
                            val existing = state.chatMessages.firstOrNull { it.id == nextAssistantId }
                            if (existing == null) {
                                state.copy(
                                    chatMessages = state.chatMessages + ItineraryChatMessage(
                                        id = nextAssistantId,
                                        role = ItineraryChatRole.ASSISTANT,
                                        text = event.text
                                    )
                                )
                            } else {
                                state.copy(
                                    chatMessages = state.chatMessages.map { message ->
                                        if (message.id == nextAssistantId) {
                                            message.copy(text = message.text + event.text)
                                        } else {
                                            message
                                        }
                                    }
                                )
                            }
                        }
                    }

                    is ItineraryAssistantEvent.ProposalReady -> {
                        _uiState.update { it.copy(thinking = "", isChatSheetOpen = false) }
                    }

                    is ItineraryAssistantEvent.Error -> {
                        _uiState.update {
                            it.copy(
                                isStreaming = false,
                                thinking = "",
                                errorMessage = event.message
                            )
                        }
                    }

                    ItineraryAssistantEvent.Done -> {
                        _uiState.update { it.copy(isStreaming = false, thinking = "") }
                    }
                }
            }
        }
    }

    fun toggleChangeSelection(changeId: String) {
        _uiState.update { state ->
            val next = if (changeId in state.selectedChangeIds) {
                state.selectedChangeIds - changeId
            } else {
                state.selectedChangeIds + changeId
            }
            state.copy(selectedChangeIds = next)
        }
    }

    fun applySelectedChanges() {
        val state = _uiState.value
        val proposal = state.pendingProposal ?: return
        val groupName = boundGroupName ?: return

        repository.applyProposalChanges(
            groupName = groupName,
            proposalId = proposal.proposalId,
            selectedChangeIds = state.selectedChangeIds,
            baseVersion = proposal.baseVersion
        ).onFailure { throwable ->
            _uiState.update { it.copy(errorMessage = throwable.message ?: "Unable to apply changes") }
        }
    }

    fun discardPendingProposal() {
        val groupName = boundGroupName ?: return
        repository.discardPendingProposal(groupName)
        _uiState.update { it.copy(selectedChangeIds = emptySet()) }
    }

    fun startEditing(event: ItineraryEvent) {
        _uiState.update {
            it.copy(
                editingDay = null,
                editingEvent = event,
                isCreatingEvent = false,
                errorMessage = null
            )
        }
    }

    fun startEditingDay(day: ItineraryDay) {
        _uiState.update {
            it.copy(
                editingDay = day,
                editingEvent = null,
                isCreatingEvent = false,
                errorMessage = null
            )
        }
    }

    fun startAddingStop() {
        val state = _uiState.value
        val selectedDay = state.selectedDay ?: return
        val anchorEvent = selectedDay.events.lastOrNull()
        val draftEvent = ItineraryEvent(
            eventId = "manual-${System.currentTimeMillis()}",
            dayIndex = selectedDay.dayIndex,
            startTime = anchorEvent?.endTime ?: "09:00",
            endTime = nextHour(anchorEvent?.endTime ?: "09:00"),
            title = "",
            placeName = "",
            note = "",
            transportToNext = "",
            estimatedCost = "",
            colorHex = ItineraryEventColors.Palette[selectedDay.events.size % ItineraryEventColors.Palette.size]
        )
        _uiState.update {
            it.copy(
                editingEvent = draftEvent,
                isCreatingEvent = true,
                errorMessage = null
            )
        }
    }

    fun cancelEditing() {
        _uiState.update { it.copy(editingEvent = null, isCreatingEvent = false) }
    }

    fun cancelEditingDay() {
        _uiState.update { it.copy(editingDay = null) }
    }

    fun saveEvent(updatedEvent: ItineraryEvent) {
        val groupName = boundGroupName ?: return
        repository.updateEvent(groupName, updatedEvent)
        _uiState.update { it.copy(editingEvent = null, isCreatingEvent = false) }
    }

    fun saveDay(updatedDay: ItineraryDay) {
        val groupName = boundGroupName ?: return
        repository.updateDay(groupName, updatedDay)
        _uiState.update { it.copy(editingDay = null) }
    }

    fun deleteEditingEvent() {
        val groupName = boundGroupName ?: return
        val event = _uiState.value.editingEvent ?: return
        if (_uiState.value.isCreatingEvent) {
            _uiState.update { it.copy(editingEvent = null, isCreatingEvent = false) }
            return
        }
        repository.deleteEvent(groupName, event.eventId)
        _uiState.update { it.copy(editingEvent = null, isCreatingEvent = false) }
    }

    fun deleteEvent(eventId: String) {
        val groupName = boundGroupName ?: return
        repository.deleteEvent(groupName, eventId)
        _uiState.update { it.copy(editingEvent = null, isCreatingEvent = false) }
    }

    fun deleteEditingDay() {
        val groupName = boundGroupName ?: return
        val day = _uiState.value.editingDay ?: return
        repository.deleteDay(groupName, day.dayIndex)
        _uiState.update { it.copy(editingDay = null) }
    }

    fun deleteDay(dayIndex: Int) {
        val groupName = boundGroupName ?: return
        repository.deleteDay(groupName, dayIndex)
        _uiState.update { it.copy(editingDay = null) }
    }

    fun addDay() {
        val groupName = boundGroupName ?: return
        val newDayIndex = repository.addDay(groupName)
        _uiState.update { it.copy(selectedDayIndex = newDayIndex) }
    }

    fun moveEvent(eventId: String, moveUp: Boolean) {
        val groupName = boundGroupName ?: return
        val dayIndex = _uiState.value.selectedDayIndex
        repository.reorderEvent(
            groupName = groupName,
            dayIndex = dayIndex,
            eventId = eventId,
            moveUp = moveUp
        )
    }

    fun reorderDayEvents(fromIndex: Int, toIndex: Int) {
        val groupName = boundGroupName ?: return
        val dayIndex = _uiState.value.selectedDayIndex
        repository.reorderEvents(
            groupName = groupName,
            dayIndex = dayIndex,
            fromIndex = fromIndex,
            toIndex = toIndex
        )
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    private fun nextHour(time: String): String {
        val parts = time.split(":")
        if (parts.size != 2) return time
        val hour = parts[0].toIntOrNull() ?: return time
        val minute = parts[1].toIntOrNull() ?: return time
        val nextHour = (hour + 1).coerceAtMost(23)
        return "%02d:%02d".format(nextHour, minute)
    }
}
