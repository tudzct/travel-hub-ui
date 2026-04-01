package com.mobile.travelhub.data.api

import com.mobile.travelhub.data.model.PageResponse
import com.mobile.travelhub.data.model.ProfileUpdateRequest
import com.mobile.travelhub.data.model.UserProfileResponse
import com.mobile.travelhub.data.model.UserSummaryResponse
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface TravelHubApiService {

    @GET("api/users/{id}")
    suspend fun getUserProfile(@Path("id") id: Long): UserProfileResponse

    @GET("api/users/{id}/followers")
    suspend fun getFollowers(
        @Path("id") id: Long,
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 30
    ): PageResponse<UserSummaryResponse>

    @GET("api/users/{id}/following")
    suspend fun getFollowing(
        @Path("id") id: Long,
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 30
    ): PageResponse<UserSummaryResponse>

    @PUT("api/users/{id}")
    suspend fun updateProfile(
        @Path("id") id: Long,
        @Body request: ProfileUpdateRequest
    ): UserProfileResponse

    @POST("api/users/{targetUserId}/follow")
    suspend fun followUser(
        @Path("targetUserId") targetUserId: Long
    )

    @DELETE("api/users/{targetUserId}/follow")
    suspend fun unfollowUser(
        @Path("targetUserId") targetUserId: Long
    )
}
