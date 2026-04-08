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
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.mobile.travelhub.models.EditablePlaceDraft
import com.mobile.travelhub.viewmodels.PlaceViewModel

@Composable
fun EditPlaceScreen(
    placeId: String,
    onBack: () -> Unit,
    onSaved: () -> Unit,
    placeViewModel: PlaceViewModel = hiltViewModel()
) {
    val place by placeViewModel.observePlace(placeId).collectAsState(initial = null)

    if (place == null) {
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
                text = "Place not found",
                style = MaterialTheme.typography.headlineSmall
            )
        }
        return
    }

    val detail = place ?: return
    var title by remember(detail.id) { mutableStateOf(detail.title) }
    var provinceName by remember(detail.id) { mutableStateOf(detail.provinceName) }
    var bestTime by remember(detail.id) { mutableStateOf(detail.bestTime.orEmpty()) }
    var mainImageUrl by remember(detail.id) { mutableStateOf(detail.mainImageUrl) }
    var description by remember(detail.id) { mutableStateOf(detail.description) }
    var errorMessage by remember(detail.id) { mutableStateOf<String?>(null) }
    val galleryUrls = remember(detail.id) { mutableStateListOf<String>() }

    LaunchedEffect(detail.id) {
        galleryUrls.clear()
        if (detail.galleryUrls.isEmpty()) {
            galleryUrls.add("")
        } else {
            galleryUrls.addAll(detail.galleryUrls)
        }
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
                onClick = {
                    val result = placeViewModel.updatePlace(
                        placeId = detail.id,
                        draft = EditablePlaceDraft(
                            title = title,
                            description = description,
                            provinceName = provinceName,
                            bestTime = bestTime,
                            mainImageUrl = mainImageUrl,
                            galleryUrls = galleryUrls.toList()
                        )
                    )

                    result.onSuccess {
                        errorMessage = null
                        onSaved()
                    }.onFailure { throwable ->
                        errorMessage = throwable.message ?: "Unable to save changes"
                    }
                }
            ) {
                Text("Save")
            }
        }

        Text(
            text = "Edit destination",
            style = MaterialTheme.typography.headlineMedium
        )

        errorMessage?.let { error ->
            Text(
                text = error,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium
            )
        }

        OutlinedTextField(
            value = title,
            onValueChange = { title = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Title") },
            singleLine = true
        )

        OutlinedTextField(
            value = provinceName,
            onValueChange = { provinceName = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Province / City") },
            singleLine = true
        )

        OutlinedTextField(
            value = bestTime,
            onValueChange = { bestTime = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Best time") },
            singleLine = true
        )

        OutlinedTextField(
            value = mainImageUrl,
            onValueChange = { mainImageUrl = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Main image URL") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri)
        )

        OutlinedTextField(
            value = description,
            onValueChange = { description = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Description") },
            minLines = 5
        )

        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(
                text = "Gallery image URLs",
                style = MaterialTheme.typography.titleMedium
            )

            galleryUrls.forEachIndexed { index, url ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = url,
                        onValueChange = { newValue -> galleryUrls[index] = newValue },
                        modifier = Modifier.weight(1f),
                        label = { Text("Image ${index + 1}") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri)
                    )
                    OutlinedButton(
                        onClick = {
                            if (galleryUrls.size > 1) {
                                galleryUrls.removeAt(index)
                            } else {
                                galleryUrls[index] = ""
                            }
                        },
                        modifier = Modifier.padding(top = 8.dp)
                    ) {
                        Text("Remove")
                    }
                }
            }

            OutlinedButton(onClick = { galleryUrls.add("") }) {
                Text("Add image")
            }
        }
    }
}
