package com.mobile.travelhub.viewmodels

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mobile.travelhub.data.LocationRepository
import com.mobile.travelhub.data.PlaceRepository
import com.mobile.travelhub.data.userMessage
import com.mobile.travelhub.data.api.UserApiService
import com.mobile.travelhub.data.model.AdminProvinceResponse
import com.mobile.travelhub.data.model.TravelPlaceListItemResponse
import com.mobile.travelhub.usecase.CreatePostUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class CreatePostUiState(
    val description: String = "",
    val selectedImages: List<Uri> = emptyList(),
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
    val isSubmitting: Boolean = false,
    val errorMessage: String? = null,
    val isPostCreated: Boolean = false,
    val userName: String = "",
    val userAvatarUrl: String? = null
) {
    val selectedProvince: AdminProvinceResponse?
        get() = provinces.firstOrNull { it.id == selectedProvinceId }

    val selectedPlace: TravelPlaceListItemResponse?
        get() = places.firstOrNull { it.id == selectedPlaceId }

    val resolvedLocation: String
        get() = listOfNotNull(
            selectedPlace?.name,
            selectedPlace?.province?.name ?: selectedProvince?.name
        ).distinct().joinToString(", ")

    val canSubmit: Boolean
        get() = description.isNotBlank() &&
            selectedImages.isNotEmpty() &&
            selectedPlaceId != null &&
            !isSubmitting
}

@HiltViewModel
class CreatePostViewModel @Inject constructor(
    private val createPostUseCase: CreatePostUseCase,
    private val locationRepository: LocationRepository,
    private val placeRepository: PlaceRepository,
    private val userApiService: UserApiService,
    private val postInteractionEventBus: PostInteractionEventBus
) : ViewModel() {

    private val _uiState = MutableStateFlow(CreatePostUiState())
    val uiState: StateFlow<CreatePostUiState> = _uiState.asStateFlow()
    private var placeSearchJob: Job? = null
    private var placesRequestId = 0

    init {
        collectPostInteractionEvents()
        loadProvinces()
        loadUserProfile()
    }

    fun updateDescription(value: String) {
        _uiState.update { it.copy(description = value) }
    }

    fun addSelectedImages(images: List<Uri>) {
        _uiState.update { state ->
            state.copy(
                selectedImages = (state.selectedImages + images)
                    .distinct()
                    .take(MAX_IMAGE_COUNT)
            )
        }
    }

    fun removeImage(imageUri: Uri) {
        _uiState.update { state ->
            state.copy(selectedImages = state.selectedImages.filterNot { it == imageUri })
        }
    }

    fun clearErrorMessage() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    fun consumePostCreated() {
        _uiState.update { it.copy(isPostCreated = false) }
    }

    fun selectProvince(provinceId: Long) {
        placeSearchJob?.cancel()
        _uiState.update {
            it.copy(
                selectedProvinceId = provinceId,
                selectedPlaceId = null,
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
        _uiState.update { it.copy(selectedPlaceId = placeId, errorMessage = null) }
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

    fun submitPost() {
        val current = _uiState.value
        if (current.isSubmitting) return

        if (current.description.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Vui lòng nhập mô tả bài viết") }
            return
        }

        if (current.selectedImages.isEmpty()) {
            _uiState.update { it.copy(errorMessage = "Vui lòng chọn ít nhất một ảnh") }
            return
        }

        if (current.selectedPlaceId == null) {
            _uiState.update { it.copy(errorMessage = "Vui lòng chọn địa điểm") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true, errorMessage = null, isPostCreated = false) }

            createPostUseCase(
                description = current.description,
                imageUris = current.selectedImages,
                travelPlaceId = current.selectedPlaceId
            ).onSuccess {
                _uiState.update {
                    it.copy(
                        description = "",
                        selectedImages = emptyList(),
                        selectedPlaceId = null,
                        places = emptyList(),
                        placeQuery = "",
                        placesPage = 0,
                        placesTotalPages = 0,
                        isSubmitting = false,
                        isPostCreated = true,
                        errorMessage = null
                    )
                }
                _uiState.value.selectedProvinceId?.let { loadPlaces(provinceId = it) }
            }.onFailure { throwable ->
                _uiState.update {
                    it.copy(
                        isSubmitting = false,
                        errorMessage = throwable.userMessage("Không thể tạo bài viết")
                    )
                }
            }
        }
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
                        selectedPlaceId = null,
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

    fun loadUserProfile() {
        viewModelScope.launch {
            runCatching { userApiService.getMyProfile() }
                .onSuccess { profile ->
                    _uiState.update {
                        it.copy(
                            userName = profile.name,
                            userAvatarUrl = profile.avatarUrl
                        )
                    }
                }
        }
    }

    private fun collectPostInteractionEvents() {
        viewModelScope.launch {
            postInteractionEventBus.events.collect { event ->
                if (event is PostInteractionEvent.UserProfileChanged) {
                    _uiState.update {
                        it.copy(
                            userName = event.name.takeIf { name -> name.isNotBlank() } ?: it.userName,
                            userAvatarUrl = event.avatarUrl?.takeIf { avatarUrl -> avatarUrl.isNotBlank() }
                        )
                    }
                }
            }
        }
    }

    companion object {
        const val MAX_IMAGE_COUNT = 5
        private const val PLACES_PAGE_SIZE = 20
        private const val PLACE_SEARCH_DEBOUNCE_MS = 350L
    }
}
