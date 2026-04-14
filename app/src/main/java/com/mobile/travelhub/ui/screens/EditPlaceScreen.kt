package com.mobile.travelhub.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.mobile.travelhub.viewmodels.AdminPlaceEditorViewModel

@Composable
fun EditPlaceScreen(
    placeId: Long?,
    onBack: () -> Unit,
    onSaved: (Long) -> Unit,
    viewModel: AdminPlaceEditorViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(placeId) {
        if (placeId != null) {
            viewModel.loadForEdit(placeId)
        }
    }

    LaunchedEffect(uiState.savedPlaceId) {
        val savedPlaceId = uiState.savedPlaceId ?: return@LaunchedEffect
        viewModel.consumeSavedPlaceId()
        onSaved(savedPlaceId)
    }

    if (!uiState.isAdmin) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedButton(onClick = onBack) {
                Text("Back")
            }
            Text(
                text = "Bạn không có quyền quản trị địa điểm",
                style = MaterialTheme.typography.headlineSmall
            )
        }
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            OutlinedButton(onClick = onBack) {
                Text("Cancel")
            }
            Button(
                onClick = { viewModel.submit(placeId) },
                enabled = !uiState.isSaving && !uiState.isLoading
            ) {
                Text(if (uiState.isSaving) "Saving..." else "Save")
            }
        }

        Text(
            text = if (placeId == null) "Thêm địa điểm" else "Sửa địa điểm",
            style = MaterialTheme.typography.headlineMedium
        )

        uiState.errorMessage?.let { error ->
            Text(
                text = error,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium
            )
        }

        OutlinedTextField(
            value = uiState.provinceId,
            onValueChange = viewModel::updateProvinceId,
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Province ID") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true
        )

        OutlinedTextField(
            value = uiState.name,
            onValueChange = viewModel::updateName,
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Tên địa điểm") },
            singleLine = true
        )

        OutlinedTextField(
            value = uiState.description,
            onValueChange = viewModel::updateDescription,
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Mô tả") },
            minLines = 5
        )

        OutlinedTextField(
            value = uiState.lat,
            onValueChange = viewModel::updateLat,
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Latitude") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
        )

        OutlinedTextField(
            value = uiState.lon,
            onValueChange = viewModel::updateLon,
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Longitude") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
        )

        OutlinedTextField(
            value = uiState.openingTime,
            onValueChange = viewModel::updateOpeningTime,
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Giờ mở cửa") },
            singleLine = true
        )

        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(
                text = "Image URLs",
                style = MaterialTheme.typography.titleMedium
            )
            uiState.imageUrls.forEachIndexed { index, url ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = url,
                        onValueChange = { viewModel.updateImageUrl(index, it) },
                        modifier = Modifier.weight(1f),
                        label = { Text("Image ${index + 1}") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri)
                    )
                    OutlinedButton(
                        onClick = { viewModel.removeImageField(index) },
                        modifier = Modifier.padding(top = 8.dp)
                    ) {
                        Text("Remove")
                    }
                }
            }

            OutlinedButton(onClick = viewModel::addImageField) {
                Text("Add image")
            }
        }
    }
}
