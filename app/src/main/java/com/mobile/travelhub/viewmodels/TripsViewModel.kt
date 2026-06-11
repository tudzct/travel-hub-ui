package com.mobile.travelhub.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mobile.travelhub.data.TripRepository
import com.mobile.travelhub.data.httpStatusCode
import com.mobile.travelhub.data.userMessage
import com.mobile.travelhub.data.model.PastTripResponse
import com.mobile.travelhub.data.model.TripDashboardResponse
import com.mobile.travelhub.data.model.UpcomingTripResponse
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

data class TripsUiState(
    val isLoading: Boolean = true,
    val isJoining: Boolean = false,
    val activeTrip: UpcomingTripUiModel? = null,
    val upcomingTrips: List<UpcomingTripUiModel> = emptyList(),
    val pastTrips: List<PastTripUiModel> = emptyList(),
    val isPastTripsLoading: Boolean = false,
    val isPastTripsLoadingMore: Boolean = false,
    val pastTripsPage: Int = 0,
    val pastTripsHasMore: Boolean = true,
    val errorMessage: String? = null
)

data class UpcomingTripUiModel(
    val tripId: Long,
    val name: String,
    val location: String,
    val coverImageUrl: String? = null,
    val startDate: String? = null,
    val endDate: String? = null,
    val daysLeft: Int = 0,
    val memberCount: Int = 0
)

data class PastTripUiModel(
    val tripId: Long,
    val locationName: String,
    val dateString: String,
    val imageUrl: String? = null
)

