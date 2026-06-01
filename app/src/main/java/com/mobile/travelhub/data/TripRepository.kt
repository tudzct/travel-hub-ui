package com.mobile.travelhub.data

import com.mobile.travelhub.data.api.TripExpenseApiService
import com.mobile.travelhub.data.api.TripMemberApiService
import com.mobile.travelhub.data.api.ItineraryApiService
import com.mobile.travelhub.data.api.TripApiService
import com.mobile.travelhub.data.model.CreateTripRequest
import com.mobile.travelhub.data.model.CreateTripExpenseRequest
import com.mobile.travelhub.data.model.TripExpenseResponse
import com.mobile.travelhub.data.model.TripDashboardResponse
import com.mobile.travelhub.data.model.TripDayResponse
import com.mobile.travelhub.data.model.TripDetailResponse
import com.mobile.travelhub.data.model.TripExpenseTransactionResponse
import com.mobile.travelhub.data.model.TripJoinRequestResponse
import com.mobile.travelhub.data.model.TripMemberResponse
import com.mobile.travelhub.data.model.UpdateTripMemberRoleRequest
import com.mobile.travelhub.data.model.UpdateTripRequest
import com.mobile.travelhub.data.model.JoinTripRequest
import com.mobile.travelhub.data.model.JoinTripResultResponse
import com.mobile.travelhub.data.model.TripInviteCodeResponse
import com.mobile.travelhub.data.model.UpdateTripExpenseRequest
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TripRepository @Inject constructor(
    private val tripApiService: TripApiService,
    private val itineraryApiService: ItineraryApiService,
    private val tripMemberApiService: TripMemberApiService,
    private val tripExpenseApiService: TripExpenseApiService
) {
    suspend fun getDashboard(): Result<TripDashboardResponse> {
        return runCatching { tripApiService.getDashboard() }
    }

    suspend fun createTrip(request: CreateTripRequest): Result<Long> {
        return runCatching { tripApiService.createTrip(request) }
    }

    suspend fun getTripDetail(tripId: Long): Result<TripDetailResponse> {
        return runCatching { tripApiService.getTripDetail(tripId) }
    }

    suspend fun updateTrip(tripId: Long, request: UpdateTripRequest): Result<Unit> {
        return runCatching { tripApiService.updateTrip(tripId, request) }
    }

    suspend fun deleteTrip(tripId: Long): Result<Unit> {
        return runCatching { tripApiService.deleteTrip(tripId) }
    }

    suspend fun joinTrip(request: JoinTripRequest): Result<JoinTripResultResponse> {
        return runCatching { tripApiService.joinTrip(request) }
    }

    suspend fun getTripByInviteCode(code: String): Result<com.mobile.travelhub.data.model.TripInfoResponse> {
        return runCatching { tripApiService.getTripByInviteCode(code) }
    }

    suspend fun getInviteCode(tripId: Long): Result<TripInviteCodeResponse> {
        return runCatching { tripApiService.getInviteCode(tripId) }
    }

    suspend fun regenerateInviteCode(tripId: Long): Result<TripInviteCodeResponse> {
        return runCatching { tripApiService.regenerateInviteCode(tripId) }
    }

    suspend fun getJoinRequests(tripId: Long): Result<List<TripJoinRequestResponse>> {
        return runCatching { tripMemberApiService.getJoinRequests(tripId) }
    }

    suspend fun approveJoinRequest(tripId: Long, userId: Long): Result<Unit> {
        return runCatching { tripMemberApiService.approveRequest(tripId, userId) }
    }

    suspend fun rejectJoinRequest(tripId: Long, userId: Long): Result<Unit> {
        return runCatching { tripMemberApiService.rejectRequest(tripId, userId) }
    }

    suspend fun removeTripMember(tripId: Long, userId: Long): Result<Unit> {
        return runCatching { tripMemberApiService.removeMember(tripId, userId) }
    }

    suspend fun leaveTrip(tripId: Long): Result<Unit> {
        return runCatching { tripMemberApiService.leaveTrip(tripId) }
    }

    suspend fun updateTripMemberRole(
        tripId: Long,
        userId: Long,
        request: UpdateTripMemberRoleRequest
    ): Result<TripMemberResponse> {
        return runCatching { tripMemberApiService.updateMemberRole(tripId, userId, request) }
    }

    suspend fun listTripExpenses(tripId: Long): Result<TripExpenseResponse> {
        return runCatching { tripExpenseApiService.listExpenses(tripId) }
    }

    suspend fun addTripExpense(
        tripId: Long,
        request: CreateTripExpenseRequest
    ): Result<TripExpenseTransactionResponse> {
        return runCatching { tripExpenseApiService.addExpense(tripId, request) }
    }

    suspend fun updateTripExpense(
        tripId: Long,
        expenseId: Long,
        request: UpdateTripExpenseRequest
    ): Result<TripExpenseTransactionResponse> {
        return runCatching { tripExpenseApiService.updateExpense(tripId, expenseId, request) }
    }

    suspend fun deleteTripExpense(tripId: Long, expenseId: Long): Result<Unit> {
        return runCatching { tripExpenseApiService.deleteExpense(tripId, expenseId) }
    }

    suspend fun listTripDays(tripId: Long): Result<List<TripDayResponse>> {
        return runCatching { itineraryApiService.listTripDays(tripId) }
    }
}
