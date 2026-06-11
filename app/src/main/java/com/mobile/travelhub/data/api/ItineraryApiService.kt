package com.mobile.travelhub.data.api

import com.mobile.travelhub.data.model.TripActivityResponse
import com.mobile.travelhub.data.model.TripDayResponse
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface ItineraryApiService {
    @GET("/api/trips/{tripId}/days")
    suspend fun listTripDays(@Path("tripId") tripId: Long): List<TripDayResponse>

    @POST("/api/trips/{tripId}/activities")
    suspend fun createTripActivity(
        @Path("tripId") tripId: Long,
        @Body request: CreateTripActivityRequestDto
    ): TripActivityResponse

    @PUT("/api/trips/{tripId}/activities/{activityId}")
    suspend fun updateTripActivity(
        @Path("tripId") tripId: Long,
        @Path("activityId") activityId: Long,
        @Body request: UpdateTripActivityRequestDto
    ): TripActivityResponse

    @DELETE("/api/trips/{tripId}/activities/{activityId}")
    suspend fun deleteTripActivity(
        @Path("tripId") tripId: Long,
        @Path("activityId") activityId: Long
    )
}

data class CreateTripActivityRequestDto(
    val date: String,
    val title: String,
    val description: String,
    val startTime: String,
    val endTime: String,
    val locationName: String,
    val address: String,
    val type: String,
    val orderIndex: Int
)

typealias UpdateTripActivityRequestDto = CreateTripActivityRequestDto
