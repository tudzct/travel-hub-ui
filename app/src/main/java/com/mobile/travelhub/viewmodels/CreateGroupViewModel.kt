package com.mobile.travelhub.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mobile.travelhub.data.LocationRepository
import com.mobile.travelhub.data.PlaceRepository
import com.mobile.travelhub.data.TripRepository
import com.mobile.travelhub.data.model.CreateTripRequest
import com.mobile.travelhub.data.model.AdminProvinceResponse
import com.mobile.travelhub.data.model.TravelPlaceListItemResponse
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class CreateGroupUiState(
    val name: String = "",
    val destination: String = "",
    val destinationCoverImageUrl: String? = null,
    val provinces: List<AdminProvinceResponse> = emptyList(),
    val places: List<TravelPlaceListItemResponse> = emptyList(),
    val selectedProvinceId: Long? = null,
    val selectedPlaceId: Long? = null,
    val isLoadingLocations: Boolean = false,
    val startDate: String = "",
    val endDate: String = "",
    val budgetMin: String = "",
    val budgetMax: String = "",
    val isSaving: Boolean = false,
    val errorMessage: String? = null
) {
    val selectedProvince: AdminProvinceResponse?
        get() = provinces.firstOrNull { it.id == selectedProvinceId }

    val selectedPlace: TravelPlaceListItemResponse?
        get() = places.firstOrNull { it.id == selectedPlaceId }
}

