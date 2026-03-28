package com.mobile.travelhub.models

sealed class StreamEvent {
    data class Thinking(val text: String) : StreamEvent()
    data class Message(val text: String) : StreamEvent()
    object Done : StreamEvent()
}

data class UiState(
    val thinking: String = "",
    val answer: String = "",
    val isStreaming: Boolean = true
)