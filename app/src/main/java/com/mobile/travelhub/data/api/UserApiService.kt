package com.mobile.travelhub.data.api

import com.mobile.travelhub.data.model.PageResponse
import com.mobile.travelhub.data.model.PaginationResponse
import com.mobile.travelhub.data.model.ProfileUpdateRequest
import com.mobile.travelhub.data.model.BankAccountRequest
import com.mobile.travelhub.data.model.BankAccountResponse
import com.mobile.travelhub.data.model.ChangePasswordRequest
import com.mobile.travelhub.data.model.GetPostsResponse
import com.mobile.travelhub.data.model.UserProfileResponse
import com.mobile.travelhub.data.model.UserSummaryResponse
import com.mobile.travelhub.data.model.TopTravelerPeriod
import com.mobile.travelhub.data.model.TopTravelerResponse
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface UserApiService {
    @GET("api/users/me")
    suspend fun getMyProfile(): UserProfileResponse

    @GET("api/users/{id}")
    suspend fun getUserProfile(@Path("id") id: Long): UserProfileResponse

    @GET("api/users/search")
    suspend fun searchUsers(
        @Query("username") username: String,
        @Query("page") page: Int = 0,
        @Query("pageSize") pageSize: Int = 10
    ): PaginationResponse<UserProfileResponse>

    @GET("api/users/top-travelers")
    suspend fun getTopTravelers(
        @Query("period") period: TopTravelerPeriod,
        @Query("page") page: Int = 0,
        @Query("pageSize") pageSize: Int = 4
    ): PaginationResponse<TopTravelerResponse>

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

    @GET("api/users/{id}/posts")
    suspend fun getUserPosts(
        @Path("id") id: Long,
        @Query("page") page: Int = 0,
        @Query("pageSize") pageSize: Int = 20
    ): GetPostsResponse

    @GET("api/users/{id}/liked-posts")
    suspend fun getUserLikedPosts(
        @Path("id") id: Long,
        @Query("page") page: Int = 0,
        @Query("pageSize") pageSize: Int = 20
    ): GetPostsResponse

    @GET("api/users/{id}/saved-posts")
    suspend fun getUserSavedPosts(
        @Path("id") id: Long,
        @Query("page") page: Int = 0,
        @Query("pageSize") pageSize: Int = 20
    ): GetPostsResponse

    @PUT("api/users/me")
    suspend fun updateMyProfile(
        @Body request: ProfileUpdateRequest
    ): UserProfileResponse

    @PUT("api/users/me/password")
    suspend fun changePassword(
        @Body request: ChangePasswordRequest
    )

    @PUT("api/users/{id}")
    suspend fun updateProfile(
        @Path("id") id: Long,
        @Body request: ProfileUpdateRequest
    ): UserProfileResponse

    @PUT("api/me/bank-accounts/default")
    suspend fun upsertDefaultBankAccount(
        @Body request: BankAccountRequest
    ): BankAccountResponse

    @POST("api/users/{targetUserId}/follow")
    suspend fun followUser(
        @Path("targetUserId") targetUserId: Long
    )

    @DELETE("api/users/{targetUserId}/follow")
    suspend fun unfollowUser(
        @Path("targetUserId") targetUserId: Long
    )

}
