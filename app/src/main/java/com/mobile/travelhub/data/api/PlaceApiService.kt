package com.mobile.travelhub.data.api

import com.mobile.travelhub.data.model.PaginationResponse
import com.mobile.travelhub.data.model.ProvinceResponse
import com.mobile.travelhub.data.model.TravelPlaceDetailResponse
import com.mobile.travelhub.data.model.TravelPlaceListItemResponse
import com.mobile.travelhub.data.model.TravelPlaceReviewResponse
import com.mobile.travelhub.data.model.TravelPlaceViewHistoryResponse
import com.mobile.travelhub.data.model.UpsertTravelPlaceRequest
import com.mobile.travelhub.data.model.UpsertTravelPlaceReviewRequest
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface PlaceApiService {

    @GET("api/places")
    suspend fun getPlaces(
        @Query("page") page: Int = 0,
        @Query("pageSize") pageSize: Int = 10,
        @Query("provinceId") provinceId: Long? = null,
        @Query("keyword") keyword: String? = null
    ): PaginationResponse<TravelPlaceListItemResponse>

    @GET("api/places/{placeId}")
    suspend fun getPlaceDetail(
        @Path("placeId") placeId: Long
    ): TravelPlaceDetailResponse

    @GET("api/places/{placeId}/reviews")
    suspend fun getReviews(
        @Path("placeId") placeId: Long,
        @Query("page") page: Int = 0,
        @Query("pageSize") pageSize: Int = 10
    ): PaginationResponse<TravelPlaceReviewResponse>

    @PUT("api/places/{placeId}/review")
    suspend fun upsertReview(
        @Path("placeId") placeId: Long,
        @Body body: UpsertTravelPlaceReviewRequest
    ): TravelPlaceReviewResponse

    @GET("api/users/me/place-view-history")
    suspend fun getViewHistory(
        @Query("page") page: Int = 0,
        @Query("pageSize") pageSize: Int = 10
    ): PaginationResponse<TravelPlaceViewHistoryResponse>

    @POST("api/admin/places")
    suspend fun createPlace(
        @Body body: UpsertTravelPlaceRequest
    ): TravelPlaceDetailResponse

    @PUT("api/admin/places/{placeId}")
    suspend fun updatePlace(
        @Path("placeId") placeId: Long,
        @Body body: UpsertTravelPlaceRequest
    ): TravelPlaceDetailResponse
}
