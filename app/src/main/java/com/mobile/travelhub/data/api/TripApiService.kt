package com.mobile.travelhub.data.api

import com.mobile.travelhub.data.model.CreateTripRequest
import com.mobile.travelhub.data.model.TripDashboardResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface TripApiService {
    @GET("api/users/me/dashboard")
    suspend fun getDashboard(): TripDashboardResponse

    @POST("api/trips")
    suspend fun createTrip(
        @Body request: CreateTripRequest
    ): Long
}