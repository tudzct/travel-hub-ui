package com.mobile.travelhub.data.api

import com.mobile.travelhub.data.model.CreateTripRequest
import com.mobile.travelhub.data.model.AddTripPhotosRequest
import com.mobile.travelhub.data.model.CreateTripPostRequest
import com.mobile.travelhub.data.model.PostResponse
import com.mobile.travelhub.data.model.PastTripsPageResponse
import com.mobile.travelhub.data.model.TripDashboardResponse
import com.mobile.travelhub.data.model.TripDetailResponse
import com.mobile.travelhub.data.model.TripPhotoResponse
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
import retrofit2.http.Query

interface TripApiService {
    @GET("api/users/me/dashboard")
    suspend fun getDashboard(): TripDashboardResponse

    @GET("api/users/me/past-trips")
    suspend fun getPastTrips(
        @Query("page") page: Int = 0,
        @Query("pageSize") pageSize: Int = 5
    ): PastTripsPageResponse

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

    @GET("api/trips/{tripId}/photos")
    suspend fun getTripPhotos(
        @Path("tripId") tripId: Long
    ): List<TripPhotoResponse>

    @POST("api/trips/{tripId}/photos")
    suspend fun addTripPhotos(
        @Path("tripId") tripId: Long,
        @Body request: AddTripPhotosRequest
    ): List<TripPhotoResponse>

    @POST("api/trips/{tripId}/publish-post")
    suspend fun publishTripPost(
        @Path("tripId") tripId: Long,
        @Body request: CreateTripPostRequest
    ): PostResponse
}
