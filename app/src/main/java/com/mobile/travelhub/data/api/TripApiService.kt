package com.mobile.travelhub.data.api

import com.mobile.travelhub.data.model.CreateTripRequest
import com.mobile.travelhub.data.model.TripDashboardResponse
import com.mobile.travelhub.data.model.TripDetailResponse
import com.mobile.travelhub.data.model.UpdateTripRequest
import com.mobile.travelhub.data.model.JoinTripRequest
import com.mobile.travelhub.data.model.JoinTripResultResponse
import com.mobile.travelhub.data.model.SettlementResponse
import com.mobile.travelhub.data.model.TripInviteCodeResponse
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface TripApiService {
    @GET("api/users/me/dashboard")
    suspend fun getDashboard(): TripDashboardResponse

    @POST("api/trips")
    suspend fun createTrip(
        @Body request: CreateTripRequest
    ): TripDetailResponse

    @GET("api/trips/{tripId}")
    suspend fun getTripDetail(
        @Path("tripId") tripId: Long
    ): TripDetailResponse

    @PUT("api/trips/{tripId}")
    suspend fun updateTrip(
        @Path("tripId") tripId: Long,
        @Body request: UpdateTripRequest
    ): Unit

    @DELETE("api/trips/{tripId}")
    suspend fun deleteTrip(
        @Path("tripId") tripId: Long
    ): Unit

    @POST("api/trips/join")
    suspend fun joinTrip(
        @Body request: JoinTripRequest
    ): JoinTripResultResponse

    @GET("api/trips/invite/{code}")
    suspend fun getTripByInviteCode(
        @Path("code") code: String
    ): com.mobile.travelhub.data.model.TripInfoResponse

    @GET("api/trips/{tripId}/invite-code")
    suspend fun getInviteCode(
        @Path("tripId") tripId: Long
    ): TripInviteCodeResponse

    @POST("api/trips/{tripId}/invite-code/regenerate")
    suspend fun regenerateInviteCode(
        @Path("tripId") tripId: Long
    ): TripInviteCodeResponse

    @POST("api/trips/{tripId}/finish")
    suspend fun finishTrip(
        @Path("tripId") tripId: Long
    ): List<SettlementResponse>

    @GET("api/trips/{tripId}/settlements")
    suspend fun listSettlements(
        @Path("tripId") tripId: Long
    ): List<SettlementResponse>
}
