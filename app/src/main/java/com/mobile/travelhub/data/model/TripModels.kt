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
    val startTime: String? = null,
    @SerializedName("end_time")
    val endTime: String? = null,
    val title: String,
    @SerializedName("place_name")
    val placeName: String,
    val note: String? = null,
    @SerializedName("transport_to_next")
    val transportToNext: String? = null,
    @SerializedName("estimated_cost")
    val estimatedCost: String? = null,
    @SerializedName("color_hex")
    val colorHex: Long? = null,
    @SerializedName("icon_name")
    val iconName: String? = null
)

// Trip Detail APIs
data class UpdateTripRequest(
    val name: String,
    val destination: String,
    val startDate: String,
    val endDate: String,
    val budgetMin: Double? = null,
    val budgetMax: Double? = null
)

data class JoinTripRequest(
    val inviteCode: String
)

data class TripInviteCodeResponse(
    val inviteCode: String,
    val inviteLink: String,
    val expiredAt: String? = null
)

data class TripJoinRequestResponse(
    val userId: Long,
    val name: String,
    val avatarUrl: String? = null,
    val requestedAt: String? = null
)

data class UpdateTripMemberRoleRequest(
    val role: String
)

data class JoinTripResultResponse(
    val tripId: Long,
    val status: String,
    val message: String
)

data class TripInfoResponse(
    val id: Long,
    val name: String,
    val location: String,
    val coverImageUrl: String? = null,
    val description: String? = null,
    val startDate: String? = null,
    val endDate: String? = null,
    val budgetMin: Double? = null,
    val budgetMax: Double? = null,
    val status: String? = null,
    val inviteCode: String? = null,
    val maxMembers: Int? = null
)

data class TripMemberResponse(
    val userId: Long,
    val name: String,
    val avatarUrl: String? = null,
    val role: String
)

data class TripActivityItemResponse(
    val id: Long,
    val title: String? = null,
    val description: String? = null,
    val timestamp: String? = null,
    val actorName: String? = null
)

data class TripDetailHighlightsResponse(
    val title: String? = null
)

data class TripDetailResponse(
    val tripInfo: TripInfoResponse,
    val myRole: String,
    val members: List<TripMemberResponse> = emptyList(),
    val highlights: TripDetailHighlightsResponse? = null,
    val recentActivities: List<TripActivityItemResponse> = emptyList()
)

data class CreateTripExpenseRequest(
    val title: String,
    val amount: Double,
    val category: String,
    val paidByUserId: Long
)

data class UpdateTripExpenseRequest(
    val title: String,
    val amount: Double,
    val category: String,
    val paidByUserId: Long
)

data class TripExpenseSummaryResponse(
    val totalAmount: Double? = 0.0,
    val perPersonAmount: Double? = 0.0,
    val myBalance: Double? = 0.0
)

data class TripExpenseContributionResponse(
    val userId: Long? = null,
    val userName: String? = null,
    val avatarUrl: String? = null,
    val amountPaid: Double? = 0.0,
    val percentage: Double? = 0.0
)

data class TripExpenseTransactionResponse(
    val id: Long? = null,
    val title: String? = null,
    val category: String? = null,
    val paidByUserId: Long? = null,
    val paidByName: String? = null,
    val amount: Double? = 0.0,
    val date: String? = null
)

data class TripExpenseResponse(
    val summary: TripExpenseSummaryResponse,
    val contributions: List<TripExpenseContributionResponse> = emptyList(),
    val transactions: List<TripExpenseTransactionResponse> = emptyList()
)
