package com.mobile.travelhub.viewmodels

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
    val isSubmitting: Boolean = false,
    val errorMessage: String? = null,
    val isPostCreated: Boolean = false
)

@HiltViewModel
class CreatePostViewModel @Inject constructor(
    private val createPostUseCase: CreatePostUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(CreatePostUiState())
    val uiState: StateFlow<CreatePostUiState> = _uiState.asStateFlow()

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

        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true, errorMessage = null, isPostCreated = false) }

            createPostUseCase(
                description = current.description,
                imageUris = current.selectedImages
            ).onSuccess {
                _uiState.update {
                    it.copy(
                        description = "",
                        selectedImages = emptyList(),
                        isSubmitting = false,
                        isPostCreated = true,
                        errorMessage = null
                    )
                }
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

    companion object {
        const val MAX_IMAGE_COUNT = 5
    }
}
