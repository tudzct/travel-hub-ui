package com.mobile.travelhub.viewmodels

data class ItineraryBotUiState(
    val thinking: String = "",
    val answer: String = "",
    val error: String? = null,
    val isStreaming: Boolean = true
)
