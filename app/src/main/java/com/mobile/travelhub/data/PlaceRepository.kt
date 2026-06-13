package com.mobile.travelhub.data

import com.mobile.travelhub.data.api.PlaceApiService
import com.mobile.travelhub.data.api.FeaturedPlaceApiService
import com.mobile.travelhub.data.model.PaginationResponse
import com.mobile.travelhub.data.model.TravelPlaceDetailResponse
import com.mobile.travelhub.data.model.TravelPlaceListItemResponse
import com.mobile.travelhub.data.model.TravelPlaceReviewListSummaryResponse
import com.mobile.travelhub.data.model.TravelPlaceReviewResponse
import com.mobile.travelhub.data.model.TravelPlaceViewHistoryResponse
import com.mobile.travelhub.data.model.UpsertTravelPlaceReviewRequest
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PlaceRepository @Inject constructor(
    private val placeApiService: PlaceApiService,
    private val featuredPlaceApiService: FeaturedPlaceApiService
) {
    suspend fun getPlaces(
        page: Int = 0,
        pageSize: Int = 10,
        provinceId: Long? = null,
        keyword: String? = null
    ): PaginationResponse<TravelPlaceListItemResponse> {
        return placeApiService.getPlaces(page = page, pageSize = pageSize, provinceId = provinceId, keyword = keyword)
    }

    suspend fun getFeaturedPlaces(): List<TravelPlaceListItemResponse> {
        return featuredPlaceApiService.getFeaturedPlaces()
    }

    suspend fun getRecommendedPlaces(
        page: Int = 0,
        pageSize: Int = 10,
        provinceId: Long? = null
    ): PaginationResponse<TravelPlaceListItemResponse> {
        return placeApiService.getRecommendedPlaces(
            page = page,
            pageSize = pageSize,
            provinceId = provinceId
        )
    }

    suspend fun getPlaceDetail(placeId: Long): TravelPlaceDetailResponse {
        return placeApiService.getPlaceDetail(placeId = placeId)
    }

    suspend fun getReviews(
        placeId: Long,
        page: Int = 0,
        pageSize: Int = 10,
        rating: Int? = null,
        sort: String = "NEWEST"
    ): PaginationResponse<TravelPlaceReviewResponse> {
        return placeApiService.getReviews(
            placeId = placeId,
            page = page,
            pageSize = pageSize,
            rating = rating,
            sort = sort
        )
    }

    suspend fun getReviewSummary(placeId: Long): TravelPlaceReviewListSummaryResponse {
        return placeApiService.getReviewSummary(placeId = placeId)
    }

    suspend fun getMyReview(placeId: Long): TravelPlaceReviewResponse? {
        val response = placeApiService.getMyReview(placeId = placeId)
        return if (response.isSuccessful) response.body() else null
    }

    suspend fun upsertReview(
        placeId: Long,
        body: UpsertTravelPlaceReviewRequest
    ): TravelPlaceReviewResponse {
        return placeApiService.upsertReview(placeId = placeId, body = body)
    }

    suspend fun deleteReview(placeId: Long) {
        placeApiService.deleteReview(placeId = placeId)
    }

    suspend fun getViewHistory(
        page: Int = 0,
        pageSize: Int = 10
    ): PaginationResponse<TravelPlaceViewHistoryResponse> {
        return placeApiService.getViewHistory(page = page, pageSize = pageSize)
    }
}
