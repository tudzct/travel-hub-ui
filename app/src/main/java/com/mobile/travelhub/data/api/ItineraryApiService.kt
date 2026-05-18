package com.mobile.travelhub.data.api

import com.google.gson.annotations.SerializedName
import com.mobile.travelhub.data.model.ItineraryResponse
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface ItineraryApiService {
    @GET("/api/itineraries/by-group/{groupName}")
    suspend fun getByGroupName(@Path("groupName") groupName: String): ItineraryResponse

    @POST("/api/itineraries")
    suspend fun createItinerary(@Body request: CreateItineraryRequestDto): ItineraryResponse

    @POST("/api/itineraries/{itineraryId}/days")
    suspend fun createDay(
        @Path("itineraryId") itineraryId: Long,
        @Body request: CreateItineraryDayRequestDto
    ): ItineraryResponse

    @PUT("/api/itineraries/{itineraryId}/days/{dayId}")
    suspend fun updateDay(
        @Path("itineraryId") itineraryId: Long,
        @Path("dayId") dayId: Long,
        @Body request: UpdateItineraryDayRequestDto
    ): ItineraryResponse

    @DELETE("/api/itineraries/{itineraryId}/days/{dayId}")
    suspend fun deleteDay(
        @Path("itineraryId") itineraryId: Long,
        @Path("dayId") dayId: Long
    ): ItineraryResponse

    @POST("/api/itineraries/{itineraryId}/stops")
    suspend fun createStop(
        @Path("itineraryId") itineraryId: Long,
        @Body request: UpsertItineraryStopRequestDto
    ): ItineraryResponse

    @PUT("/api/itineraries/{itineraryId}/stops/{stopId}")
    suspend fun updateStop(
        @Path("itineraryId") itineraryId: Long,
        @Path("stopId") stopId: Long,
        @Body request: UpsertItineraryStopRequestDto
    ): ItineraryResponse

    @DELETE("/api/itineraries/{itineraryId}/stops/{stopId}")
    suspend fun deleteStop(
        @Path("itineraryId") itineraryId: Long,
        @Path("stopId") stopId: Long
    ): ItineraryResponse

    @POST("/api/itineraries/{itineraryId}/ai/proposals")
    suspend fun createAiProposal(
        @Path("itineraryId") itineraryId: Long,
        @Body request: CreateItineraryAiProposalRequestDto
    ): ItineraryAiProposalResponseDto

    @POST("/api/itineraries/{itineraryId}/ai/proposals/{proposalId}/apply")
    suspend fun applyAiProposal(
        @Path("itineraryId") itineraryId: Long,
        @Path("proposalId") proposalId: String,
        @Body request: ApplyItineraryAiProposalRequestDto
    ): ItineraryResponse
}

data class CreateItineraryRequestDto(
    val groupName: String,
    @SerializedName("trip_id")
    val tripId: Long? = null
)

data class CreateItineraryDayRequestDto(
    val label: String,
    val dateLabel: String
)

data class UpdateItineraryDayRequestDto(
    val label: String,
    val dateLabel: String
)

data class UpsertItineraryStopRequestDto(
    @SerializedName("day_id")
    val dayId: Long,
    @SerializedName("sort_order")
    val sortOrder: Int? = null,
    @SerializedName("start_time")
    val startTime: String,
    @SerializedName("end_time")
    val endTime: String,
    val title: String,
    @SerializedName("place_name")
    val placeName: String,
    val note: String,
    @SerializedName("transport_to_next")
    val transportToNext: String,
    @SerializedName("estimated_cost")
    val estimatedCost: String,
    @SerializedName("color_hex")
    val colorHex: Long,
    @SerializedName("icon_name")
    val iconName: String
)

data class CreateItineraryAiProposalRequestDto(
    val prompt: String,
    @SerializedName("input_type")
    val inputType: String,
    @SerializedName("selected_day_id")
    val selectedDayId: Long?,
    @SerializedName("selected_day_index")
    val selectedDayIndex: Int?,
    @SerializedName("desired_days")
    val desiredDays: Int?,
    val destination: String?,
    val task: String?,
    @SerializedName("base_version")
    val baseVersion: Int
)

data class ApplyItineraryAiProposalRequestDto(
    @SerializedName("selected_change_ids")
    val selectedChangeIds: List<String>,
    @SerializedName("base_version")
    val baseVersion: Int
)

data class ItineraryAiProposalResponseDto(
    @SerializedName("proposal_id")
    val proposalId: String,
    @SerializedName("base_version")
    val baseVersion: Int,
    val summary: String,
    val changes: List<ItineraryAiChangeResponseDto>
)

data class ItineraryAiChangeResponseDto(
    @SerializedName("change_id")
    val changeId: String,
    val type: String,
    val reason: String,
    @SerializedName("insert_at")
    val insertAt: Int?,
    @SerializedName("from_day_id")
    val fromDayId: Long?,
    @SerializedName("from_day_index")
    val fromDayIndex: Int?,
    @SerializedName("from_index")
    val fromIndex: Int?,
    @SerializedName("to_day_id")
    val toDayId: Long?,
    @SerializedName("to_day_index")
    val toDayIndex: Int?,
    @SerializedName("to_index")
    val toIndex: Int?,
    @SerializedName("target_day_id")
    val targetDayId: Long?,
    @SerializedName("target_stop_id")
    val targetStopId: Long?,
    @SerializedName("day_before")
    val dayBefore: ItineraryAiDayDraftDto?,
    @SerializedName("day_after")
    val dayAfter: ItineraryAiDayDraftDto?,
    @SerializedName("stop_before")
    val stopBefore: ItineraryAiStopDraftDto?,
    @SerializedName("stop_after")
    val stopAfter: ItineraryAiStopDraftDto?
)

data class ItineraryAiDayDraftDto(
    val id: Long?,
    @SerializedName("day_index")
    val dayIndex: Int,
    val label: String,
    @SerializedName("date_label")
    val dateLabel: String,
    val stops: List<ItineraryAiStopDraftDto>?
)

data class ItineraryAiStopDraftDto(
    val id: Long?,
    @SerializedName("day_id")
    val dayId: Long?,
    @SerializedName("day_index")
    val dayIndex: Int?,
    @SerializedName("sort_order")
    val sortOrder: Int?,
    @SerializedName("start_time")
    val startTime: String?,
    @SerializedName("end_time")
    val endTime: String?,
    val title: String,
    @SerializedName("place_name")
    val placeName: String,
    val note: String?,
    @SerializedName("transport_to_next")
    val transportToNext: String?,
    @SerializedName("estimated_cost")
    val estimatedCost: String?,
    @SerializedName("color_hex")
    val colorHex: Long?,
    @SerializedName("icon_name")
    val iconName: String?
)
