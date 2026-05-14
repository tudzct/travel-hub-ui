package com.mobile.travelhub.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mobile.travelhub.data.TripRepository
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

data class TripsUiState(
    val isLoading: Boolean = true,
    val isJoining: Boolean = false,
    val activeTrip: UpcomingTripUiModel? = null,
    val upcomingTrips: List<UpcomingTripUiModel> = emptyList(),
    val pastTrips: List<PastTripUiModel> = emptyList(),
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

    private val _uiState = MutableStateFlow(TripsUiState())
    val uiState: StateFlow<TripsUiState> = _uiState.asStateFlow()

    init {
        refreshDashboard()
    }

    fun refreshDashboard() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            tripRepository.getDashboard()
                .onSuccess { dashboard ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            activeTrip = dashboard.activeTrip?.toUiModel(),
                            upcomingTrips = dashboard.upcomingTrips.map { it.toUiModel() },
                            pastTrips = dashboard.pastTrips.map { it.toUiModel() },
                            errorMessage = null
                        )
                    }
                }
                .onFailure { throwable ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = throwable.message ?: "Không tải được danh sách chuyến đi"
                        )
                    }
                }
        }
    }

    fun joinTrip(inviteCode: String, onResult: (Boolean, String) -> Unit = { _, _ -> }) {
        val normalizedCode = inviteCode.trim().uppercase()
        if (normalizedCode.isBlank()) {
            onResult(false, "Vui lòng nhập mã chuyến đi")
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isJoining = true, errorMessage = null) }
            tripRepository.joinTrip(com.mobile.travelhub.data.model.JoinTripRequest(inviteCode = normalizedCode))
                .onSuccess { response ->
                    _uiState.update { it.copy(isJoining = false) }
                    refreshDashboard()
                    onResult(true, response.message.ifBlank { "Đã gửi yêu cầu tham gia" })
                }
                .onFailure { throwable ->
                    val message = throwable.message ?: "Không tham gia được chuyến đi"
                    _uiState.update {
                        it.copy(
                            isJoining = false,
                            errorMessage = message
                        )
                    }
                    onResult(false, message)
                }
        }
    }

    private fun UpcomingTripResponse.toUiModel(): UpcomingTripUiModel {
        return UpcomingTripUiModel(
            tripId = tripId,
            name = name,
            location = location,
            coverImageUrl = coverImageUrl,
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
}