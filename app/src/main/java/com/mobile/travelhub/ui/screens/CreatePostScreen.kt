package com.mobile.travelhub.ui.screens

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import coil.compose.AsyncImage
import com.mobile.travelhub.ui.components.PrimaryProfileButton
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
            viewModel.setSelectedImages(uris)
        }
    }

    LaunchedEffect(uiState.errorMessage) {
        val errorMessage = uiState.errorMessage ?: return@LaunchedEffect
        Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show()
        viewModel.clearErrorMessage()
    }

    LaunchedEffect(uiState.isPostCreated) {
        if (!uiState.isPostCreated) return@LaunchedEffect
        Toast.makeText(context, "Post created successfully", Toast.LENGTH_SHORT).show()
        viewModel.consumePostCreated()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 16.dp)
    ) {
        Text(
            text = "Create Post",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = uiState.description,
            onValueChange = viewModel::updateDescription,
            label = { Text("Description") },
            placeholder = { Text("Share your travel story...") },
            minLines = 4,
            maxLines = 8,
            modifier = Modifier.fillMaxWidth(),
            enabled = !uiState.isSubmitting
        )

        Spacer(modifier = Modifier.height(16.dp))

        PrimaryProfileButton(
            text = if (uiState.selectedImages.isEmpty()) "Select Images" else "Change Images",
            onClick = {
                imagePickerLauncher.launch(
                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                )
            },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Selected images: ${uiState.selectedImages.size}/${CreatePostViewModel.MAX_IMAGE_COUNT}",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        if (uiState.selectedImages.isNotEmpty()) {
            Spacer(modifier = Modifier.height(12.dp))

            LazyRow {
                items(uiState.selectedImages) { uri ->
                    Box(
                        modifier = Modifier
                            .padding(end = 12.dp)
                            .clip(RoundedCornerShape(12.dp))
                    ) {
                        AsyncImage(
                            model = uri,
                            contentDescription = "Selected image",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .height(100.dp)
                                .fillMaxWidth(0.33f)
                                .clip(RoundedCornerShape(12.dp))
                        )

                        Box(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(6.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.9f))
                                .clickable(enabled = !uiState.isSubmitting) { viewModel.removeImage(uri) }
                                .padding(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Close,
                                contentDescription = "Remove image",
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }
        } else {
            Spacer(modifier = Modifier.height(16.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Outlined.Image,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "No images selected",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        PrimaryProfileButton(
            text = if (uiState.isSubmitting) "Posting..." else "Post",
            onClick = viewModel::submitPost,
            modifier = Modifier.fillMaxWidth()
        )

        if (uiState.isSubmitting) {
            Spacer(modifier = Modifier.height(12.dp))
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }

        Spacer(modifier = Modifier.height(12.dp))
    }
}
