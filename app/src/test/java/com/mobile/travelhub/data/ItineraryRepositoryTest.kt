package com.mobile.travelhub.data

import com.mobile.travelhub.data.api.CreateTripActivityRequestDto
import com.mobile.travelhub.data.api.ItineraryApiService
import com.mobile.travelhub.data.model.TripActivityResponse
import com.mobile.travelhub.data.model.TripDayResponse
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test

class ItineraryRepositoryTest {

    @Test
    fun refreshWorkspace_mapsTripDaysAndActivitiesToItineraryWorkspace() = runBlocking {
        val repository = ItineraryRepository(FakeItineraryApiService())

        repository.refreshWorkspace(groupName = "Da Nang", tripId = 42)

        val workspace = repository.observeWorkspace("Da Nang").value
        assertEquals(listOf(1), workspace.days.map { it.dayIndex })
        assertEquals("01/06/2026", workspace.days.first().dateLabel)
        assertEquals(listOf("Breakfast", "Museum"), workspace.days.first().events.map { it.title })
    }

    @Test
    fun reorderEvents_updatesActivityOrderIndex() = runBlocking {
        val api = FakeItineraryApiService()
        val repository = ItineraryRepository(api)
        repository.refreshWorkspace(groupName = "Da Nang", tripId = 42)

        repository.reorderEvents(
            groupName = "Da Nang",
            dayIndex = 1,
            fromIndex = 0,
            toIndex = 1
        )

        assertEquals(10, api.lastUpdatedActivityId)
        assertEquals(2, api.lastUpdateRequest?.orderIndex)
        assertEquals("2026-06-01", api.lastUpdateRequest?.date)
    }

    private class FakeItineraryApiService : ItineraryApiService {
        var lastUpdatedActivityId: Long? = null
        var lastUpdateRequest: CreateTripActivityRequestDto? = null

        private val days = listOf(
            TripDayResponse(
                id = 1,
                tripId = 42,
                date = "2026-06-01",
                dayNumber = 1,
                activities = listOf(
                    TripActivityResponse(
                        id = 10,
                        tripDayId = 1,
                        title = "Breakfast",
                        description = "Local food",
                        startTime = "08:00:00",
                        endTime = "09:00:00",
                        locationName = "Market",
                        address = "Da Nang",
                        type = "FOOD",
                        orderIndex = 1,
                        estimatedCost = 100000.0
                    ),
                    TripActivityResponse(
                        id = 11,
                        tripDayId = 1,
                        title = "Museum",
                        description = "Visit museum",
                        startTime = "10:00:00",
                        endTime = "11:00:00",
                        locationName = "Museum",
                        address = "Da Nang",
                        type = "PLACE",
                        orderIndex = 2,
                        estimatedCost = 0.0
                    )
                )
            )
        )

        override suspend fun listTripDays(tripId: Long): List<TripDayResponse> = days

        override suspend fun createTripActivity(
            tripId: Long,
            request: CreateTripActivityRequestDto
        ): TripActivityResponse {
            return TripActivityResponse(
                id = 12,
                tripDayId = 1,
                title = request.title,
                description = request.description,
                startTime = request.startTime,
                endTime = request.endTime,
                locationName = request.locationName,
                address = request.address,
                type = request.type,
                orderIndex = request.orderIndex,
                estimatedCost = request.estimatedCost
            )
        }

        override suspend fun updateTripActivity(
            tripId: Long,
            activityId: Long,
            request: CreateTripActivityRequestDto
        ): TripActivityResponse {
            lastUpdatedActivityId = activityId
            lastUpdateRequest = request
            return TripActivityResponse(
                id = activityId,
                tripDayId = 1,
                title = request.title,
                description = request.description,
                startTime = request.startTime,
                endTime = request.endTime,
                locationName = request.locationName,
                address = request.address,
                type = request.type,
                orderIndex = request.orderIndex,
                estimatedCost = request.estimatedCost
            )
        }

        override suspend fun deleteTripActivity(tripId: Long, activityId: Long) = Unit
    }
}
