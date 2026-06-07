package com.mobile.travelhub.ui.screens

import androidx.compose.ui.res.stringResource
import android.content.Intent
import android.app.Activity
import android.net.Uri
import android.content.ActivityNotFoundException
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import coil3.compose.AsyncImage
import com.mobile.travelhub.data.model.TravelPlaceListItemResponse
import com.mobile.travelhub.data.model.TravelPlaceReviewResponse
import com.mobile.travelhub.ui.components.ExpandableDescription
import com.mobile.travelhub.ui.components.SimpleFormTextField
import com.mobile.travelhub.ui.components.ReviewWriteSheet
import com.mobile.travelhub.ui.components.TravelPlaceReviewCard
import com.mobile.travelhub.ui.components.TravelPlaceReviewCardSkeleton
import com.mobile.travelhub.ui.components.InlineLoadingSkeleton
import com.mobile.travelhub.ui.components.LoadingContentSkeleton
import com.mobile.travelhub.ui.components.LoadingListSkeleton
import com.mobile.travelhub.ui.components.RetryButton
import com.mobile.travelhub.ui.components.SkeletonBlock
import com.mobile.travelhub.ui.theme.OnSurface
import com.mobile.travelhub.ui.theme.OnSurfaceVariant
import com.mobile.travelhub.ui.theme.PrimaryBlue
import com.mobile.travelhub.ui.theme.SurfaceBg
import com.mobile.travelhub.ui.theme.SurfaceContainerLow
import com.mobile.travelhub.ui.theme.SurfaceContainerLowest
import com.mobile.travelhub.viewmodels.PlaceDetailUiModel
import com.mobile.travelhub.viewmodels.PlaceDetailViewModel
import com.mobile.travelhub.viewmodels.ReviewViewModel
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import com.mobile.travelhub.R

