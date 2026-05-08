package com.mobile.travelhub.data.model

import com.google.gson.annotations.SerializedName

data class CreateTripRequest(
    val name: String,
    val destination: String,
    val startDate: String,
    val endDate: String,
    val budgetMin: Double? = null,
    val budgetMax: Double? = null
)

data class TripDashboardResponse(
    val activeTrip: ActiveTripResponse? = null,
    val upcomingTrips: List<UpcomingTripResponse> = emptyList(),
    val pastTrips: List<PastTripResponse> = emptyList()
)

data class ActiveTripResponse(
    val tripId: Long,
    val name: String,
    val location: String,
    val coverImageUrl: String? = null,
    val startDate: String? = null,
    val endDate: String? = null
)

data class UpcomingTripResponse(
    val tripId: Long,
    val name: String,
    val location: String,
    val coverImageUrl: String? = null,
    val daysLeft: Int = 0,
    val memberCount: Int = 0
)

data class PastTripResponse(
    val tripId: Long,
    val locationName: String,
    val dateString: String,
    val imageUrl: String? = null
)

data class ItineraryResponse(
    val id: Long,
    @SerializedName("group_name")
    val groupName: String,
    val version: Int,
    @SerializedName("owner_id")
    val ownerId: Long,
    val days: List<ItineraryDayResponse> = emptyList(),
    @SerializedName("created_at")
    val createdAt: String? = null,
    @SerializedName("updated_at")
    val updatedAt: String? = null
)

data class ItineraryDayResponse(
    val id: Long,
    @SerializedName("day_index")
    val dayIndex: Int,
    val label: String,
    @SerializedName("date_label")
    val dateLabel: String,
    val stops: List<ItineraryStopResponse> = emptyList()
)

data class ItineraryStopResponse(
    val id: Long,
    @SerializedName("sort_order")
    val sortOrder: Int,
    @SerializedName("start_time")
    val startTime: String,
    @SerializedName("end_time")
    val endTime: String,
    val title: String,
    @SerializedName("place_name")
    val placeName: String,
    val note: String? = null,
    @SerializedName("transport_to_next")
    val transportToNext: String? = null,
    @SerializedName("estimated_cost")
    val estimatedCost: String? = null,
    val highlighted: Boolean = false
)