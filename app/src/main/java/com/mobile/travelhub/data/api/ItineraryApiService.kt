package com.mobile.travelhub.data.api

import com.mobile.travelhub.data.model.ItineraryResponse
import retrofit2.http.GET
import retrofit2.http.Path

interface ItineraryApiService {
    @GET("api/itineraries/by-group/{groupName}")
    suspend fun getItineraryByGroupName(
        @Path("groupName") groupName: String
    ): ItineraryResponse
}