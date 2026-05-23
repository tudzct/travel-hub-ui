package com.mobile.travelhub.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mobile.travelhub.data.TripRepository
import com.mobile.travelhub.data.model.*
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

data class GroupJoinRequestUiModel(
    val userId: Long,
    val name: String,
    val avatarUrl: String? = null,
    val requestedAt: String? = null
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
    val memberInfoLabel: String = "Đang tải thành viên...",
    val myRole: String = "",
    val inviteCode: String? = null,
    val inviteLink: String? = null,
    val inviteExpiredAt: String? = null,
    val isInviteCodeLoading: Boolean = false,
    val joinRequests: List<GroupJoinRequestUiModel> = emptyList(),
    val isJoinRequestsLoading: Boolean = false,
    val members: List<GroupMemberUiModel> = emptyList(),
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

            // Lấy thông tin sơ bộ từ dashboard (nếu có) để hiển thị nhanh
            tripRepository.getDashboard()
                .onSuccess { dashboard ->
                    val snapshot = dashboard.findTripById(tripId)
                    if (snapshot != null) {
                        _uiState.update { state ->
                            state.copy(
                                location = snapshot.location,
                                startDate = snapshot.startDate.orEmpty(),
                                endDate = snapshot.endDate.orEmpty(),
                                coverImageUrl = snapshot.coverImageUrl,
                                statusLabel = snapshot.statusLabel
                            )
                        }
                    }
                }

            // Lấy thông tin chi tiết trip
            tripRepository.getTripDetail(tripId)
                .onSuccess { detail ->
                    _uiState.update { state ->
                        state.mergeTripDetail(detail)
                    }
                    val hasInviteCode = _uiState.value.inviteCode?.isNotBlank() == true
                    if (!hasInviteCode) {
                        loadInviteCode(tripId)
                    } else {
                        _uiState.update { state ->
                            state.copy(isInviteCodeLoading = false)
                        }
                    }
                    if (detail.myRole.equals("LEADER", ignoreCase = true)) {
                        loadJoinRequests(tripId)
                    } else {
                        _uiState.update { state ->
                            state.copy(
                                joinRequests = emptyList(),
                                isJoinRequestsLoading = false
                            )
                        }
                    }
                }
                .onFailure { throwable ->
                    _uiState.update {
                        it.copy(errorMessage = throwable.message ?: "Không tải được chi tiết chuyến đi")
                    }
                }

            tripRepository.listTripDays(tripId)
                .onSuccess { tripDays ->
                    _uiState.update { state ->
                        state.copy(
                            isLoading = false,
                            days = tripDays.map { day ->
                                GroupDayUiModel(
                                    dayIndex = day.dayNumber,
                                    label = "Day ${day.dayNumber}",
                                    dateLabel = day.date,
                                    stopCount = day.activities.size,
                                    firstStopTitles = day.activities.take(3).map { activity -> activity.title }
                                )
                            },
                            totalStops = tripDays.sumOf { it.activities.size }
                        )
                    }
                }

            _uiState.update { it.copy(isLoading = false) }
        }
    }

    private fun loadInviteCode(tripId: Long) {
        viewModelScope.launch {
            _uiState.update { it.copy(isInviteCodeLoading = true) }
            tripRepository.getInviteCode(tripId)
                .onSuccess { response ->
                    val inviteCode = response.inviteCode.takeIf { it.isNotBlank() }
                    val inviteLink = response.inviteLink.takeIf { it.isNotBlank() }
                    _uiState.update {
                        it.copy(
                            isInviteCodeLoading = false,
                            inviteCode = inviteCode ?: it.inviteCode,
                            inviteLink = inviteLink ?: it.inviteLink,
                            inviteExpiredAt = response.expiredAt
                        )
                    }
                }
                .onFailure {
                    _uiState.update { state ->
                        state.copy(isInviteCodeLoading = false)
                    }
                }
        }
    }

    private fun loadJoinRequests(tripId: Long) {
        viewModelScope.launch {
            _uiState.update { it.copy(isJoinRequestsLoading = true) }
            tripRepository.getJoinRequests(tripId)
                .onSuccess { requests ->
                    _uiState.update {
                        it.copy(
                            joinRequests = requests.map { request ->
                                GroupJoinRequestUiModel(
                                    userId = request.userId,
                                    name = request.name,
                                    avatarUrl = request.avatarUrl,
                                    requestedAt = request.requestedAt
                                )
                            },
                            isJoinRequestsLoading = false
                        )
                    }
                }
                .onFailure {
                    _uiState.update {
                        it.copy(
                            joinRequests = emptyList(),
                            isJoinRequestsLoading = false
                        )
                    }
                }
        }
    }

    fun approveJoinRequest(userId: Long) {
        val tripId = uiState.value.tripId
        if (tripId == -1L) return

        viewModelScope.launch {
            tripRepository.approveJoinRequest(tripId, userId)
                .onSuccess {
                    loadJoinRequests(tripId)
                    // Reload members list
                    tripRepository.getTripDetail(tripId).onSuccess { detail ->
                        _uiState.update { it.mergeTripDetail(detail) }
                    }
                }
                .onFailure { throwable ->
                    _uiState.update { state ->
                        state.copy(
                            isJoinRequestsLoading = false,
                            errorMessage = throwable.message ?: "Không thể chấp nhận yêu cầu vào nhóm"
                        )
                    }
                }
        }
    }

    fun rejectJoinRequest(userId: Long) {
        val tripId = uiState.value.tripId
        if (tripId == -1L) return

        viewModelScope.launch {
            tripRepository.rejectJoinRequest(tripId, userId)
                .onSuccess {
                    loadJoinRequests(tripId)
                }
                .onFailure { throwable ->
                    _uiState.update { state ->
                        state.copy(
                            isJoinRequestsLoading = false,
                            errorMessage = throwable.message ?: "Không thể từ chối yêu cầu vào nhóm"
                        )
                    }
                }
        }
    }

    fun leaveGroup(onDone: (Boolean, String) -> Unit) {
        val tripId = uiState.value.tripId
        if (tripId == -1L) return
        
        viewModelScope.launch {
            tripRepository.leaveTrip(tripId)
                .onSuccess {
                    onDone(true, "Đã rời khỏi nhóm")
                }
                .onFailure { throwable ->
                    onDone(false, throwable.message ?: "Không thể rời nhóm")
                }
        }
    }

    fun deleteGroup(tripId: Long, onDone: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            tripRepository.deleteTrip(tripId)
                .onSuccess {
                    onDone(true, "Đã xóa nhóm")
                }
                .onFailure { throwable ->
                    onDone(false, throwable.message ?: "Không xóa được nhóm")
                }
        }
    }

    private fun TripDashboardResponse.findTripById(tripId: Long): DashboardTripSnapshot? {
        activeTrip?.takeIf { it.tripId == tripId }?.let { return it.toSnapshot() }
        upcomingTrips.firstOrNull { it.tripId == tripId }?.let { return it.toSnapshot() }
        pastTrips.firstOrNull { it.tripId == tripId }?.let { return it.toSnapshot() }
        return null
    }

    private fun ActiveTripResponse.toSnapshot(): DashboardTripSnapshot {
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

    private fun UpcomingTripResponse.toSnapshot(): DashboardTripSnapshot {
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

    private fun PastTripResponse.toSnapshot(): DashboardTripSnapshot {
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

    private fun GroupDetailUiState.mergeTripDetail(detail: TripDetailResponse): GroupDetailUiState {
        return copy(
            groupName = detail.tripInfo.name,
            location = detail.tripInfo.location,
            startDate = detail.tripInfo.startDate.orEmpty(),
            endDate = detail.tripInfo.endDate.orEmpty(),
            coverImageUrl = detail.tripInfo.coverImageUrl,
            statusLabel = detail.tripInfo.status ?: statusLabel,
            myRole = detail.myRole,
            inviteCode = detail.tripInfo.inviteCode?.takeIf { it.isNotBlank() } ?: inviteCode,
            members = detail.members.map { member ->
                GroupMemberUiModel(
                    userId = member.userId,
                    name = member.name,
                    avatarUrl = member.avatarUrl,
                    role = member.role
                )
            },
            memberInfoLabel = "${detail.members.size} thành viên"
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
