package com.mobile.travelhub.data.api

import com.mobile.travelhub.data.model.CreateTripExpenseRequest
import com.mobile.travelhub.data.model.TripExpenseResponse
import com.mobile.travelhub.data.model.TripExpenseTransactionResponse
import com.mobile.travelhub.data.model.UpdateTripExpenseRequest
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface TripExpenseApiService {
    @GET("api/trips/{tripId}/expenses")
    suspend fun listExpenses(
        @Path("tripId") tripId: Long
    ): TripExpenseResponse

    @POST("api/trips/{tripId}/expenses")
    suspend fun addExpense(
        @Path("tripId") tripId: Long,
        @Body request: CreateTripExpenseRequest
    ): TripExpenseTransactionResponse

    @PUT("api/trips/{tripId}/expenses/{expenseId}")
    suspend fun updateExpense(
        @Path("tripId") tripId: Long,
        @Path("expenseId") expenseId: Long,
        @Body request: UpdateTripExpenseRequest
    ): TripExpenseTransactionResponse

    @DELETE("api/trips/{tripId}/expenses/{expenseId}")
    suspend fun deleteExpense(
        @Path("tripId") tripId: Long,
        @Path("expenseId") expenseId: Long
    ): Unit
}