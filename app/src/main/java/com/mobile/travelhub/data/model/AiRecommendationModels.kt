package com.mobile.travelhub.data.model

import com.google.gson.annotations.SerializedName

data class AiTravelPlaceRecommendationRequest(
    @SerializedName("viewedPlaceIds")
    val viewedPlaceIds: List<Long> = emptyList(),
    @SerializedName("provinceId")
    val provinceId: Long? = null,
    val limit: Int = 10,
    val offset: Int = 0
)

data class AiTravelPlaceRecommendationResponse(
    @SerializedName("userId")
    val userId: Long,
    val items: List<AiTravelPlaceRecommendationItem> = emptyList()
)

data class AiTravelPlaceRecommendationItem(
    @SerializedName("travelPlaceId")
    val travelPlaceId: Long,
    val score: Double,
    @SerializedName("preferenceScore")
    val preferenceScore: Double,
    @SerializedName("historyScore")
    val historyScore: Double,
    @SerializedName("popularityScore")
    val popularityScore: Double
)
