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
    val provinceErrorMessage: String? = null,
    val placesErrorMessage: String? = null,
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
}

@HiltViewModel
class CreatePostViewModel @Inject constructor(
    private val createPostUseCase: CreatePostUseCase,
    private val locationRepository: LocationRepository,
    private val placeRepository: PlaceRepository,
    private val userApiService: UserApiService
) : ViewModel() {

    private val _uiState = MutableStateFlow(CreatePostUiState())
    val uiState: StateFlow<CreatePostUiState> = _uiState.asStateFlow()

    init {
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
        _uiState.update {
            it.copy(
                selectedProvinceId = provinceId,
                selectedPlaceId = null,
                places = emptyList(),
                placesErrorMessage = null,
                errorMessage = null
            )
        }
        loadPlaces(provinceId)
    }

    fun selectPlace(placeId: Long?) {
        _uiState.update { it.copy(selectedPlaceId = placeId, errorMessage = null) }
    }

    fun retryLoadProvinces() {
        loadProvinces()
    }

    fun retryLoadPlaces() {
        _uiState.value.selectedProvinceId?.let(::loadPlaces)
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
                        isSubmitting = false,
                        isPostCreated = true,
                        errorMessage = null
                    )
                }
                _uiState.value.selectedProvinceId?.let(::loadPlaces)
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

    private fun loadPlaces(provinceId: Long) {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isLoadingLocations = true,
                    placesErrorMessage = null
                )
            }

            runCatching {
                placeRepository.getPlaces(page = 0, pageSize = 100, provinceId = provinceId).data
            }.onSuccess { places ->
                _uiState.update {
                    it.copy(
                        places = places,
                        selectedPlaceId = null,
                        isLoadingLocations = false,
                        placesErrorMessage = null
                    )
                }
            }.onFailure { throwable ->
                _uiState.update {
                    it.copy(
                        isLoadingLocations = false,
                        places = emptyList(),
                        placesErrorMessage = throwable.userMessage("Không thể tải danh sách địa điểm")
                    )
                }
            }
        }
    }

    private fun loadUserProfile() {
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

    companion object {
        const val MAX_IMAGE_COUNT = 5
    }
}
