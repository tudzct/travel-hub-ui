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
import com.mobile.travelhub.data.model.SettlementResponse
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
    private val detailCache = java.util.concurrent.ConcurrentHashMap<Long, TripDetailResponse>()
    private val daysCache = java.util.concurrent.ConcurrentHashMap<Long, List<TripDayResponse>>()
    private val expenseCache = java.util.concurrent.ConcurrentHashMap<Long, TripExpenseResponse>()
    private val settlementCache = java.util.concurrent.ConcurrentHashMap<Long, List<SettlementResponse>>()
    private val freshlyCreatedTripIds = java.util.concurrent.ConcurrentHashMap.newKeySet<Long>()

    fun getCachedTripDetail(tripId: Long): TripDetailResponse? = detailCache[tripId]
    fun getCachedTripDays(tripId: Long): List<TripDayResponse>? = daysCache[tripId]
    fun getCachedTripExpenses(tripId: Long): TripExpenseResponse? = expenseCache[tripId]
    fun getCachedTripSettlements(tripId: Long): List<SettlementResponse>? = settlementCache[tripId]
    fun clearCache(tripId: Long) {
        detailCache.remove(tripId)
        daysCache.remove(tripId)
        expenseCache.remove(tripId)
        settlementCache.remove(tripId)
        freshlyCreatedTripIds.remove(tripId)
    }

    suspend fun getDashboard(): Result<TripDashboardResponse> {
        return runCatching { tripApiService.getDashboard() }
    }

    suspend fun createTrip(request: CreateTripRequest): Result<TripDetailResponse> {
        return runCatching {
            val response = tripApiService.createTrip(request)
            val tripId = response.tripInfo.id
            detailCache[tripId] = response
            daysCache[tripId] = emptyList()
            freshlyCreatedTripIds.add(tripId)
            response
        }
    }

    fun consumeFreshlyCreatedTripDetail(tripId: Long): TripDetailResponse? {
        if (!freshlyCreatedTripIds.remove(tripId)) {
            return null
        }
        return detailCache[tripId]
    }

    suspend fun getTripDetail(tripId: Long): Result<TripDetailResponse> {
        return runCatching {
            val response = tripApiService.getTripDetail(tripId)
            detailCache[tripId] = response
            response
        }
    }

    suspend fun updateTrip(tripId: Long, request: UpdateTripRequest): Result<Unit> {
        return runCatching { tripApiService.updateTrip(tripId, request) }
    }

    suspend fun deleteTrip(tripId: Long): Result<Unit> {
        return runCatching {
            tripApiService.deleteTrip(tripId)
            clearCache(tripId)
        }
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

    suspend fun finishTrip(tripId: Long): Result<List<SettlementResponse>> {
        return runCatching {
            val response = tripApiService.finishTrip(tripId)
            settlementCache[tripId] = response
            detailCache.remove(tripId)
            response
        }
    }

    suspend fun listTripSettlements(tripId: Long): Result<List<SettlementResponse>> {
        return runCatching {
            val response = tripApiService.listSettlements(tripId)
            settlementCache[tripId] = response
            response
        }
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
        return runCatching {
            tripMemberApiService.leaveTrip(tripId)
            clearCache(tripId)
        }
    }

    suspend fun updateTripMemberRole(
        tripId: Long,
        userId: Long,
        request: UpdateTripMemberRoleRequest
    ): Result<TripMemberResponse> {
        return runCatching { tripMemberApiService.updateMemberRole(tripId, userId, request) }
    }

    suspend fun listTripExpenses(tripId: Long): Result<TripExpenseResponse> {
        return runCatching {
            val response = tripExpenseApiService.listExpenses(tripId)
            expenseCache[tripId] = response
            response
        }
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
        return runCatching {
            val response = itineraryApiService.listTripDays(tripId)
            daysCache[tripId] = response
            response
        }
    }
}
