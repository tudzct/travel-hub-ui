package com.mobile.travelhub.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mobile.travelhub.data.TripRepository
import com.mobile.travelhub.data.model.TripDashboardResponse
import com.mobile.travelhub.data.model.TripDetailResponse
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

data class GroupMemberUiModel(
    val userId: Long,
    val name: String,
    val avatarUrl: String? = null,
    val role: String
)

data class GroupActivityUiModel(
    val id: Long,
    val title: String,
    val description: String? = null,
    val timestamp: String? = null,
    val actorName: String? = null
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
    val myRole: String = "",
    val members: List<GroupMemberUiModel> = emptyList(),
    val recentActivities: List<GroupActivityUiModel> = emptyList(),
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

            tripRepository.getTripDetail(tripId)
                .onSuccess { detail ->
                    _uiState.update { state ->
                        state.mergeTripDetail(detail)
                    }
                }
                .onFailure { throwable ->
                    _uiState.update {
                        it.copy(errorMessage = throwable.message ?: "Không tải được chi tiết chuyến đi")
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
                            totalStops = itinerary.days.sumOf { it.stops.size }
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


    private fun GroupDetailUiState.mergeTripDetail(detail: TripDetailResponse): GroupDetailUiState {
        return copy(
            location = detail.tripInfo.location,
            startDate = detail.tripInfo.startDate.orEmpty(),
            endDate = detail.tripInfo.endDate.orEmpty(),
            coverImageUrl = detail.tripInfo.coverImageUrl,
            statusLabel = detail.tripInfo.status ?: statusLabel,
            myRole = detail.myRole,
            members = detail.members.map { member ->
                GroupMemberUiModel(
                    userId = member.userId,
                    name = member.name,
                    avatarUrl = member.avatarUrl,
                    role = member.role
                )
            },
            recentActivities = detail.recentActivities.map { activity ->
                GroupActivityUiModel(
                    id = activity.id,
                    title = activity.title,
                    description = activity.description,
                    timestamp = activity.timestamp,
                    actorName = activity.actorName
                )
            },
            memberInfoLabel = "${detail.members.size} thành viên · Vai trò: ${detail.myRole}",
            activityLabel = detail.recentActivities.firstOrNull()?.title ?: "Chưa có hoạt động gần đây"
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