@HiltViewModel
class TripsViewModel @Inject constructor(
    private val tripRepository: TripRepository
) : ViewModel() {
    private companion object {
        const val PAST_TRIPS_PAGE_SIZE = 5
    }

    private val _uiState = MutableStateFlow(TripsUiState())
    val uiState: StateFlow<TripsUiState> = _uiState.asStateFlow()

    init {
        refreshDashboard()
    }

    fun refreshDashboard(isSilent: Boolean = false) {
        viewModelScope.launch {
            val hasData = _uiState.value.activeTrip != null || _uiState.value.upcomingTrips.isNotEmpty() || _uiState.value.pastTrips.isNotEmpty()
            if (!isSilent) {
                _uiState.update { it.copy(isLoading = !hasData, errorMessage = null) }
            }
            tripRepository.getDashboard()
                .onSuccess { dashboard ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            activeTrip = dashboard.activeTrip?.toUiModel(),
                            upcomingTrips = dashboard.upcomingTrips.map { it.toUiModel() },
                            errorMessage = null
                        )
                    }
                    if (!isSilent && _uiState.value.pastTrips.isEmpty() && !_uiState.value.isPastTripsLoading) {
                        refreshPastTrips()
                    }
                }
                .onFailure { throwable ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = if (isSilent && hasData) it.errorMessage else throwable.userMessage("Không tải được danh sách chuyến đi")
                        )
                    }
                }
        }
    }

    fun refreshPastTrips() {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    pastTrips = emptyList(),
                    pastTripsPage = 0,
                    pastTripsHasMore = true,
                    isPastTripsLoading = true,
                    isPastTripsLoadingMore = false
                )
            }
            loadPastTripsPage(page = 0, append = false)
        }
    }

    fun loadMorePastTrips() {
        val state = _uiState.value
        if (state.isPastTripsLoading || state.isPastTripsLoadingMore || !state.pastTripsHasMore) {
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isPastTripsLoadingMore = true) }
            loadPastTripsPage(page = state.pastTripsPage + 1, append = true)
        }
    }

    private suspend fun loadPastTripsPage(page: Int, append: Boolean) {
        tripRepository.getPastTrips(page = page, pageSize = PAST_TRIPS_PAGE_SIZE)
            .onSuccess { response ->
                val newItems = response.data.map { it.toUiModel() }
                _uiState.update { state ->
                    val mergedItems = if (append) {
                        (state.pastTrips + newItems).distinctBy { it.tripId }
                    } else {
                        newItems
                    }
                    state.copy(
                        pastTrips = mergedItems,
                        pastTripsPage = response.pageNumber,
                        pastTripsHasMore = response.pageNumber + 1 < response.totalPages,
                        isPastTripsLoading = false,
                        isPastTripsLoadingMore = false,
                        errorMessage = null
                    )
                }
            }
            .onFailure { throwable ->
                _uiState.update {
                    it.copy(
                        isPastTripsLoading = false,
                        isPastTripsLoadingMore = false,
                        errorMessage = throwable.userMessage("Không tải được nhật ký hành trình")
                    )
                }
            }
    }

    fun joinTrip(inviteCode: String, onResult: (Boolean, String) -> Unit = { _, _ -> }) {
        val normalizedCode = inviteCode.trim().uppercase()
        if (normalizedCode.isBlank()) {
            onResult(false, "Vui lòng nhập mã chuyến đi")
            return
        }

        if (normalizedCode.length != 8) {
            onResult(false, "Mã chuyến đi không hợp lệ")
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isJoining = true, errorMessage = null) }
            // First, lookup trip by invite code without creating join request
            tripRepository.getTripByInviteCode(normalizedCode)
                .onSuccess { tripInfo ->
                    if (_uiState.value.hasJoinedTrip(tripInfo.id)) {
                        _uiState.update { it.copy(isJoining = false) }
                        onResult(false, "Bạn đã tham gia chuyến đi này")
                        return@onSuccess
                    }

                    var alreadyJoinedLatestDashboard = false
                    tripRepository.getDashboard()
                        .onSuccess { dashboard ->
                            _uiState.update {
                                it.copy(
                                    activeTrip = dashboard.activeTrip?.toUiModel(),
                                    upcomingTrips = dashboard.upcomingTrips.map { it.toUiModel() }
                                )
                            }
                            alreadyJoinedLatestDashboard = dashboard.hasJoinedTrip(tripInfo.id)
                        }

                    if (alreadyJoinedLatestDashboard) {
                        _uiState.update { it.copy(isJoining = false) }
                        onResult(false, "Bạn đã tham gia chuyến đi này")
                        return@onSuccess
                    }

                    if (isPastDate(tripInfo.endDate)) {
                        _uiState.update { it.copy(isJoining = false) }
                        onResult(false, "Chuyến đi đã kết thúc")
                        return@onSuccess
                    }

                    // Trip is valid and not ended -> call joinTrip to send request
                    tripRepository.joinTrip(com.mobile.travelhub.data.model.JoinTripRequest(inviteCode = normalizedCode))
                        .onSuccess {
                            _uiState.update { it.copy(isJoining = false) }
                            refreshDashboard()
                            onResult(true, "Đã gửi yêu cầu tham gia nhóm")
                        }
                        .onFailure { throwable ->
                            val message = joinTripErrorMessage(throwable)
                            _uiState.update {
                                it.copy(isJoining = false, errorMessage = null)
                            }
                            onResult(false, message)
                        }
                }
                .onFailure { throwable ->
                    // If lookup fails, surface a friendly error (don't proceed to create join request)
                    _uiState.update { it.copy(isJoining = false) }
                    val message = joinTripErrorMessage(throwable)
                    onResult(false, message)
                }
        }
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

    private fun UpcomingTripResponse.toUiModel(): UpcomingTripUiModel {
        return UpcomingTripUiModel(
            tripId = tripId,
            name = name,
            location = location,
            coverImageUrl = coverImageUrl,
            startDate = startDate,
            endDate = endDate,
            daysLeft = daysLeft,
            memberCount = memberCount
        )
    }

    private fun com.mobile.travelhub.data.model.ActiveTripResponse.toUiModel(): UpcomingTripUiModel {
        return UpcomingTripUiModel(
            tripId = tripId,
            name = name,
            location = location,
            coverImageUrl = coverImageUrl,
            startDate = startDate,
            endDate = endDate
        )
    }

    private fun PastTripResponse.toUiModel(): PastTripUiModel {
        return PastTripUiModel(
            tripId = tripId,
            locationName = locationName,
            dateString = dateString,
            imageUrl = imageUrl
        )
    }

    private fun joinTripErrorMessage(throwable: Throwable): String {
        val raw = throwable.userMessage("Không tham gia được chuyến đi")
        if (raw.contains("kết thúc", ignoreCase = true) || raw.contains("đã hoàn thành", ignoreCase = true) || raw.contains("ended", ignoreCase = true)) {
            return "Chuyến đi đã kết thúc"
        }
        if (raw.contains("đã tham gia", ignoreCase = true) || raw.contains("already a member", ignoreCase = true)) {
            return "Bạn đã tham gia chuyến đi này"
        }
        if (raw.contains("ngân hàng", ignoreCase = true) || raw.contains("số tài khoản", ignoreCase = true) || raw.contains("bank account", ignoreCase = true)) {
            return raw
        }

        return when (throwable.httpStatusCode()) {
            400 -> "Mã chuyến đi không hợp lệ"
            404 -> "Không tìm thấy chuyến đi"
            409 -> "Bạn đã gửi yêu cầu hoặc đã là thành viên của nhóm này"
            else -> throwable.userMessage("Không tham gia được chuyến đi")
        }
    }

    private fun TripsUiState.hasJoinedTrip(tripId: Long): Boolean {
        return activeTrip?.tripId == tripId ||
                upcomingTrips.any { it.tripId == tripId } ||
                pastTrips.any { it.tripId == tripId }
    }

    private fun TripDashboardResponse.hasJoinedTrip(tripId: Long): Boolean {
        return activeTrip?.tripId == tripId ||
                upcomingTrips.any { it.tripId == tripId } ||
                pastTrips.any { it.tripId == tripId }
    }
}
