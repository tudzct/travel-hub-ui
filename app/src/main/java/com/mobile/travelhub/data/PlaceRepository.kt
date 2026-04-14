package com.mobile.travelhub.data

import com.mobile.travelhub.data.api.PlaceApiFactory
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
    private val authRepository: AuthRepository
) {
    private val api: PlaceApiService by lazy {
        PlaceApiFactory.create(accessTokenProvider = authRepository::getAccessToken)
    }

    suspend fun getPlaces(
        page: Int = 0,
        pageSize: Int = 10,
        provinceId: Long? = null,
        keyword: String? = null
    ): PaginationResponse<TravelPlaceListItemResponse> {
        return api.getPlaces(page = page, pageSize = pageSize, provinceId = provinceId, keyword = keyword)
    }

    suspend fun getPlaceDetail(placeId: Long): TravelPlaceDetailResponse {
        return api.getPlaceDetail(placeId)
    }

    suspend fun getReviews(
        placeId: Long,
        page: Int = 0,
        pageSize: Int = 10
    ): PaginationResponse<TravelPlaceReviewResponse> {
        return api.getReviews(placeId = placeId, page = page, pageSize = pageSize)
    }

    suspend fun upsertReview(
        placeId: Long,
        body: UpsertTravelPlaceReviewRequest
    ): TravelPlaceReviewResponse {
        return api.upsertReview(placeId = placeId, body = body)
    }

    suspend fun getViewHistory(
        page: Int = 0,
        pageSize: Int = 10
    ): PaginationResponse<TravelPlaceViewHistoryResponse> {
        return api.getViewHistory(page = page, pageSize = pageSize)
    }

    suspend fun createPlace(body: UpsertTravelPlaceRequest): TravelPlaceDetailResponse {
        return api.createPlace(body)
    }

    suspend fun updatePlace(
        placeId: Long,
        body: UpsertTravelPlaceRequest
    ): TravelPlaceDetailResponse {
        return api.updatePlace(placeId = placeId, body = body)
    }
}