@HiltViewModel
class CreateGroupViewModel @Inject constructor(
    private val tripRepository: TripRepository,
    private val locationRepository: LocationRepository,
    private val placeRepository: PlaceRepository
) : ViewModel() {

    companion object {
        const val MAX_TRIP_DAYS = 90L
    }

    private val displayDateFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")

    private val _uiState = MutableStateFlow(CreateGroupUiState())
    val uiState: StateFlow<CreateGroupUiState> = _uiState.asStateFlow()

    init {
        loadProvinces()
    }

    fun updateName(value: String) {
        _uiState.update { it.copy(name = value, errorMessage = null) }
    }

    fun updateDestination(value: String) {
        _uiState.update {
            it.copy(
                destination = value,
                destinationCoverImageUrl = null,
                selectedProvinceId = null,
                selectedPlaceId = null,
                places = emptyList(),
                errorMessage = null
            )
        }
    }

    fun selectProvince(provinceId: Long?) {
        if (provinceId == null) {
            _uiState.update {
                it.copy(
                    selectedProvinceId = null,
                    selectedPlaceId = null,
                    destination = "",
                    destinationCoverImageUrl = null,
                    places = emptyList(),
                    errorMessage = null
                )
            }
            return
        }

        _uiState.update {
            it.copy(
                selectedProvinceId = provinceId,
                selectedPlaceId = null,
                destination = "",
                destinationCoverImageUrl = null,
                places = emptyList(),
                errorMessage = null
            )
        }
        loadPlaces(provinceId)
    }

    fun selectPlace(placeId: Long?) {
        if (placeId == null) {
            _uiState.update {
                it.copy(
                    selectedPlaceId = null,
                    destination = "",
                    destinationCoverImageUrl = null,
                    errorMessage = null
                )
            }
            return
        }

        val place = _uiState.value.places.firstOrNull { it.id == placeId } ?: return
        _uiState.update {
            it.copy(
                selectedPlaceId = placeId,
                destination = formatDestination(place),
                destinationCoverImageUrl = place.mainImage,
                errorMessage = null
            )
        }
    }

    fun refreshDestinationOptions() {
        if (_uiState.value.provinces.isEmpty()) {
            loadProvinces()
        }
    }

    fun updateStartDate(value: String) {
        _uiState.update { it.copy(startDate = value, errorMessage = null) }
    }

    fun updateEndDate(value: String) {
        _uiState.update { it.copy(endDate = value, errorMessage = null) }
    }

    fun updateBudgetMin(value: String) {
        _uiState.update { it.copy(budgetMin = value, errorMessage = null) }
    }

    fun updateBudgetMax(value: String) {
        _uiState.update { it.copy(budgetMax = value, errorMessage = null) }
    }

    private fun loadProvinces() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingLocations = true, errorMessage = null) }
            runCatching { locationRepository.getProvinces() }
                .onSuccess { provinces ->
                    _uiState.update {
                        it.copy(
                            provinces = provinces,
                            isLoadingLocations = false
                        )
                    }
                }
                .onFailure { throwable ->
                    _uiState.update {
                        it.copy(
                            isLoadingLocations = false,
                            errorMessage = throwable.message ?: "Unable to load provinces"
                        )
                    }
                }
        }
    }

    private fun loadPlaces(provinceId: Long) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingLocations = true, errorMessage = null) }
            runCatching {
                placeRepository.getPlaces(page = 0, pageSize = 100, provinceId = provinceId).data
            }.onSuccess { places ->
                _uiState.update {
                    it.copy(
                        places = places,
                        isLoadingLocations = false
                    )
                }
            }.onFailure { throwable ->
                _uiState.update {
                    it.copy(
                        isLoadingLocations = false,
                        errorMessage = throwable.message ?: "Unable to load places"
                    )
                }
            }
        }
    }

    private fun formatDestination(place: TravelPlaceListItemResponse): String {
        return listOf(place.name, place.province.name)
            .distinct()
            .joinToString(", ")
    }

    // Check if a date can be selected for start date (must be today or later)
    fun canSelectStartDate(date: LocalDate): Boolean {
        val today = LocalDate.now()
        return !date.isBefore(today) && date.isBefore(today.plusDays(MAX_TRIP_DAYS))
    }

    fun canSelectEndDate(startDate: LocalDate, date: LocalDate): Boolean {
        val today = LocalDate.now()
        return date.isAfter(startDate) &&
            !date.isAfter(startDate.plusDays(MAX_TRIP_DAYS)) &&
            !date.isAfter(today.plusDays(MAX_TRIP_DAYS))
    }

    // Get maximum selectable end date (start date + 90 days)
    fun getMaxEndDate(startDate: LocalDate?): LocalDate? {
        val today = LocalDate.now()
        return startDate?.plusDays(MAX_TRIP_DAYS)?.let { maxEnd ->
            if (maxEnd.isAfter(today.plusDays(MAX_TRIP_DAYS))) today.plusDays(MAX_TRIP_DAYS) else maxEnd
        }
    }

    // Validate all date constraints
    private fun validateDates(startDate: LocalDate, endDate: LocalDate): String? {
        val today = LocalDate.now()

        if (startDate.isBefore(today)) {
            return "Ngày đi phải lớn hơn hoặc bằng ngày hôm nay"
        }

        if (!startDate.isBefore(today.plusDays(MAX_TRIP_DAYS))) {
            return "Ngày đi phải sớm hơn ngày hôm nay 90 ngày"
        }

        if (!endDate.isAfter(startDate)) {
            return "Ngày về phải sau ngày đi ít nhất 1 ngày"
        }

        if (endDate.isAfter(startDate.plusDays(MAX_TRIP_DAYS))) {
            return "Ngày về không được vượt quá 90 ngày từ ngày đi"
        }

        if (endDate.isAfter(today.plusDays(MAX_TRIP_DAYS))) {
            return "Ngày về không được vượt quá 90 ngày kể từ hôm nay"
        }

        return null
    }

    fun createTrip(onCreated: (Long) -> Unit) {
        val state = _uiState.value
        val name = state.name.trim()
        val destination = state.destination.trim()
        val startDate = parseDisplayDate(state.startDate.trim())
        val endDate = parseDisplayDate(state.endDate.trim())

        if (name.isBlank() || destination.isBlank() || startDate == null || endDate == null) {
            _uiState.update { it.copy(errorMessage = "Vui lòng nhập đủ tên, điểm đến và ngày đi") }
            return
        }

        val dateError = validateDates(startDate, endDate)
        if (dateError != null) {
            _uiState.update { it.copy(errorMessage = dateError) }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, errorMessage = null) }
            val request = CreateTripRequest(
                name = name,
                destination = destination,
                startDate = startDate.toString(),
                endDate = endDate.toString(),
                coverImageUrl = state.destinationCoverImageUrl,
                budgetMin = state.budgetMin.trim().toDoubleOrNull(),
                budgetMax = state.budgetMax.trim().toDoubleOrNull()
            )

            tripRepository.createTrip(request)
                .onSuccess { tripId ->
                    _uiState.update { it.copy(isSaving = false, errorMessage = null) }
                    onCreated(tripId)
                }
                .onFailure { throwable ->
                    _uiState.update {
                        it.copy(
                            isSaving = false,
                            errorMessage = throwable.message ?: "Không tạo được chuyến đi"
                        )
                    }
                }
        }
    }

    private fun parseDisplayDate(value: String): LocalDate? {
        if (value.isBlank()) {
            return null
        }

        return runCatching {
            LocalDate.parse(value, displayDateFormatter)
        }.getOrNull()
    }
}