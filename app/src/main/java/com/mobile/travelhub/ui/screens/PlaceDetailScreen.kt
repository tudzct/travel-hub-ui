package com.mobile.travelhub.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import coil.compose.AsyncImage
import com.mobile.travelhub.data.model.TravelPlaceReviewResponse
import com.mobile.travelhub.viewmodels.PlaceDetailViewModel
import com.mobile.travelhub.viewmodels.ReviewViewModel
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlaceDetailScreen(
    placeId: Long,
    onBack: () -> Unit,
    onEdit: (Long) -> Unit,
    onShowAllReviews: (Long) -> Unit,
    onRequireLogin: () -> Unit,
    placeDetailViewModel: PlaceDetailViewModel = hiltViewModel(),
    reviewViewModel: ReviewViewModel = hiltViewModel()
) {
    val uiState by placeDetailViewModel.uiState.collectAsState()
    val reviewUiState by reviewViewModel.uiState.collectAsState()
    var showReviewSheet by remember { mutableStateOf(false) }

    LaunchedEffect(placeId) {
        placeDetailViewModel.loadPlace(placeId)
    }

    LaunchedEffect(showReviewSheet, uiState.detail?.myReview?.id) {
        if (showReviewSheet) {
            reviewViewModel.initialize(uiState.detail?.myReview)
        }
    }

    LaunchedEffect(reviewUiState.submittedReview?.id, reviewUiState.submittedReview?.updatedAt) {
        val submittedReview = reviewUiState.submittedReview ?: return@LaunchedEffect
        placeDetailViewModel.applyReviewSaved(submittedReview)
        showReviewSheet = false
        reviewViewModel.consumeSubmittedReview()
    }

    LaunchedEffect(reviewUiState.unauthorized) {
        if (reviewUiState.unauthorized) {
            reviewViewModel.clearUnauthorized()
            showReviewSheet = false
            onRequireLogin()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        when {
            uiState.isLoading && uiState.detail == null -> {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = MaterialTheme.colorScheme.primary
                )
            }

            uiState.errorMessage != null && uiState.detail == null -> {
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
                        text = "Không thể tải địa điểm",
                        style = MaterialTheme.typography.headlineSmall
                    )
                    Text(
                        text = uiState.errorMessage.orEmpty(),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            uiState.detail != null -> {
                val detail = uiState.detail ?: return
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item {
                        DetailTopBar(
                            title = detail.name,
                            isAdmin = uiState.isAdmin,
                            onBack = onBack,
                            onEdit = { onEdit(detail.id) }
                        )
                    }

                    item {
                        LazyRow(
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(detail.images.ifEmpty { listOf() }, key = { it.id }) { image ->
                                AsyncImage(
                                    model = image.imageUrl,
                                    contentDescription = detail.name,
                                    modifier = Modifier
                                        .width(320.dp)
                                        .height(220.dp)
                                        .clip(RoundedCornerShape(28.dp)),
                                    contentScale = ContentScale.Crop
                                )
                            }
                            if (detail.images.isEmpty()) {
                                item {
                                    Surface(
                                        modifier = Modifier
                                            .width(320.dp)
                                            .height(220.dp),
                                        shape = RoundedCornerShape(28.dp),
                                        color = MaterialTheme.colorScheme.surfaceContainerLow
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Text("No image")
                                        }
                                    }
                                }
                            }
                        }
                    }

                    item {
                        Column(
                            modifier = Modifier.padding(horizontal = 16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                text = detail.name,
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = detail.province.name,
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                            detail.openingTime?.takeIf { it.isNotBlank() }?.let {
                                Text(
                                    text = "Giờ mở cửa: $it",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Text(
                                text = "Lượt xem: ${detail.views ?: 0}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    item {
                        Surface(
                            modifier = Modifier.padding(horizontal = 16.dp),
                            shape = RoundedCornerShape(24.dp),
                            color = MaterialTheme.colorScheme.surfaceContainerLow
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Text(
                                    text = "Mô tả",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = detail.description.orEmpty().ifBlank { "Chưa có mô tả." },
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    item {
                        ReviewSummaryCard(
                            averageRating = detail.reviewSummary.averageRating,
                            reviewCount = detail.reviewSummary.reviewCount,
                            myReview = detail.myReview,
                            onWriteReview = { showReviewSheet = true }
                        )
                    }

                    item {
                        ReviewPreviewSection(
                            reviews = uiState.reviewPreview,
                            isLoading = uiState.reviewPreviewLoading,
                            errorMessage = uiState.reviewErrorMessage,
                            onShowAll = { onShowAllReviews(detail.id) }
                        )
                    }

                    item {
                        Spacer(modifier = Modifier.height(96.dp))
                    }
                }
            }
        }

        if (showReviewSheet && uiState.detail != null) {
            ReviewBottomSheet(
                uiState = reviewUiState,
                onDismiss = { showReviewSheet = false },
                onRatingChange = reviewViewModel::updateRating,
                onContentChange = reviewViewModel::updateContent,
                onSubmit = { reviewViewModel.submit(uiState.detail!!.id) }
            )
        }
    }
}

@Composable
private fun DetailTopBar(
    title: String,
    isAdmin: Boolean,
    onBack: () -> Unit,
    onEdit: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back"
                )
            }
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        if (isAdmin) {
            OutlinedButton(onClick = onEdit) {
                Text("Sửa địa điểm")
            }
        }
    }
}

@Composable
private fun ReviewSummaryCard(
    averageRating: Double,
    reviewCount: Long,
    myReview: TravelPlaceReviewResponse?,
    onWriteReview: () -> Unit
) {
    Surface(
        modifier = Modifier.padding(horizontal = 16.dp),
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLow
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = String.format("%.1f", averageRating),
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "$reviewCount reviews",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Button(onClick = onWriteReview) {
                    Text(if (myReview == null) "Viết đánh giá" else "Sửa đánh giá")
                }
            }

            if (myReview != null) {
                Text(
                    text = "Đánh giá của bạn: ${myReview.rating} sao",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
private fun ReviewPreviewSection(
    reviews: List<TravelPlaceReviewResponse>,
    isLoading: Boolean,
    errorMessage: String?,
    onShowAll: () -> Unit
) {
    Column(
        modifier = Modifier.padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Review gần đây",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = "Xem tất cả",
                modifier = Modifier.clickable(onClick = onShowAll),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary
            )
        }

        when {
            isLoading -> {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }

            errorMessage != null -> {
                Text(
                    text = errorMessage,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error
                )
            }

            reviews.isEmpty() -> {
                Text(
                    text = "Chưa có review nào cho địa điểm này.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            else -> {
                reviews.forEach { review ->
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = MaterialTheme.colorScheme.surfaceContainerLow
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = review.user.name.ifBlank { review.user.username },
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Text(
                                        text = formatBackendInstant(review.updatedAt ?: review.createdAt),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Star,
                                        contentDescription = null,
                                        tint = Color(0xFFFFB800),
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = review.rating.toString(),
                                        style = MaterialTheme.typography.labelLarge,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                            Text(
                                text = review.content,
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ReviewBottomSheet(
    uiState: com.mobile.travelhub.viewmodels.ReviewUiState,
    onDismiss: () -> Unit,
    onRatingChange: (Int) -> Unit,
    onContentChange: (String) -> Unit,
    onSubmit: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Review địa điểm",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                (1..5).forEach { star ->
                    Surface(
                        modifier = Modifier.clickable { onRatingChange(star) },
                        shape = CircleShape,
                        color = if (star <= uiState.rating) Color(0xFFFFF4D6) else MaterialTheme.colorScheme.surfaceContainerLow
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .padding(8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = "$star star",
                                tint = if (star <= uiState.rating) Color(0xFFFFB800) else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            OutlinedTextField(
                value = uiState.content,
                onValueChange = onContentChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Nội dung review") },
                minLines = 4
            )

            uiState.errorMessage?.let { error ->
                Text(
                    text = error,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error
                )
            }

            Button(
                onClick = onSubmit,
                enabled = !uiState.isSubmitting,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (uiState.isSubmitting) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("Gửi review")
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}

private fun formatBackendInstant(raw: String?): String {
    if (raw.isNullOrBlank()) {
        return "Không rõ thời gian"
    }
    return runCatching {
        val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")
            .withZone(ZoneId.systemDefault())
        formatter.format(Instant.parse(raw))
    }.getOrDefault(raw)
}
