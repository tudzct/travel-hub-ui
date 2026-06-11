package com.mobile.travelhub.data.model

import com.google.gson.annotations.SerializedName

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
    @SerializedName(value = "mainImage", alternate = ["main_image"])
    val mainImage: String? = null,
    @SerializedName(value = "averageRating", alternate = ["average_rating"])
    val averageRating: Double? = null,
    @SerializedName(value = "reviewCount", alternate = ["review_count"])
    val reviewCount: Long = 0
)

data class TravelAssistantChatResponse(
    val answer: String,
    val places: List<TravelAssistantPlaceReference> = emptyList()
)
