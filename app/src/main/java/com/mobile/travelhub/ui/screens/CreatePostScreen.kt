package com.mobile.travelhub.ui.screens

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.mobile.travelhub.R
import com.mobile.travelhub.ui.components.CreatePostScreenContent
import com.mobile.travelhub.viewmodels.CreatePostViewModel

@Composable
fun CreatePostScreen(
    viewModel: CreatePostViewModel = hiltViewModel()
) {
    val uiState = viewModel.uiState.collectAsState().value
    val context = LocalContext.current

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(maxItems = CreatePostViewModel.MAX_IMAGE_COUNT)
    ) { uris ->
        if (uris.isNotEmpty()) {
            viewModel.addSelectedImages(uris)
        }
    }

    LaunchedEffect(Unit) {
        viewModel.loadUserProfile()
    }

    LaunchedEffect(uiState.errorMessage) {
        val errorMessage = uiState.errorMessage ?: return@LaunchedEffect
        Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show()
        viewModel.clearErrorMessage()
    }

    LaunchedEffect(uiState.isPostCreated) {
        if (!uiState.isPostCreated) return@LaunchedEffect
        Toast.makeText(
            context,
            context.getString(R.string.post_created_successfully),
            Toast.LENGTH_SHORT
        ).show()
        viewModel.consumePostCreated()
    }

    CreatePostScreenContent(
        uiState = uiState,
        onDescriptionChange = viewModel::updateDescription,
        onSelectProvince = viewModel::selectProvince,
        onSelectPlace = viewModel::selectPlace,
        onRetryProvinces = viewModel::retryLoadProvinces,
        onRetryPlaces = viewModel::retryLoadPlaces,
        onOpenImagePicker = {
            imagePickerLauncher.launch(
                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
            )
        },
        onRemoveImage = viewModel::removeImage,
        onSubmitPost = viewModel::submitPost
    )
}
