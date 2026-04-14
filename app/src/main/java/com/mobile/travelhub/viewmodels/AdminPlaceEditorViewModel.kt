package com.mobile.travelhub.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mobile.travelhub.data.AuthRepository
import com.mobile.travelhub.data.PlaceRepository
import com.mobile.travelhub.data.httpStatusCode
import com.mobile.travelhub.data.model.TravelPlaceDetailResponse
import com.mobile.travelhub.data.model.UpsertTravelPlaceRequest
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AdminPlaceEditorUiState(
    val isAdmin: Boolean = false,
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val isEditMode: Boolean = false,
    val errorMessage: String? = null,
    val savedPlaceId: Long? = null,
    val provinceId: String = "",
    val name: String = "",
    val description: String = "",
    val lat: String = "",
    val lon: String = "",
    val openingTime: String = "",
    val imageUrls: List<String> = listOf("")
)

@HiltViewModel
class AdminPlaceEditorViewModel @Inject constructor(
    private val placeRepository: PlaceRepository,
    authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AdminPlaceEditorUiState(isAdmin = authRepository.isAdmin()))
    val uiState: StateFlow<AdminPlaceEditorUiState> = _uiState.asStateFlow()

    private var loadedPlaceId: Long? = null

    fun loadForEdit(placeId: Long) {
        if (loadedPlaceId == placeId) {
            return
        }
        loadedPlaceId = placeId
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, isEditMode = true, errorMessage = null, savedPlaceId = null) }
            runCatching { placeRepository.getPlaceDetail(placeId) }
                .onSuccess { detail -> applyDetail(detail) }
                .onFailure { throwable ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = throwable.message ?: "Không thể tải địa điểm"
                        )
                    }
                }
        }
    }

    private fun applyDetail(detail: TravelPlaceDetailResponse) {
        _uiState.update {
            it.copy(
                isLoading = false,
                provinceId = detail.province.id.toString(),
                name = detail.name,
                description = detail.description.orEmpty(),
                lat = detail.lat?.toString().orEmpty(),
                lon = detail.lon?.toString().orEmpty(),
                openingTime = detail.openingTime.orEmpty(),
                imageUrls = if (detail.images.isEmpty()) listOf("") else detail.images.map { image -> image.imageUrl }
            )
        }
    }

    fun updateProvinceId(value: String) {
        _uiState.update { it.copy(provinceId = value, errorMessage = null) }
    }

    fun updateName(value: String) {
        _uiState.update { it.copy(name = value, errorMessage = null) }
    }

    fun updateDescription(value: String) {
        _uiState.update { it.copy(description = value, errorMessage = null) }
    }

    fun updateLat(value: String) {
        _uiState.update { it.copy(lat = value, errorMessage = null) }
    }

    fun updateLon(value: String) {
        _uiState.update { it.copy(lon = value, errorMessage = null) }
    }

    fun updateOpeningTime(value: String) {
        _uiState.update { it.copy(openingTime = value, errorMessage = null) }
    }

    fun updateImageUrl(index: Int, value: String) {
        val updated = uiState.value.imageUrls.toMutableList()
        updated[index] = value
        _uiState.update { it.copy(imageUrls = updated, errorMessage = null) }
    }

    fun addImageField() {
        _uiState.update { it.copy(imageUrls = it.imageUrls + "") }
    }

    fun removeImageField(index: Int) {
        val updated = uiState.value.imageUrls.toMutableList()
        if (updated.size == 1) {
            updated[0] = ""
        } else {
            updated.removeAt(index)
        }
        _uiState.update { it.copy(imageUrls = updated, errorMessage = null) }
    }

    fun submit(placeId: Long?) {
        val provinceId = uiState.value.provinceId.trim().toLongOrNull()
        if (provinceId == null) {
            _uiState.update { it.copy(errorMessage = "provinceId phải là số hợp lệ") }
            return
        }
        if (uiState.value.name.trim().isBlank()) {
            _uiState.update { it.copy(errorMessage = "Tên địa điểm là bắt buộc") }
            return
        }

        val request = UpsertTravelPlaceRequest(
            provinceId = provinceId,
            name = uiState.value.name.trim(),
            description = uiState.value.description.trim().ifBlank { null },
            lat = uiState.value.lat.trim().takeIf { it.isNotBlank() }?.toDoubleOrNull(),
            lon = uiState.value.lon.trim().takeIf { it.isNotBlank() }?.toDoubleOrNull(),
            openingTime = uiState.value.openingTime.trim().ifBlank { null },
            imageUrls = uiState.value.imageUrls.map { it.trim() }.filter { it.isNotBlank() }
        )

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, errorMessage = null, savedPlaceId = null) }
            val result = runCatching {
                if (placeId == null) {
                    placeRepository.createPlace(request)
                } else {
                    placeRepository.updatePlace(placeId, request)
                }
            }
            result.onSuccess { detail ->
                _uiState.update { it.copy(isSaving = false, savedPlaceId = detail.id, errorMessage = null) }
            }.onFailure { throwable ->
                _uiState.update {
                    it.copy(
                        isSaving = false,
                        errorMessage = when (throwable.httpStatusCode()) {
                            400 -> "Dữ liệu địa điểm không hợp lệ"
                            401 -> "Bạn cần đăng nhập lại"
                            403 -> "Bạn không có quyền quản trị địa điểm"
                            404 -> "Không tìm thấy địa điểm"
                            else -> throwable.message ?: "Không thể lưu địa điểm"
                        }
                    )
                }
            }
        }
    }

    fun consumeSavedPlaceId() {
        _uiState.update { it.copy(savedPlaceId = null) }
    }
}
