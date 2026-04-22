package com.mobile.travelhub.viewmodels

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mobile.travelhub.data.LocationRepository
import com.mobile.travelhub.data.PlaceRepository
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
    val isSubmitting: Boolean = false,
    val errorMessage: String? = null,
    val isPostCreated: Boolean = false
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
    private val placeRepository: PlaceRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CreatePostUiState())
    val uiState: StateFlow<CreatePostUiState> = _uiState.asStateFlow()

    init {
        loadProvinces()
    }

    fun updateDescription(value: String) {
        _uiState.update { it.copy(description = value) }
    }

    fun setSelectedImages(images: List<Uri>) {
        _uiState.update { it.copy(selectedImages = images.take(MAX_IMAGE_COUNT)) }
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
                errorMessage = null
            )
        }
        loadPlaces(provinceId)
    }

    fun selectPlace(placeId: Long?) {
        _uiState.update { it.copy(selectedPlaceId = placeId, errorMessage = null) }
    }

    fun submitPost() {
        val current = _uiState.value
        if (current.isSubmitting) return

        if (current.description.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Description is required") }
            return
        }

        if (current.selectedImages.isEmpty()) {
            _uiState.update { it.copy(errorMessage = "Please select at least one image") }
            return
        }

        if (current.selectedPlaceId == null) {
            _uiState.update { it.copy(errorMessage = "Please select a place") }
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
                        errorMessage = throwable.message ?: "Failed to create post"
                    )
                }
            }
        }
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
                        selectedPlaceId = null,
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

    companion object {
        const val MAX_IMAGE_COUNT = 5
    }
}
