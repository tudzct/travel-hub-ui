package com.mobile.travelhub.data.api

import com.mobile.travelhub.data.model.TravelAssistantChatRequest
import com.mobile.travelhub.data.model.TravelAssistantChatResponse
import retrofit2.http.Body
import retrofit2.http.POST

interface TravelAssistantApiService {
    @POST("/api/travel-assistant/chat")
    suspend fun chat(
        @Body request: TravelAssistantChatRequest
    ): TravelAssistantChatResponse
}
