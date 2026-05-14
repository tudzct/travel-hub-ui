package com.mobile.travelhub.data.api

import com.mobile.travelhub.data.model.TripJoinRequestResponse
import com.mobile.travelhub.data.model.TripMemberResponse
import com.mobile.travelhub.data.model.UpdateTripMemberRoleRequest
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.PUT

interface TripMemberApiService {
    @GET("api/trips/{tripId}/requests")
    suspend fun getJoinRequests(
        @Path("tripId") tripId: Long
    ): List<TripJoinRequestResponse>

    @POST("api/trips/{tripId}/requests/{userId}/approve")
    suspend fun approveRequest(
        @Path("tripId") tripId: Long,
        @Path("userId") userId: Long
    ): Unit

    @POST("api/trips/{tripId}/requests/{userId}/reject")
    suspend fun rejectRequest(
        @Path("tripId") tripId: Long,
        @Path("userId") userId: Long
    ): Unit

    @DELETE("api/trips/{tripId}/members/{userId}")
    suspend fun removeMember(
        @Path("tripId") tripId: Long,
        @Path("userId") userId: Long
    ): Unit

    @POST("api/trips/{tripId}/leave")
    suspend fun leaveTrip(
        @Path("tripId") tripId: Long
    ): Unit

    @PUT("api/trips/{tripId}/members/{userId}/role")
    suspend fun updateMemberRole(
        @Path("tripId") tripId: Long,
        @Path("userId") userId: Long,
        @Body request: UpdateTripMemberRoleRequest
    ): TripMemberResponse
}