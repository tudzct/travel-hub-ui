package com.mobile.travelhub.data

import com.mobile.travelhub.data.api.ItineraryApiService
import com.mobile.travelhub.data.api.TripApiService
import com.mobile.travelhub.data.model.CreateTripRequest
import com.mobile.travelhub.data.model.ItineraryResponse
import com.mobile.travelhub.data.model.TripDashboardResponse
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TripRepository @Inject constructor(
    private val tripApiService: TripApiService,
    private val itineraryApiService: ItineraryApiService
) {
    suspend fun getDashboard(): Result<TripDashboardResponse> {
        return runCatching { tripApiService.getDashboard() }
    }

    suspend fun createTrip(request: CreateTripRequest): Result<Long> {
        return runCatching { tripApiService.createTrip(request) }
    }

    suspend fun getItineraryByGroupName(groupName: String): Result<ItineraryResponse> {
        return runCatching { itineraryApiService.getItineraryByGroupName(groupName) }
    }
}