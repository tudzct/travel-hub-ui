package com.mobile.travelhub.data.model

data class PaginationResponse<T>(
    val pageNumber: Int = 0,
    val pageSize: Int = 0,
    val totalPages: Int = 0,
    val totalElements: Long = 0,
    val data: List<T> = emptyList()
)

data class ProvinceResponse(
    val id: Long,
    val name: String,
    val image: String?
)

data class TravelPlaceListItemResponse(
    val id: Long,
    val name: String,
    val description: String?,
    val province: ProvinceResponse,
    val mainImage: String?,
    val views: Int?,
    val openingTime: String?,
    val averageRating: Double,
    val reviewCount: Long
)

data class TravelPlaceImageResponse(
    val id: Long,
    val imageUrl: String,
    val main: Boolean
)

data class TravelPlaceReviewSummaryResponse(
    val averageRating: Double,
    val reviewCount: Long
)

data class TravelPlaceReviewAuthorResponse(
    val id: Long,
    val name: String,
    val username: String,
    val avatarUrl: String?
)

data class TravelPlaceReviewResponse(
    val id: Long,
    val user: TravelPlaceReviewAuthorResponse,
    val rating: Int,
    val content: String,
    val createdAt: String?,
    val updatedAt: String?
)

data class TravelPlaceDetailResponse(
    val id: Long,
    val name: String,
    val description: String?,
    val lat: Double?,
    val lon: Double?,
    val views: Int?,
    val openingTime: String?,
    val province: ProvinceResponse,
    val images: List<TravelPlaceImageResponse>,
    val reviewSummary: TravelPlaceReviewSummaryResponse,
    val myReview: TravelPlaceReviewResponse?
)

data class TravelPlaceViewHistoryResponse(
    val placeId: Long,
    val placeName: String,
    val mainImage: String?,
    val provinceName: String,
    val viewedAt: String?
)

data class UpsertTravelPlaceReviewRequest(
    val rating: Int,
    val content: String
)

data class UpsertTravelPlaceRequest(
    val provinceId: Long,
    val name: String,
    val description: String?,
    val lat: Double?,
    val lon: Double?,
    val openingTime: String?,
    val imageUrls: List<String>
)
