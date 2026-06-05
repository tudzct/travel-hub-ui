package com.mobile.travelhub.data

import com.mobile.travelhub.data.api.TravelAssistantApiService
import com.mobile.travelhub.data.model.TravelAssistantChatRequest
import com.mobile.travelhub.data.model.TravelAssistantChatResponse
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TravelAssistantRepository @Inject constructor(
    private val apiService: TravelAssistantApiService
) {
    suspend fun chat(request: TravelAssistantChatRequest): TravelAssistantChatResponse {
        return apiService.chat(request)
    }
}
