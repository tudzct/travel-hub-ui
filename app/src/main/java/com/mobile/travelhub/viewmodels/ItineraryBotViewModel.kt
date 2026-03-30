package com.mobile.travelhub.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mobile.travelhub.models.StreamEvent
import com.mobile.travelhub.models.UiState
import com.mobile.travelhub.usecase.ItineraryBotUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ItineraryBotViewModel @Inject constructor(
    private val useCase: ItineraryBotUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(UiState())
    val state: StateFlow<UiState> = _state

    private val answerBuffer = StringBuilder()
    private val thinkingBuffer = StringBuilder()

    fun start() {
        viewModelScope.launch {
            useCase.execute().collect { event ->
                when (event) {

                    is StreamEvent.Thinking -> {
                        thinkingBuffer.append(event.text)
                        emitState()
                    }

                    is StreamEvent.Message -> {
                        answerBuffer.append(event.text)
                        emitState()
                    }

                    StreamEvent.Done -> {
                        _state.update {
                            it.copy(isStreaming = false)
                        }
                    }
                }
            }
        }
    }

    private var lastEmit = 0L

    private fun emitState() {
        val now = System.currentTimeMillis()

        if (now - lastEmit < 50) return // batch 50ms

        lastEmit = now

        _state.update {
            it.copy(
                thinking = thinkingBuffer.toString(),
                answer = answerBuffer.toString()
            )
        }
    }
}
