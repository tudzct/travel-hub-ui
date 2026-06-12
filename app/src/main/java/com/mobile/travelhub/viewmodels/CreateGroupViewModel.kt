package com.mobile.travelhub.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mobile.travelhub.data.LocationRepository
import com.mobile.travelhub.data.PlaceRepository
import com.mobile.travelhub.data.TripRepository
import com.mobile.travelhub.data.userMessage
import com.mobile.travelhub.data.model.CreateTripRequest
import com.mobile.travelhub.data.model.AdminProvinceResponse
import com.mobile.travelhub.data.model.TravelPlaceListItemResponse
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
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
    val isLoadingMorePlaces: Boolean = false,
    val provinceErrorMessage: String? = null,
    val placesErrorMessage: String? = null,
    val placesLoadMoreErrorMessage: String? = null,
    val placeQuery: String = "",
    val placesPage: Int = 0,
    val placesTotalPages: Int = 0,
    val startDate: String = "",
    val endDate: String = "",
    val budgetMin: String = "0",
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
        private const val PLACES_PAGE_SIZE = 20
        private const val PLACE_SEARCH_DEBOUNCE_MS = 350L
    }

    private val displayDateFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")

    private val _uiState = MutableStateFlow(CreateGroupUiState())
    val uiState: StateFlow<CreateGroupUiState> = _uiState.asStateFlow()
    private var placeSearchJob: Job? = null
    private var placesRequestId = 0

    init {
        loadProvinces()
    }

    fun updateName(value: String) {
        _uiState.update { it.copy(name = value, errorMessage = null) }
    }

    fun updateDestination(value: String) {
        placeSearchJob?.cancel()
        placesRequestId++
        _uiState.update {
            it.copy(
                destination = value,
                destinationCoverImageUrl = null,
                selectedProvinceId = null,
                selectedPlaceId = null,
                places = emptyList(),
                isLoadingLocations = false,
                isLoadingMorePlaces = false,
                placeQuery = "",
                placesPage = 0,
                placesTotalPages = 0,
                errorMessage = null
            )
        }
    }

    fun selectProvince(provinceId: Long?) {
        placeSearchJob?.cancel()
        if (provinceId == null) {
            placesRequestId++
            _uiState.update {
                it.copy(
                    selectedProvinceId = null,
                    selectedPlaceId = null,
                    destination = "",
                    destinationCoverImageUrl = null,
                    places = emptyList(),
                    isLoadingLocations = false,
                    isLoadingMorePlaces = false,
                    placeQuery = "",
                    placesPage = 0,
                    placesTotalPages = 0,
                    placesErrorMessage = null,
                    placesLoadMoreErrorMessage = null,
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
                placeQuery = "",
                placesPage = 0,
                placesTotalPages = 0,
                placesErrorMessage = null,
                placesLoadMoreErrorMessage = null,
                errorMessage = null
            )
        }
        loadPlaces(provinceId = provinceId)
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
        // Fetch place detail to get full images list and use first image as cover
        viewModelScope.launch {
            _uiState.update {
                it.copy(selectedPlaceId = placeId, destination = formatDestination(place), destinationCoverImageUrl = null, errorMessage = null)
            }
            runCatching { placeRepository.getPlaceDetail(placeId) }
                .onSuccess { detail ->
                    val firstImage = detail.images.firstOrNull()?.imageUrl ?: place.mainImage
                    _uiState.update { it.copy(destinationCoverImageUrl = firstImage) }
                }
                .onFailure {
                    // fallback to mainImage
                    _uiState.update { it.copy(destinationCoverImageUrl = place.mainImage) }
                }
        }
    }

    fun refreshDestinationOptions() {
        if (_uiState.value.provinces.isEmpty()) {
            loadProvinces()
        }
    }

    fun retryLoadProvinces() {
        loadProvinces()
    }

    fun retryLoadPlaces() {
        placeSearchJob?.cancel()
        val state = _uiState.value
        state.selectedProvinceId?.let {
            loadPlaces(provinceId = it, query = state.placeQuery)
        }
    }

    fun updatePlaceQuery(value: String) {
        val provinceId = _uiState.value.selectedProvinceId ?: return
        placesRequestId++
        _uiState.update {
            it.copy(
                placeQuery = value,
                selectedPlaceId = null,
                destination = "",
                destinationCoverImageUrl = null,
                places = emptyList(),
                isLoadingLocations = true,
                isLoadingMorePlaces = false,
                placesPage = 0,
                placesTotalPages = 0,
                placesErrorMessage = null,
                placesLoadMoreErrorMessage = null
            )
        }
        placeSearchJob?.cancel()
        placeSearchJob = viewModelScope.launch {
            delay(PLACE_SEARCH_DEBOUNCE_MS)
            loadPlaces(provinceId = provinceId, query = value)
        }
    }

    fun loadMorePlaces() {
        val state = _uiState.value
        val provinceId = state.selectedProvinceId ?: return
        if (
            state.isLoadingLocations ||
            state.isLoadingMorePlaces ||
            state.placesPage + 1 >= state.placesTotalPages
        ) {
            return
        }

        val nextPage = state.placesPage + 1
        val query = state.placeQuery
        val requestId = placesRequestId
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isLoadingMorePlaces = true,
                    placesLoadMoreErrorMessage = null
                )
            }
            runCatching {
                placeRepository.getPlaces(
                    page = nextPage,
                    pageSize = PLACES_PAGE_SIZE,
                    provinceId = provinceId,
                    keyword = query.trim().takeIf(String::isNotEmpty)
                )
            }.onSuccess { response ->
                if (requestId != placesRequestId || _uiState.value.placeQuery != query) return@onSuccess
                val existingIds = _uiState.value.places.mapTo(hashSetOf()) { it.id }
                _uiState.update {
                    it.copy(
                        places = it.places + response.data.filterNot { place -> place.id in existingIds },
                        isLoadingMorePlaces = false,
                        placesLoadMoreErrorMessage = null,
                        placesPage = response.pageNumber,
                        placesTotalPages = response.totalPages
                    )
                }
            }.onFailure { throwable ->
                if (requestId != placesRequestId || _uiState.value.placeQuery != query) return@onFailure
                _uiState.update {
                    it.copy(
                        isLoadingMorePlaces = false,
                        placesLoadMoreErrorMessage = throwable.userMessage("Không thể tải thêm địa điểm")
                    )
                }
            }
        }
    }

    fun updateStartDate(value: String) {
        _uiState.update { it.copy(startDate = value, errorMessage = null) }
    }

    fun updateEndDate(value: String) {
        _uiState.update { it.copy(endDate = value, errorMessage = null) }
    }

    fun updateBudgetMax(value: String) {
        _uiState.update { it.copy(budgetMax = value, errorMessage = null) }
    }

    private fun loadProvinces() {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isLoadingLocations = true,
                    provinceErrorMessage = null
                )
            }
            runCatching { locationRepository.getProvinces() }
                .onSuccess { provinces ->
                    _uiState.update {
                        it.copy(
                            provinces = provinces,
                            isLoadingLocations = false,
                            provinceErrorMessage = null
                        )
                    }
                }
                .onFailure { throwable ->
                    _uiState.update {
                        it.copy(
                            isLoadingLocations = false,
                            provinceErrorMessage = throwable.userMessage("Không thể tải danh sách tỉnh/thành phố")
                        )
                    }
                }
        }
    }

    private fun loadPlaces(provinceId: Long, query: String = "") {
        val requestId = ++placesRequestId
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isLoadingLocations = true,
                    isLoadingMorePlaces = false,
                    placesErrorMessage = null,
                    placesLoadMoreErrorMessage = null
                )
            }
            runCatching {
                placeRepository.getPlaces(
                    page = 0,
                    pageSize = PLACES_PAGE_SIZE,
                    provinceId = provinceId,
                    keyword = query.trim().takeIf(String::isNotEmpty)
                )
            }.onSuccess { response ->
                if (
                    requestId != placesRequestId ||
                    _uiState.value.selectedProvinceId != provinceId ||
                    _uiState.value.placeQuery != query
                ) {
                    return@onSuccess
                }
                _uiState.update {
                    it.copy(
                        places = response.data,
                        isLoadingLocations = false,
                        placesErrorMessage = null,
                        placesPage = response.pageNumber,
                        placesTotalPages = response.totalPages
                    )
                }
            }.onFailure { throwable ->
                if (
                    requestId != placesRequestId ||
                    _uiState.value.selectedProvinceId != provinceId ||
                    _uiState.value.placeQuery != query
                ) {
                    return@onFailure
                }
                _uiState.update {
                    it.copy(
                        isLoadingLocations = false,
                        places = emptyList(),
                        placesErrorMessage = throwable.userMessage("Không thể tải danh sách địa điểm"),
                        placesPage = 0,
                        placesTotalPages = 0
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
                placeId = state.selectedPlaceId,
                budgetMin = state.budgetMin.replace(".", "").trim().toDoubleOrNull() ?: 0.0,
                budgetMax = state.budgetMax.replace(".", "").trim().toDoubleOrNull()
            )

            tripRepository.createTrip(request)
                .onSuccess { tripDetail ->
                    _uiState.update { it.copy(isSaving = false, errorMessage = null) }
                    onCreated(tripDetail.tripInfo.id)
                }
                .onFailure { throwable ->
                    _uiState.update {
                        it.copy(
                            isSaving = false,
                            errorMessage = throwable.userMessage("Không tạo được chuyến đi")
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
