package com.mobile.travelhub.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mobile.travelhub.data.TripRepository
import com.mobile.travelhub.data.model.TripDashboardResponse
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class GroupDayUiModel(
    val dayIndex: Int,
    val label: String,
    val dateLabel: String,
    val stopCount: Int,
    val firstStopTitles: List<String>
)

data class GroupDetailUiState(
    val isLoading: Boolean = true,
    val tripId: Long = -1L,
    val groupName: String = "",
    val location: String = "",
    val startDate: String = "",
    val endDate: String = "",
    val coverImageUrl: String? = null,
    val statusLabel: String = "",
    val days: List<GroupDayUiModel> = emptyList(),
    val totalStops: Int = 0,
    val memberInfoLabel: String = "Dữ liệu thành viên chưa có từ BE",
    val activityLabel: String = "BE chưa có feed hoạt động nhóm",
    val errorMessage: String? = null
)

@HiltViewModel
class GroupDetailViewModel @Inject constructor(
    private val tripRepository: TripRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(GroupDetailUiState())
    val uiState: StateFlow<GroupDetailUiState> = _uiState.asStateFlow()

    fun loadGroup(tripId: Long, groupName: String) {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isLoading = true,
                    tripId = tripId,
                    groupName = groupName,
                    errorMessage = null
                )
            }

            tripRepository.getDashboard()
                .onSuccess { dashboard ->
                    val snapshot = dashboard.findTripById(tripId)
                    _uiState.update { state ->
                        state.copy(
                            location = snapshot?.location.orEmpty(),
                            startDate = snapshot?.startDate.orEmpty(),
                            endDate = snapshot?.endDate.orEmpty(),
                            coverImageUrl = snapshot?.coverImageUrl,
                            statusLabel = snapshot?.statusLabel.orEmpty()
                        )
                    }
                }
                .onFailure { throwable ->
                    _uiState.update {
                        it.copy(errorMessage = throwable.message ?: "Không tải được thông tin chuyến đi")
                    }
                }

            tripRepository.getItineraryByGroupName(groupName)
                .onSuccess { itinerary ->
                    _uiState.update { state ->
                        state.copy(
                            isLoading = false,
                            days = itinerary.days.map { day ->
                                GroupDayUiModel(
                                    dayIndex = day.dayIndex,
                                    label = day.label,
                                    dateLabel = day.dateLabel,
                                    stopCount = day.stops.size,
                                    firstStopTitles = day.stops.take(3).map { stop -> stop.title }
                                )
                            },
                            totalStops = itinerary.days.sumOf { it.stops.size },
                            memberInfoLabel = "Thành viên chi tiết chưa có endpoint",
                            activityLabel = "Itinerary đã nối từ BE"
                        )
                    }
                }
                .onFailure { throwable ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = throwable.message ?: "Không tải được lịch trình"
                        )
                    }
                }

            _uiState.update { it.copy(isLoading = false) }
        }
    }

    private fun TripDashboardResponse.findTripById(tripId: Long): DashboardTripSnapshot? {
        activeTrip?.takeIf { it.tripId == tripId }?.let { return it.toSnapshot() }
        upcomingTrips.firstOrNull { it.tripId == tripId }?.let { return it.toSnapshot() }
        pastTrips.firstOrNull { it.tripId == tripId }?.let { return it.toSnapshot() }
        return null
    }

    private fun com.mobile.travelhub.data.model.ActiveTripResponse.toSnapshot(): DashboardTripSnapshot {
        return DashboardTripSnapshot(
            tripId = tripId,
            name = name,
            location = location,
            coverImageUrl = coverImageUrl,
            startDate = startDate,
            endDate = endDate,
            statusLabel = "Đang diễn ra"
        )
    }

    private fun com.mobile.travelhub.data.model.UpcomingTripResponse.toSnapshot(): DashboardTripSnapshot {
        return DashboardTripSnapshot(
            tripId = tripId,
            name = name,
            location = location,
            coverImageUrl = coverImageUrl,
            startDate = null,
            endDate = null,
            statusLabel = "Sắp khởi hành · Còn $daysLeft ngày"
        )
    }

    private fun com.mobile.travelhub.data.model.PastTripResponse.toSnapshot(): DashboardTripSnapshot {
        return DashboardTripSnapshot(
            tripId = tripId,
            name = locationName,
            location = locationName,
            coverImageUrl = imageUrl,
            startDate = dateString,
            endDate = dateString,
            statusLabel = "Đã hoàn thành"
        )
    }

    private data class DashboardTripSnapshot(
        val tripId: Long,
        val name: String,
        val location: String,
        val coverImageUrl: String?,
        val startDate: String?,
        val endDate: String?,
        val statusLabel: String
    )
}