private val PlaceDetailHorizontalPadding = 16.dp
private val PlaceDetailSectionPadding = 16.dp

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun PlaceDetailScreen(
    placeId: Long,
    initialPlace: TravelPlaceListItemResponse?,
    onBack: () -> Unit,
    onPlaceClick: (TravelPlaceListItemResponse) -> Unit,
    onShowAllReviews: (Long) -> Unit,
    onReviewAuthorClick: (Long) -> Unit,
    onRequireLogin: () -> Unit,
    onUserClick: (Long) -> Unit = {},
    placeDetailViewModel: PlaceDetailViewModel = hiltViewModel(),
    reviewViewModel: ReviewViewModel = hiltViewModel()
) {
    val uiState by placeDetailViewModel.uiState.collectAsState()
    val reviewUiState by reviewViewModel.uiState.collectAsState()
    val context = LocalContext.current
    var showReviewSheet by remember { mutableStateOf(false) }

    LaunchedEffect(placeId, initialPlace?.id) {
        if (initialPlace != null) {
            placeDetailViewModel.loadPlace(initialPlace)
        } else {
            placeDetailViewModel.loadPlaceById(placeId)
        }
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
            .background(SurfaceBg)
    ) {
        when {
            uiState.isLoading && uiState.detail == null -> {
                LoadingContentSkeleton(modifier = Modifier.fillMaxSize())
            }

            uiState.errorMessage != null && uiState.detail == null -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = onBack,
                            modifier = Modifier
                                .size(44.dp)
                                .background(Color(0xFFF2F4F7), CircleShape)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = stringResource(R.string.ui_b52b36b726),
                                tint = OnSurface
                            )
                        }
                        Text(
                            text = stringResource(R.string.ui_3682846f79),
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = OnSurface
                        )
                    }
                    Text(
                        text = uiState.errorMessage.orEmpty(),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    RetryButton(
                        onClick = {
                            if (initialPlace != null) {
                                placeDetailViewModel.loadPlace(initialPlace)
                            } else {
                                placeDetailViewModel.loadPlaceById(placeId)
                            }
                        },
                        filled = true
                    )
                }
            }

            uiState.detail != null -> {
                val detail = uiState.detail ?: return
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        PlaceHeroSection(
                            detail = detail
                        )
                    }

                    item {
                        FlatSection {
                            PlaceInfoSection(detail = detail)
                        }
                    }

                    item {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                text = "Mô tả",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(horizontal = PlaceDetailHorizontalPadding)
                            )
                            FlatSection {
                                ExpandableDescription(
                                    description = detail.description.orEmpty().ifBlank { "Chưa có mô tả." },
                                    title = null
                                )
                            }
                        }
                    }

                    item {
                        PlaceActionSection(
                            myReview = detail.myReview,
                            onWriteReview = { showReviewSheet = true },
                            onDirections = { openDirections(context, detail) }
                        )
                    }

                    item {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                text = stringResource(R.string.ui_d0170783fe),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(horizontal = PlaceDetailHorizontalPadding)
                            )
                            FlatSection {
                                ReviewSummarySection(
                                    averageRating = detail.reviewSummary.averageRating,
                                    reviewCount = detail.reviewSummary.reviewCount,
                                    myReview = detail.myReview,
                                    onWriteReview = { showReviewSheet = true }
                                )
                            }
                        }
                    }

                    item {
                        ReviewPreviewSection(
                            reviews = uiState.reviewPreview,
                            isLoading = uiState.reviewPreviewLoading,
                            errorMessage = uiState.reviewErrorMessage,
                            onShowAll = { onShowAllReviews(detail.id) },
                            onUserClick = onUserClick,
                            onAuthorClick = onReviewAuthorClick
                        )
                    }

                    item {
                        RelatedPlacesSection(
                            places = uiState.relatedPlaces,
                            isLoading = uiState.relatedPlacesLoading,
                            errorMessage = uiState.relatedPlacesErrorMessage,
                            onPlaceClick = onPlaceClick
                        )
                    }

                    item {
                        Spacer(modifier = Modifier.height(18.dp))
                    }
                }

                PinnedBackButton(onBack = onBack)
            }
        }

        if (showReviewSheet && uiState.detail != null) {
            ReviewWriteSheet(
                uiState = reviewUiState,
                titleResId = R.string.ui_b19c813eda,
                placeholderResId = R.string.ui_79d0b72c6c,
                submitTextResId = R.string.ui_96ee09303d,
                onDismiss = { showReviewSheet = false },
                onRatingChange = reviewViewModel::updateRating,
                onContentChange = reviewViewModel::updateContent,
                onSubmit = { reviewViewModel.submit(uiState.detail!!.id) }
            )
        }
    }
}

