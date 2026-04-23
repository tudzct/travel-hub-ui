package com.mobile.travelhub.data

import com.mobile.travelhub.data.api.PlaceApiService
import com.mobile.travelhub.data.model.PaginationResponse
import com.mobile.travelhub.data.model.TravelPlaceDetailResponse
import com.mobile.travelhub.data.model.TravelPlaceListItemResponse
import com.mobile.travelhub.data.model.TravelPlaceReviewResponse
import com.mobile.travelhub.data.model.TravelPlaceViewHistoryResponse
import com.mobile.travelhub.data.model.UpsertTravelPlaceRequest
import com.mobile.travelhub.data.model.UpsertTravelPlaceReviewRequest
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PlaceRepository @Inject constructor(
    private val placeApiService: PlaceApiService
) {
    suspend fun getPlaces(
        page: Int = 0,
        pageSize: Int = 10,
        provinceId: Long? = null,
        keyword: String? = null
    ): PaginationResponse<TravelPlaceListItemResponse> {
        return placeApiService.getPlaces(page = page, pageSize = pageSize, provinceId = provinceId, keyword = keyword)
    }

    suspend fun getPlaceDetail(placeId: Long): TravelPlaceDetailResponse {
        return placeApiService.getPlaceDetail(placeId)
    }

    suspend fun getReviews(
        placeId: Long,
        page: Int = 0,
        pageSize: Int = 10
    ): PaginationResponse<TravelPlaceReviewResponse> {
        return placeApiService.getReviews(placeId = placeId, page = page, pageSize = pageSize)
    }

    suspend fun upsertReview(
        placeId: Long,
        body: UpsertTravelPlaceReviewRequest
    ): TravelPlaceReviewResponse {
        return placeApiService.upsertReview(placeId = placeId, body = body)
    }

    suspend fun getViewHistory(
        page: Int = 0,
        pageSize: Int = 10
    ): PaginationResponse<TravelPlaceViewHistoryResponse> {
        return placeApiService.getViewHistory(page = page, pageSize = pageSize)
    }

    suspend fun createPlace(body: UpsertTravelPlaceRequest): TravelPlaceDetailResponse {
        return placeApiService.createPlace(body)
    }

    suspend fun updatePlace(
        placeId: Long,
        body: UpsertTravelPlaceRequest
    ): TravelPlaceDetailResponse {
        return placeApiService.updatePlace(placeId = placeId, body = body)
    }
}
