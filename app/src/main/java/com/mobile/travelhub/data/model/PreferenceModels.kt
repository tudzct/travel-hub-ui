package com.mobile.travelhub.data.model

import com.google.gson.annotations.SerializedName

data class PreferenceUpdateRequest(
    @SerializedName("trip_type")
    val tripType: String?,
    val interests: List<String>,
    val destination: String?
)

data class PreferenceResponse(
    @SerializedName("user_id")
    val userId: Long,
    @SerializedName("trip_type")
    val tripType: String?,
    val interests: List<String>,
    val destination: String?,
    @SerializedName("updated_at")
    val updatedAt: String
)
