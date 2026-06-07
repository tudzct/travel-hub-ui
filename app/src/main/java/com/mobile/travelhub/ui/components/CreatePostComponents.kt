package com.mobile.travelhub.ui.components

import androidx.compose.ui.res.stringResource
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.draw.drawBehind
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.ExpandMore
import androidx.compose.material.icons.rounded.Public
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.mobile.travelhub.viewmodels.CreatePostUiState
import com.mobile.travelhub.viewmodels.CreatePostViewModel
import com.mobile.travelhub.R

@Composable
fun CreatePostScreenContent(
    uiState: CreatePostUiState,
    onDescriptionChange: (String) -> Unit,
    onClose: () -> Unit,
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
            canSubmit = uiState.canSubmit,
            onClose = onClose,
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
            onDescriptionChange = { onDescriptionChange(it.take(MaxPostDescriptionLength)) },
            isSubmitting = uiState.isSubmitting
        )

        // ── Divider ──
        HorizontalDivider(
            modifier = Modifier.padding(horizontal = 24.dp),
            thickness = 1.dp,
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
        )

        Spacer(modifier = Modifier.height(18.dp))

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

        Spacer(modifier = Modifier.height(24.dp))

        ShareExperienceHint()

        Spacer(modifier = Modifier.height(24.dp))

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
    canSubmit: Boolean,
    onClose: () -> Unit,
    onSubmitPost: () -> Unit
) {
    Box(
    modifier = Modifier
        .fillMaxWidth()
        .height(56.dp)
    ) {
        Text(
            text = stringResource(R.string.ui_03f9ddf337),
            modifier = Modifier.align(Alignment.Center),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )

        TextButton(
            onClick = onSubmitPost,
            enabled = canSubmit,
            modifier = Modifier.align(Alignment.CenterEnd),
            colors = ButtonDefaults.textButtonColors(
                contentColor = MaterialTheme.colorScheme.primary,
                disabledContentColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.42f)
            )
        ) {
            Text(
                text = if (isSubmitting) "Đang đăng..." else "Đăng",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

private const val MaxPostDescriptionLength = 500

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
            .padding(start = 24.dp, end = 18.dp, top = 16.dp, bottom = 18.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceVariant)
        ) {
            if (userAvatarUrl != null) {
                AsyncImage(
                    model = userAvatarUrl,
                    contentDescription = stringResource(R.string.ui_900527c977),
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

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = userName.ifBlank { "User" },
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(6.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(RoundedCornerShape(18.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.72f))
                    .padding(horizontal = 10.dp, vertical = 5.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.Public,
                    contentDescription = stringResource(R.string.ui_cde252312d),
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = stringResource(R.string.ui_cde252312d),
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 13.sp
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
            .padding(horizontal = 24.dp)
            .padding(bottom = 16.dp)
    ) {
        SimpleFormTextField(
            value = description,
            onValueChange = onDescriptionChange,
            placeholder = stringResource(R.string.ui_eeed751665),
            enabled = !isSubmitting,
            singleLine = false,
            minLines = 5,
            maxLines = 8,
            shape = RoundedCornerShape(18.dp),
            focusedContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.07f),
            unfocusedContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.06f),
            disabledContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.05f),
            textStyle = MaterialTheme.typography.bodyLarge.copy(
                lineHeight = 26.sp,
                fontSize = 18.sp
            ),
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 156.dp)
        )

        Text(
            text = "${description.length}/$MaxPostDescriptionLength",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 20.dp, bottom = 16.dp)
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
    onSelectPlace: (Long?) -> Unit,
    onRetryProvinces: () -> Unit,
    onRetryPlaces: () -> Unit
) {
    Column(modifier = Modifier.padding(horizontal = 24.dp)) {
        DestinationPlacePicker(
            label = stringResource(R.string.ui_1e0698362b),
            selectedProvince = uiState.selectedProvince,
            selectedPlace = uiState.selectedPlace,
            provinces = uiState.provinces,
            places = uiState.places,
            isLoading = uiState.isLoadingLocations,
            enabled = !uiState.isSubmitting &&
                (uiState.provinces.isNotEmpty() || uiState.provinceErrorMessage != null),
            placeholder = stringResource(R.string.ui_9433146e77),
            labelLeadingIcon = Icons.Filled.LocationOn,
            uppercaseLabel = false,
            compactAnchor = true,
            anchorContainerColor = MaterialTheme.colorScheme.surface,
            anchorBorderColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.65f),
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
    Column(modifier = Modifier.padding(horizontal = 24.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Outlined.Image,
                contentDescription = null,
                modifier = Modifier.size(22.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = stringResource(
                    R.string.selected_image_count,
                    selectedImages.size,
                    CreatePostViewModel.MAX_IMAGE_COUNT
                ),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Spacer(modifier = Modifier.height(18.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(324.dp)
                .clip(RoundedCornerShape(18.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.16f))
                .dashedBorder(
                    color = MaterialTheme.colorScheme.outlineVariant,
                    radius = 18.dp
                )
                .clickable(enabled = !isSubmitting, onClick = onOpenImagePicker)
                .padding(horizontal = 22.dp, vertical = 28.dp)
        ) {
            Column(
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(bottom = 36.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.10f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Image,
                        contentDescription = null,
                        modifier = Modifier.size(34.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Add,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = stringResource(R.string.ui_0f74781e9d),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Bạn có thể chọn tối đa ${CreatePostViewModel.MAX_IMAGE_COUNT} ảnh",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.72f)
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                repeat(CreatePostViewModel.MAX_IMAGE_COUNT) { index ->
                    ImageSlot(
                        uri = selectedImages.getOrNull(index),
                        enabled = !isSubmitting,
                        onAdd = onOpenImagePicker,
                        onRemove = {
                            selectedImages.getOrNull(index)?.let(onRemoveImage)
                        },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
private fun ImageSlot(
    uri: Uri?,
    enabled: Boolean,
    onAdd: () -> Unit,
    onRemove: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .aspectRatio(1f)
            .clip(RoundedCornerShape(14.dp))
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f),
                shape = RoundedCornerShape(14.dp)
            )
            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.72f))
            .clickable(enabled = enabled, onClick = if (uri == null) onAdd else onRemove),
        contentAlignment = Alignment.Center
    ) {
        if (uri == null) {
            Icon(
                imageVector = Icons.Outlined.Add,
                contentDescription = stringResource(R.string.ui_0f74781e9d),
                modifier = Modifier.size(30.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.38f)
            )
        } else {
            AsyncImage(
                model = uri,
                contentDescription = stringResource(R.string.ui_f6b00f2f1b),
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(4.dp)
                    .size(22.dp)
                    .clip(CircleShape)
                    .background(Color.Black.copy(alpha = 0.62f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.Close,
                    contentDescription = stringResource(R.string.ui_a501ea7f86),
                    modifier = Modifier.size(14.dp),
                    tint = Color.White
                )
            }
        }
    }
}

private fun Modifier.dashedBorder(
    color: Color,
    radius: androidx.compose.ui.unit.Dp
): Modifier = drawBehind {
    val strokeWidth = 1.dp.toPx()
    drawRoundRect(
        color = color,
        topLeft = Offset(strokeWidth / 2, strokeWidth / 2),
        size = size.copy(
            width = size.width - strokeWidth,
            height = size.height - strokeWidth
        ),
        cornerRadius = CornerRadius(radius.toPx(), radius.toPx()),
        style = Stroke(
            width = strokeWidth,
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(14f, 10f), 0f)
        )
    )
}

@Composable
private fun ShareExperienceHint() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp),
        verticalAlignment = Alignment.Top
    ) {
        Icon(
            imageVector = Icons.Outlined.Info,
            contentDescription = null,
            modifier = Modifier
                .padding(top = 2.dp)
                .size(22.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = "Hãy chia sẻ trải nghiệm thật của bạn để giúp mọi người có chuyến đi tuyệt vời hơn!",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            lineHeight = 21.sp
        )
    }
}
