package com.mobile.travelhub.models

sealed class StreamEvent {
    data class Thinking(val text: String) : StreamEvent()
    data class Message(val text: String) : StreamEvent()
    data class Error(val message: String) : StreamEvent()
    object Done : StreamEvent()
}

data class UiState(
    val thinking: String = "",
    val answer: String = "",
    val error: String? = null,
    val isStreaming: Boolean = true
)
