package com.mobile.travelhub.data.model

data class TravelAssistantMessageRequest(
    val role: String,
    val content: String
)

data class TravelAssistantChatRequest(
    val message: String,
    val history: List<TravelAssistantMessageRequest>
)

data class TravelAssistantPlaceReference(
    val id: Long,
    val name: String,
    val province: String? = null,
    val averageRating: Double? = null,
    val reviewCount: Long = 0
)

data class TravelAssistantChatResponse(
    val answer: String,
    val places: List<TravelAssistantPlaceReference> = emptyList()
)
