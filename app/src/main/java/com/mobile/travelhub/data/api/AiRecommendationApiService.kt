package com.mobile.travelhub.data.api

import com.mobile.travelhub.data.model.AiTravelPlaceRecommendationRequest
import com.mobile.travelhub.data.model.AiTravelPlaceRecommendationResponse
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Path

interface AiRecommendationApiService {
    @POST("api/users/{userId}/travel-place-recommendations")
    suspend fun getRecommendedPlaces(
        @Path("userId") userId: Long,
        @Body request: AiTravelPlaceRecommendationRequest
    ): AiTravelPlaceRecommendationResponse
}
