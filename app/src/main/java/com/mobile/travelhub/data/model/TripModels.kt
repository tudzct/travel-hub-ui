package com.mobile.travelhub.data.model

data class CreateTripRequest(
    val name: String,
    val destination: String,
    val startDate: String,
    val endDate: String,
    val coverImageUrl: String? = null,
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

data class TripDayResponse(
    val id: Long,
    val tripId: Long,
    val date: String,
    val dayNumber: Int,
    val activities: List<TripActivityResponse> = emptyList()
)

data class TripActivityResponse(
    val id: Long,
    val tripDayId: Long,
    val title: String,
    val description: String? = null,
    val startTime: String? = null,
    val endTime: String? = null,
    val locationName: String? = null,
    val address: String? = null,
    val type: String? = null,
    val orderIndex: Int? = null,
    val estimatedCost: Double? = null
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

data class TripDetailResponse(
    val tripInfo: TripInfoResponse,
    val myRole: String,
    val members: List<TripMemberResponse> = emptyList()
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
