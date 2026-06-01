package com.mobile.travelhub.data.api

import com.mobile.travelhub.data.model.TravelPlaceListItemResponse
import retrofit2.http.GET

interface FeaturedPlaceApiService {
    @GET("api/places/featured")
    suspend fun getFeaturedPlaces(): List<TravelPlaceListItemResponse>
}
