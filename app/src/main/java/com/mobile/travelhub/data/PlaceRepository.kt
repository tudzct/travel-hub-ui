package com.mobile.travelhub.data

import com.mobile.travelhub.data.api.AiRecommendationApiService
import com.mobile.travelhub.data.api.PlaceApiService
import com.mobile.travelhub.data.model.AiTravelPlaceRecommendationRequest
import com.mobile.travelhub.data.model.PaginationResponse
import com.mobile.travelhub.data.model.TravelPlaceDetailResponse
import com.mobile.travelhub.data.model.TravelPlaceListItemResponse
import com.mobile.travelhub.data.model.TravelPlaceReviewResponse
import com.mobile.travelhub.data.model.TravelPlaceViewHistoryResponse
import com.mobile.travelhub.data.model.UpsertTravelPlaceReviewRequest
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PlaceRepository @Inject constructor(
    private val placeApiService: PlaceApiService,
    private val aiRecommendationApiService: AiRecommendationApiService,
    private val authRepository: AuthRepository
) {
    suspend fun getPlaces(
        page: Int = 0,
        pageSize: Int = 10,
        provinceId: Long? = null,
        keyword: String? = null
    ): PaginationResponse<TravelPlaceListItemResponse> {
        return placeApiService.getPlaces(page = page, pageSize = pageSize, provinceId = provinceId, keyword = keyword)
    }

    suspend fun getRecommendedPlaces(
        page: Int = 0,
        pageSize: Int = 10,
        provinceId: Long? = null
    ): PaginationResponse<TravelPlaceListItemResponse> {
        val session = authRepository.getSavedSession()
            ?: error("Cannot load recommendations before login")
        val viewedPlaceIds = placeApiService.getViewHistory(page = 0, pageSize = 20)
            .data
            .map { it.placeId }
            .distinct()

        val recommendationResponse = aiRecommendationApiService.getRecommendedPlaces(
            userId = session.userId.toLong(),
            request = AiTravelPlaceRecommendationRequest(
                viewedPlaceIds = viewedPlaceIds,
                provinceId = provinceId,
                limit = pageSize,
                offset = page * pageSize
            )
        )

        val items = coroutineScope {
            recommendationResponse.items.map { recommendation ->
                async {
                    runCatching { placeApiService.getPlaceDetail(recommendation.travelPlaceId) }
                        .getOrNull()
                        ?.toListItem()
                }
            }.awaitAll().filterNotNull()
        }

        return PaginationResponse(
            pageNumber = page,
            pageSize = pageSize,
            totalPages = if (items.isEmpty()) 0 else page + 1,
            totalElements = items.size.toLong(),
            data = items
        )
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

    private fun TravelPlaceDetailResponse.toListItem(): TravelPlaceListItemResponse {
        return TravelPlaceListItemResponse(
            id = id,
            name = name,
            description = description,
            province = province,
            mainImage = images.firstOrNull { it.main }?.imageUrl ?: images.firstOrNull()?.imageUrl,
            views = views,
            openingTime = openingTime,
            averageRating = reviewSummary.averageRating,
            reviewCount = reviewSummary.reviewCount
        )
    }
}