@Composable
private fun PlaceHeroSection(
    detail: PlaceDetailUiModel
) {
    val imageUrls = detail.imageUrls.ifEmpty { listOfNotNull(detail.mainImage) }
    val displayUrls = remember(imageUrls) { imageUrls.map { it.trim() }.filter { it.isNotBlank() } }
    val hasImages = displayUrls.isNotEmpty()
    val imageCount = displayUrls.size.coerceAtLeast(1)
    val pagerState = rememberPagerState(pageCount = { imageCount })

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(SurfaceContainerLow)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(380.dp)
        ) {
            if (!hasImages) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(SurfaceContainerLow),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stringResource(R.string.ui_fc5e5bdcd5),
                        style = MaterialTheme.typography.bodyLarge,
                        color = OnSurfaceVariant
                    )
                }
            } else if (displayUrls.size == 1) {
                AsyncImage(
                    model = displayUrls.first(),
                    contentDescription = detail.name,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier.fillMaxSize()
                ) { page ->
                    AsyncImage(
                        model = displayUrls[page],
                        contentDescription = detail.name,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Black.copy(alpha = 0.24f),
                                Color.Transparent,
                                Color.Black.copy(alpha = 0.82f)
                            )
                        )
                    )
            )

            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .fillMaxWidth()
                    .padding(start = 24.dp, end = 24.dp, bottom = if (displayUrls.size > 1) 52.dp else 28.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                val placeRating = placeRatingLabel(
                    averageRating = detail.reviewSummary.averageRating,
                    reviewCount = detail.reviewSummary.reviewCount
                )
                if (placeRating != null) {
                    Surface(
                        shape = RoundedCornerShape(999.dp),
                        color = PrimaryBlue.copy(alpha = 0.92f)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = placeRating,
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                    }
                }
                Text(
                    text = detail.name,
                    color = Color.White,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 34.sp,
                    lineHeight = 38.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            if (displayUrls.size > 1) {
                Row(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 22.dp),
                    horizontalArrangement = Arrangement.Center
                ) {
                    repeat(displayUrls.size) { index ->
                        Box(
                            modifier = Modifier
                                .padding(horizontal = 3.dp)
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(
                                    if (pagerState.currentPage == index) {
                                        Color.White
                                    } else {
                                        Color.White.copy(alpha = 0.34f)
                                    }
                                )
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PinnedBackButton(onBack: () -> Unit) {
    Surface(
        modifier = Modifier
            .padding(start = 16.dp, top = 48.dp),
        shape = CircleShape,
        color = Color.Black.copy(alpha = 0.3f)
    ) {
        IconButton(onClick = onBack) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = stringResource(R.string.ui_b52b36b726),
                tint = Color.White
            )
        }
    }
}

@Composable
private fun FlatSection(content: @Composable () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = PlaceDetailHorizontalPadding)
            .clip(RoundedCornerShape(20.dp))
            .background(SurfaceContainerLowest)
            .padding(PlaceDetailSectionPadding)
    ) {
        content()
    }
}

@Composable
private fun PlaceInfoSection(detail: PlaceDetailUiModel) {
    Column(
        modifier = Modifier
            .fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        InfoRow(
            icon = Icons.Default.LocationOn,
            label = stringResource(R.string.ui_c12e4bb029),
            value = detail.province.name
        )
        detail.openingTime?.takeIf { it.isNotBlank() }?.let {
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f))
            InfoRow(
                icon = Icons.Default.AccessTime,
                label = stringResource(R.string.ui_be5eb8d380),
                value = it
            )
        }
    }
}

