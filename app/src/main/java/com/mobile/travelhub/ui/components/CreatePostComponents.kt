package com.mobile.travelhub.ui.components

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.rounded.Public
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.mobile.travelhub.viewmodels.CreatePostUiState
import com.mobile.travelhub.viewmodels.CreatePostViewModel

@Composable
fun CreatePostScreenContent(
    uiState: CreatePostUiState,
    onDescriptionChange: (String) -> Unit,
    onSelectProvince: (Long) -> Unit,
    onSelectPlace: (Long?) -> Unit,
    onOpenImagePicker: () -> Unit,
    onRemoveImage: (Uri) -> Unit,
    onSubmitPost: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
    ) {
        // ── User Header Section ──
        UserHeaderSection(
            userName = uiState.userName,
            userAvatarUrl = uiState.userAvatarUrl,
            isSubmitting = uiState.isSubmitting
        )

        // ── Description Input ──
        DescriptionInputSection(
            description = uiState.description,
            onDescriptionChange = onDescriptionChange,
            isSubmitting = uiState.isSubmitting
        )

        // ── Divider ──
        HorizontalDivider(
            modifier = Modifier.padding(horizontal = 20.dp),
            thickness = 1.dp,
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
        )

        Spacer(modifier = Modifier.height(20.dp))

        // ── Travel Place Section ──
        TravelPlaceSection(
            uiState = uiState,
            onSelectProvince = onSelectProvince,
            onSelectPlace = onSelectPlace
        )

        Spacer(modifier = Modifier.height(24.dp))

        // ── Selected Images Section ──
        SelectedImagesSection(
            selectedImages = uiState.selectedImages,
            isSubmitting = uiState.isSubmitting,
            onOpenImagePicker = onOpenImagePicker,
            onRemoveImage = onRemoveImage
        )

        Spacer(modifier = Modifier.height(28.dp))

        // ── Submit Button ──
        Box(modifier = Modifier.padding(horizontal = 20.dp)) {
            PrimaryProfileButton(
                text = if (uiState.isSubmitting) "Đăng bài..." else "Đăng bài",
                onClick = onSubmitPost,
                modifier = Modifier.fillMaxWidth()
            )
        }

        if (uiState.isSubmitting) {
            Spacer(modifier = Modifier.height(16.dp))
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                SkeletonBlock(
                    modifier = Modifier
                        .width(96.dp)
                        .height(10.dp),
                    shape = RoundedCornerShape(5.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

// ──────────────────────────────────────────────────────────────────────────────
// User Header — Avatar + Name + Public badge
// ──────────────────────────────────────────────────────────────────────────────

@Composable
private fun UserHeaderSection(
    userName: String,
    userAvatarUrl: String?,
    isSubmitting: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Avatar
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceVariant)
        ) {
            if (userAvatarUrl != null) {
                AsyncImage(
                    model = userAvatarUrl,
                    contentDescription = "User avatar",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                // Placeholder
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = userName.firstOrNull()?.uppercase() ?: "?",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column {
            Text(
                text = userName.ifBlank { "User" },
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(2.dp))

            // Public visibility badge
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.Public,
                    contentDescription = "Công khai",
                    modifier = Modifier.size(12.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.width(3.dp))
                Text(
                    text = "Công khai",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 10.sp
                )
            }
        }
    }
}

// ──────────────────────────────────────────────────────────────────────────────
// Description Input — borderless, large text area
// ──────────────────────────────────────────────────────────────────────────────

@Composable
private fun DescriptionInputSection(
    description: String,
    onDescriptionChange: (String) -> Unit,
    isSubmitting: Boolean
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 120.dp)
            .padding(horizontal = 20.dp)
            .padding(bottom = 16.dp)
    ) {
        BasicTextField(
            value = description,
            onValueChange = onDescriptionChange,
            enabled = !isSubmitting,
            textStyle = MaterialTheme.typography.bodyLarge.copy(
                color = MaterialTheme.colorScheme.onSurface,
                lineHeight = 24.sp
            ),
            cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
            modifier = Modifier.fillMaxWidth(),
            decorationBox = { innerTextField ->
                Box {
                    if (description.isEmpty()) {
                        Text(
                            text = "Bạn đang nghĩ gì?",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                        )
                    }
                    innerTextField()
                }
            }
        )
    }
}

// ──────────────────────────────────────────────────────────────────────────────
// Travel Place Section — Province + Place dropdowns
// ──────────────────────────────────────────────────────────────────────────────

@Composable
private fun TravelPlaceSection(
    uiState: CreatePostUiState,
    onSelectProvince: (Long) -> Unit,
    onSelectPlace: (Long?) -> Unit
) {
    Column(modifier = Modifier.padding(horizontal = 20.dp)) {
        DestinationPlacePicker(
            label = "Địa điểm du lịch",
            selectedProvince = uiState.selectedProvince,
            selectedPlace = uiState.selectedPlace,
            provinces = uiState.provinces,
            places = uiState.places,
            isLoading = uiState.isLoadingLocations,
            enabled = !uiState.isSubmitting && uiState.provinces.isNotEmpty(),
            placeholder = "Chọn địa điểm",
            onProvinceSelected = onSelectProvince,
            onPlaceSelected = onSelectPlace
        )
    }
}

// ──────────────────────────────────────────────────────────────────────────────
// Selected Images Section
// ──────────────────────────────────────────────────────────────────────────────

@Composable
private fun SelectedImagesSection(
    selectedImages: List<Uri>,
    isSubmitting: Boolean,
    onOpenImagePicker: () -> Unit,
    onRemoveImage: (Uri) -> Unit
) {
    Column(modifier = Modifier.padding(horizontal = 20.dp)) {
        // Header row: title + count + button
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "ẢNH: ${selectedImages.size}/${CreatePostViewModel.MAX_IMAGE_COUNT}",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                letterSpacing = 1.sp
            )

            // Select Images button
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .clickable(enabled = !isSubmitting, onClick = onOpenImagePicker)
                    .border(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.primary,
                        shape = RoundedCornerShape(8.dp)
                    )
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Outlined.Image,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Select Images",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        if (selectedImages.isNotEmpty()) {
            // Image thumbnails
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(selectedImages) { uri ->
                    Box(
                        modifier = Modifier
                            .size(100.dp)
                            .clip(RoundedCornerShape(12.dp))
                    ) {
                        AsyncImage(
                            model = uri,
                            contentDescription = "Chọn ảnh",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(RoundedCornerShape(12.dp))
                        )

                        // Remove button
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(4.dp)
                                .size(22.dp)
                                .clip(CircleShape)
                                .background(Color.Black.copy(alpha = 0.55f))
                                .clickable(enabled = !isSubmitting) { onRemoveImage(uri) },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Close,
                                contentDescription = "Xóa ảnh",
                                modifier = Modifier.size(14.dp),
                                tint = Color.White
                            )
                        }
                    }
                }
            }
        } else {
            // Empty state
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Outlined.Image,
                        contentDescription = null,
                        modifier = Modifier.size(32.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Chưa có ảnh nào",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                    )
                }
            }
        }
    }
}

