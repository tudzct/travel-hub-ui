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
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.rounded.Public
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
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
    onRetryProvinces: () -> Unit,
    onRetryPlaces: () -> Unit,
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
        CreatePostTopBar(
            isSubmitting = uiState.isSubmitting,
            onSubmitPost = onSubmitPost
        )

        // ── User Header Section ──
        UserHeaderSection(
            userName = uiState.userName,
            userAvatarUrl = uiState.userAvatarUrl
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
            onSelectPlace = onSelectPlace,
            onRetryProvinces = onRetryProvinces,
            onRetryPlaces = onRetryPlaces
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

        if (uiState.isSubmitting) {
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

@Composable
private fun CreatePostTopBar(
    isSubmitting: Boolean,
    onSubmitPost: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 20.dp, end = 8.dp, top = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "TẠO BÀI VIẾT",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp,
            color = MaterialTheme.colorScheme.onSurface
        )
        TextButton(
            onClick = onSubmitPost,
            enabled = !isSubmitting
        ) {
            Text(
                text = if (isSubmitting) "Đang đăng..." else "Đăng",
                fontWeight = FontWeight.Bold
            )
        }
    }
}

// ──────────────────────────────────────────────────────────────────────────────
// User Header — Avatar + Name + Public badge
// ──────────────────────────────────────────────────────────────────────────────

@Composable
private fun UserHeaderSection(
    userName: String,
    userAvatarUrl: String?
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
    SimpleFormTextField(
        value = description,
        onValueChange = onDescriptionChange,
        placeholder = "Bạn đang nghĩ gì?",
        enabled = !isSubmitting,
        singleLine = false,
        minLines = 4,
        maxLines = 8,
        textStyle = MaterialTheme.typography.bodyLarge.copy(lineHeight = 24.sp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .padding(bottom = 16.dp)
    )
}

// ──────────────────────────────────────────────────────────────────────────────
// Travel Place Section — Province + Place dropdowns
// ──────────────────────────────────────────────────────────────────────────────

@Composable
private fun TravelPlaceSection(
    uiState: CreatePostUiState,
    onSelectProvince: (Long) -> Unit,
    onSelectPlace: (Long?) -> Unit,
    onRetryProvinces: () -> Unit,
    onRetryPlaces: () -> Unit
) {
    Column(modifier = Modifier.padding(horizontal = 20.dp)) {
        DestinationPlacePicker(
            label = "Địa điểm du lịch",
            selectedProvince = uiState.selectedProvince,
            selectedPlace = uiState.selectedPlace,
            provinces = uiState.provinces,
            places = uiState.places,
            isLoading = uiState.isLoadingLocations,
            enabled = !uiState.isSubmitting &&
                (uiState.provinces.isNotEmpty() || uiState.provinceErrorMessage != null),
            placeholder = "Chọn địa điểm",
            onProvinceSelected = onSelectProvince,
            onPlaceSelected = onSelectPlace,
            provinceErrorMessage = uiState.provinceErrorMessage,
            placesErrorMessage = uiState.placesErrorMessage,
            onRetryProvinces = onRetryProvinces,
            onRetryPlaces = onRetryPlaces
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

            if (selectedImages.isNotEmpty() &&
                selectedImages.size < CreatePostViewModel.MAX_IMAGE_COUNT
            ) {
                TextButton(
                    onClick = onOpenImagePicker,
                    enabled = !isSubmitting
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Image,
                        contentDescription = null,
                        modifier = Modifier.size(17.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Thêm ảnh",
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        if (selectedImages.isNotEmpty()) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                selectedImages.forEach { uri ->
                    PostImageItem(
                        uri = uri,
                        isSubmitting = isSubmitting,
                        onRemove = { onRemoveImage(uri) }
                    )
                }
            }
        } else {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(4f / 3f)
                    .clip(RoundedCornerShape(16.dp))
                    .border(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.outlineVariant,
                        shape = RoundedCornerShape(16.dp)
                    )
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
                    .clickable(enabled = !isSubmitting, onClick = onOpenImagePicker),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Image,
                            contentDescription = null,
                            modifier = Modifier.size(24.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Thêm ảnh",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

@Composable
private fun PostImageItem(
    uri: Uri,
    isSubmitting: Boolean,
    onRemove: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(4f / 3f)
            .clip(RoundedCornerShape(16.dp))
    ) {
        AsyncImage(
            model = uri,
            contentDescription = "Ảnh bài viết",
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(10.dp)
                .size(32.dp)
                .clip(CircleShape)
                .background(Color.Black.copy(alpha = 0.58f))
                .clickable(enabled = !isSubmitting, onClick = onRemove),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Outlined.Close,
                contentDescription = "Xóa ảnh",
                modifier = Modifier.size(18.dp),
                tint = Color.White
            )
        }
    }
}

