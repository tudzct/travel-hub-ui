package com.mobile.travelhub.data.api

import com.mobile.travelhub.data.model.PreferenceResponse
import com.mobile.travelhub.data.model.PreferenceUpdateRequest
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PUT
import retrofit2.http.Path

interface AIApiService {
    @PUT("api/users/{id}/preferences")
    suspend fun updatePreferences(
        @Path("id") id: Long,
        @Body request: PreferenceUpdateRequest
    ): PreferenceResponse

    @GET("api/users/{id}/preferences")
    suspend fun getPreferences(
        @Path("id") id: Long
    ): PreferenceResponse
}