@Composable
private fun InfoRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            shape = CircleShape,
            color = PrimaryBlue.copy(alpha = 0.1f)
        ) {
            Box(
                modifier = Modifier.size(40.dp),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = PrimaryBlue,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = OnSurfaceVariant
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyLarge,
                color = OnSurface,
                fontWeight = FontWeight.SemiBold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun RelatedPlacesSection(
    places: List<TravelPlaceListItemResponse>,
    isLoading: Boolean,
    errorMessage: String?,
    onPlaceClick: (TravelPlaceListItemResponse) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = PlaceDetailHorizontalPadding),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.ui_a50f97d8d3),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
        }

        when {
            isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    LoadingListSkeleton(itemCount = 2)
                }
            }

            !errorMessage.isNullOrBlank() -> {
                Text(
                    text = errorMessage,
                    modifier = Modifier.padding(horizontal = 16.dp),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error
                )
            }

            places.isEmpty() -> Unit

            else -> {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = PlaceDetailHorizontalPadding),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(places, key = { it.id }) { place ->
                        RelatedPlaceCard(
                            place = place,
                            onClick = { onPlaceClick(place) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun RelatedPlaceCard(
    place: TravelPlaceListItemResponse,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .width(196.dp)
            .height(146.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(SurfaceContainerLow)
            .clickable(onClick = onClick)
    ) {
        if (place.mainImage.isNullOrBlank()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(SurfaceContainerLow),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = place.province.name.take(1).uppercase(),
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
            }
        } else {
            AsyncImage(
                model = place.mainImage,
                contentDescription = place.name,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        }
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color.Black.copy(alpha = 0.68f)
                        )
                    )
                )
        )
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = place.name,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = Color.White,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = place.province.name,
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White.copy(alpha = 0.84f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.width(8.dp))
                placeRatingLabel(
                    averageRating = place.averageRating,
                    reviewCount = place.reviewCount
                )?.let { rating ->
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = null,
                        tint = Color(0xFFFFB800),
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(
                        text = rating,
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

@Composable
private fun ReviewSummarySection(
    averageRating: Double,
    reviewCount: Long,
    myReview: TravelPlaceReviewResponse?,
    onWriteReview: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = null,
                    tint = Color(0xFFFFB800),
                    modifier = Modifier.size(28.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    val ratingLabel = placeRatingLabel(
                        averageRating = averageRating,
                        reviewCount = reviewCount
                    )
                    if (ratingLabel != null) {
                        Text(
                            text = ratingLabel,
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )
                    } else {
                        Text(
                            text = "Chưa có đánh giá",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    Text(
                        text = if (reviewCount > 0) {
                            stringResource(R.string.review_count, reviewCount)
                        } else {
                            "0 đánh giá"
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Button(
                onClick = onWriteReview,
                colors = ButtonDefaults.buttonColors(
                    containerColor = PrimaryBlue,
                    contentColor = Color.White
                )
            ) {
                Text(
                    text = if (myReview == null) "Viết đánh giá" else "Sửa đánh giá",
                    color = Color.White
                )
            }
        }

        if (myReview != null) {
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = PrimaryBlue.copy(alpha = 0.08f)
            ) {
                Text(
                    text = stringResource(R.string.your_rating, myReview.rating),
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    style = MaterialTheme.typography.bodyMedium,
                    color = PrimaryBlue
                )
            }
        }
    }
}

private fun placeRatingLabel(averageRating: Double, reviewCount: Long): String? {
    if (reviewCount <= 0L || averageRating <= 0.0) {
        return null
    }
    return String.format("%.1f", averageRating)
}

@Composable
private fun ReviewPreviewSection(
    reviews: List<TravelPlaceReviewResponse>,
    isLoading: Boolean,
    errorMessage: String?,
    onShowAll: () -> Unit,
    onUserClick: (Long) -> Unit,
    onAuthorClick: (Long) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = PlaceDetailHorizontalPadding),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.ui_520354f2ab),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = stringResource(R.string.ui_7e04025452),
                modifier = Modifier.clickable(onClick = onShowAll),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary
            )
        }

        when {
            isLoading -> {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    repeat(2) {
                        TravelPlaceReviewCardSkeleton(horizontalPadding = 0.dp)
                    }
                }
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
                    text = stringResource(R.string.ui_332a8650bb),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            else -> {
                reviews.forEach { review ->
                    TravelPlaceReviewCard(
                        review = review,
                        onAuthorClick = { onAuthorClick(review.user.id) },
                        horizontalPadding = 0.dp
                    )
                }
            }
        }
    }
}

private fun formatReviewTimestamp(raw: String?): String {
    val formatted = formatBackendInstant(raw)
    return if (formatted.contains("/") && formatted.contains(" ")) {
        formatted.replaceFirst(" ", " • ")
    } else {
        formatted
    }
}

@Composable
private fun PlaceActionSection(
    myReview: TravelPlaceReviewResponse?,
    onWriteReview: () -> Unit,
    onDirections: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = PlaceDetailHorizontalPadding),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        OutlinedButton(
            onClick = onDirections,
            modifier = Modifier
                .weight(1f)
                .height(52.dp),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, PrimaryBlue.copy(alpha = 0.35f)),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = Color.White,
                containerColor = PrimaryBlue
            )
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.OpenInNew,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = stringResource(R.string.ui_982c95c3d7),
                fontWeight = FontWeight.Bold
            )
        }
    }
}

private fun openDirections(
    context: android.content.Context,
    detail: PlaceDetailUiModel
) {
    val label = listOf(detail.name, detail.province.name)
        .filter { it.isNotBlank() }
        .joinToString(", ")
    val intents = listOf(
        Intent(Intent.ACTION_VIEW, Uri.parse("geo:0,0?q=${Uri.encode(label)}")),
        Intent(
            Intent.ACTION_VIEW,
            Uri.parse("https://www.google.com/maps/search/?api=1&query=${Uri.encode(label)}")
        ).apply {
            addCategory(Intent.CATEGORY_BROWSABLE)
        }
    )

    for (intent in intents) {
        if (context !is Activity) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        try {
            if (intent.resolveActivity(context.packageManager) != null) {
                context.startActivity(intent)
                return
            }
        } catch (_: Exception) {
            continue
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
