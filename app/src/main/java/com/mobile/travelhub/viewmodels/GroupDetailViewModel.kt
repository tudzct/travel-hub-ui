package com.mobile.travelhub.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mobile.travelhub.data.PlaceRepository
import com.mobile.travelhub.data.TripRepository
import com.mobile.travelhub.data.httpStatusCode
import com.mobile.travelhub.data.model.ActiveTripResponse
import com.mobile.travelhub.data.model.PastTripResponse
import com.mobile.travelhub.data.model.TripDashboardResponse
import com.mobile.travelhub.data.model.TripDayResponse
import com.mobile.travelhub.data.model.TripDetailResponse
import com.mobile.travelhub.data.model.TripJoinRequestResponse
import com.mobile.travelhub.data.model.TripInviteCodeResponse
import com.mobile.travelhub.data.model.UpcomingTripResponse
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

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
    val placeId: Long? = null,
    val placeImages: List<String> = emptyList(),
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
    val errorMessage: String? = null,
    val isCompleted: Boolean = false,
    val isKickedOut: Boolean = false
)

@HiltViewModel
class GroupDetailViewModel @Inject constructor(
    private val tripRepository: TripRepository,
    private val placeRepository: PlaceRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(GroupDetailUiState())
    val uiState: StateFlow<GroupDetailUiState> = _uiState.asStateFlow()

    fun loadGroup(tripId: Long, groupName: String, isSilent: Boolean = false) {
        viewModelScope.launch {
            val cachedDetail = tripRepository.getCachedTripDetail(tripId)
            val cachedDays = tripRepository.getCachedTripDays(tripId)
            val currentState = _uiState.value
            val hasVisibleContent = currentState.tripId == tripId && currentState.groupName.isNotBlank()
            val hasCachedContent = cachedDetail != null && cachedDays != null

            if (!isSilent) {
                if (hasVisibleContent) {
                    _uiState.update {
                        it.copy(
                            isLoading = true,
                            errorMessage = null,
                            isKickedOut = false,
                            tripId = tripId
                        )
                    }
                } else if (hasCachedContent) {
                    val cachedState = GroupDetailUiState(
                        isLoading = true,
                        tripId = tripId
                    ).mergeTripDetail(cachedDetail!!).copy(
                        days = cachedDays!!.map { day ->
                            GroupDayUiModel(
                                dayIndex = day.dayNumber,
                                label = "Day ${day.dayNumber}",
                                dateLabel = day.date,
                                stopCount = day.activities.size,
                                firstStopTitles = day.activities.take(3).map { activity -> activity.title }
                            )
                        },
                        totalStops = cachedDays.sumOf { it.activities.size },
                        isLoading = true,
                        errorMessage = null,
                        isKickedOut = false
                    )

                    _uiState.update { cachedState }
                } else {
                    _uiState.update {
                        GroupDetailUiState(
                            isLoading = true,
                            tripId = tripId
                        )
                    }
                }
            }

            _uiState.update {
                it.copy(
                    tripId = tripId,
                    errorMessage = if (isSilent) it.errorMessage else null,
                    isKickedOut = if (isSilent) it.isKickedOut else false
                )
            }

            val detailDeferred = async { tripRepository.getTripDetail(tripId) }
            val daysDeferred = if (isSilent) null else async { tripRepository.listTripDays(tripId) }

            val detailResult = detailDeferred.await()
            val daysResult = daysDeferred?.await()

            if (detailResult.isFailure || (daysResult != null && daysResult.isFailure)) {
                val throwable = detailResult.exceptionOrNull() ?: daysResult?.exceptionOrNull()
                val isKicked = throwable?.httpStatusCode() == 403 ||
                        throwable?.httpStatusCode() == 404 ||
                        throwable?.message?.contains("Forbidden", ignoreCase = true) == true ||
                        throwable?.message?.contains("not found", ignoreCase = true) == true ||
                        throwable?.message?.contains("not an active member", ignoreCase = true) == true ||
                        throwable?.message?.contains("not a member", ignoreCase = true) == true

                _uiState.update { state ->
                    if (state.groupName.isNotBlank() && !isKicked) {
                        state.copy(isLoading = false, errorMessage = null)
                    } else {
                        state.copy(
                            isLoading = false,
                            errorMessage = throwable?.message ?: "Không tải được chi tiết chuyến đi",
                            isKickedOut = isKicked
                        )
                    }
                }
                return@launch
            }

            val detail = detailResult.getOrNull() ?: return@launch

            _uiState.update { state ->
                val merged = state.mergeTripDetail(detail)
                if (daysResult != null) {
                    val tripDays = daysResult.getOrNull() ?: emptyList()
                    merged.copy(
                        days = tripDays.map { day ->
                            GroupDayUiModel(
                                dayIndex = day.dayNumber,
                                label = "Day ${day.dayNumber}",
                                dateLabel = day.date,
                                stopCount = day.activities.size,
                                firstStopTitles = day.activities.take(3).map { activity -> activity.title }
                            )
                        },
                        totalStops = tripDays.sumOf { it.activities.size },
                        isLoading = false,
                        errorMessage = null
                    )
                } else {
                    merged.copy(
                        isLoading = false,
                        errorMessage = null
                    )
                }
            }

            val hasInviteCode = _uiState.value.inviteCode?.isNotBlank() == true
            if (!hasInviteCode) {
                loadInviteCode(tripId)
            } else {
                _uiState.update { state -> state.copy(isInviteCodeLoading = false) }
            }

            if (detail.myRole.equals("LEADER", ignoreCase = true)) {
                loadJoinRequests(tripId, isSilent)
            } else {
                _uiState.update { state ->
                    state.copy(
                        joinRequests = emptyList(),
                        isJoinRequestsLoading = false
                    )
                }
            }
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

    private fun loadJoinRequests(tripId: Long, isSilent: Boolean = false) {
        viewModelScope.launch {
            if (!isSilent) {
                _uiState.update { it.copy(isJoinRequestsLoading = true) }
            }
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

    fun loadPlaceImages(placeId: Long?) {
        if (placeId == null) return
        viewModelScope.launch {
            runCatching { placeRepository.getPlaceDetail(placeId) }
                .onSuccess { detail ->
                    val imgs = detail.images.map { it.imageUrl }
                    _uiState.update { it.copy(placeImages = imgs) }
                }
                .onFailure {
                    _uiState.update { it.copy(placeImages = emptyList()) }
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

    fun removeMember(userId: Long, onDone: (Boolean, String) -> Unit) {
        val tripId = uiState.value.tripId
        if (tripId == -1L) return

        viewModelScope.launch {
            tripRepository.removeTripMember(tripId, userId)
                .onSuccess {
                    tripRepository.getTripDetail(tripId).onSuccess { detail ->
                        _uiState.update { it.mergeTripDetail(detail) }
                    }
                    onDone(true, "Đã xóa thành viên")
                }
                .onFailure { throwable ->
                    onDone(false, throwable.message ?: "Không thể xóa thành viên")
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
            statusLabel = "Đang diễn ra",
            placeId = null
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
            statusLabel = "Sắp khởi hành · Còn $daysLeft ngày",
            placeId = null
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
            statusLabel = "Đã hoàn thành",
            placeId = null
        )
    }

    private fun parseLocalDate(dateText: String?): LocalDate? {
        if (dateText.isNullOrBlank()) return null
        val normalized = dateText.substringBefore("T")
        return runCatching { LocalDate.parse(normalized) }
            .recoverCatching {
                LocalDate.parse(
                    normalized,
                    DateTimeFormatter.ofPattern("dd/MM/yyyy", Locale.getDefault())
                )
            }
            .getOrNull()
    }

    private fun getTripStatusLabel(status: String?, startDateText: String?, endDateText: String?): String {
        if (status.equals("COMPLETED", ignoreCase = true) || isPastDate(endDateText)) {
            return "Đã hoàn thành"
        }

        val today = LocalDate.now()
        val startDate = parseLocalDate(startDateText)
        val endDate = parseLocalDate(endDateText)

        if (startDate != null && endDate != null) {
            if (endDate.isBefore(today)) {
                return "Đã hoàn thành"
            }
            if (!startDate.isAfter(today) && !endDate.isBefore(today)) {
                return "Đang diễn ra"
            }
            val daysLeft = java.time.temporal.ChronoUnit.DAYS.between(today, startDate)
            return if (daysLeft > 0) "Sắp khởi hành · Còn $daysLeft ngày" else "Sắp khởi hành"
        }

        return when (status?.uppercase(Locale.getDefault())) {
            "PLANNING" -> "Đang lên kế hoạch"
            "UPCOMING" -> "Sắp diễn ra"
            "ONGOING", "ACTIVE" -> "Đang diễn ra"
            "COMPLETED" -> "Đã hoàn thành"
            else -> status ?: "Chưa xác định"
        }
    }

    private fun GroupDetailUiState.mergeTripDetail(detail: TripDetailResponse): GroupDetailUiState {
        val completed = this.isCompleted ||
                detail.tripInfo.status.equals("COMPLETED", ignoreCase = true) ||
                detail.tripInfo.status?.contains("hoàn thành", ignoreCase = true) == true ||
                isPastDate(detail.tripInfo.endDate)

        return copy(
            groupName = detail.tripInfo.name,
            location = detail.tripInfo.location,
            startDate = detail.tripInfo.startDate.orEmpty(),
            endDate = detail.tripInfo.endDate.orEmpty(),
            coverImageUrl = detail.tripInfo.coverImageUrl,
            placeId = detail.tripInfo.placeId,
            statusLabel = getTripStatusLabel(detail.tripInfo.status, detail.tripInfo.startDate, detail.tripInfo.endDate),
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
            memberInfoLabel = "${detail.members.size} thành viên",
            isCompleted = completed
        )
    }

    private fun isPastDate(dateText: String?): Boolean {
        if (dateText.isNullOrBlank()) return false
        val normalized = dateText.substringBefore("T")
        val date = runCatching { LocalDate.parse(normalized) }
            .recoverCatching {
                LocalDate.parse(
                    normalized,
                    DateTimeFormatter.ofPattern("dd/MM/yyyy", Locale.getDefault())
                )
            }
            .getOrNull() ?: return false
        return date.isBefore(LocalDate.now())
    }

    private data class DashboardTripSnapshot(
        val tripId: Long,
        val name: String,
        val location: String,
        val coverImageUrl: String?,
        val startDate: String?,
        val endDate: String?,
        val statusLabel: String,
        val placeId: Long? = null
    )
}
