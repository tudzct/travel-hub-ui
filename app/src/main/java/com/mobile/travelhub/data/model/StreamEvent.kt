package com.mobile.travelhub.data.model

sealed class StreamEvent {
    data class Thinking(val text: String) : StreamEvent()
    data class Message(val text: String) : StreamEvent()
    data class Error(val message: String) : StreamEvent()
    data object Done : StreamEvent()
}
