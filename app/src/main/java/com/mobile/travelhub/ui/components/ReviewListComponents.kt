package com.mobile.travelhub.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.KeyboardArrowDown
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.Dp
import coil.compose.AsyncImage
import com.mobile.travelhub.data.model.TravelPlaceReviewResponse
import com.mobile.travelhub.ui.theme.OnSurface
import com.mobile.travelhub.ui.theme.OnSurfaceVariant
import com.mobile.travelhub.ui.theme.PrimaryBlue
import com.mobile.travelhub.ui.theme.SurfaceBg
import com.mobile.travelhub.ui.theme.SurfaceContainerLow
import com.mobile.travelhub.viewmodels.ReviewListUiState
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import kotlin.math.roundToInt

@Composable
fun ReviewListScreenContent(
    uiState: ReviewListUiState,
    onBack: () -> Unit,
    onRatingFilterSelected: (Int?) -> Unit,
    onSortSelected: (String) -> Unit,
    onLoadMore: () -> Unit,
    onAuthorClick: (Long) -> Unit,
    onWriteReview: () -> Unit
) {
    Scaffold(
        containerColor = SurfaceBg,
        topBar = {
            ReviewListTopBar(
                reviewCount = uiState.summary?.reviewCount ?: uiState.totalElements,
                onBack = onBack
            )
        },
        bottomBar = {
            Button(
                onClick = onWriteReview,
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(horizontal = 20.dp, vertical = 12.dp)
                    .height(56.dp),
                shape = RoundedCornerShape(999.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = PrimaryBlue,
                    contentColor = Color.White
                )
            ) {
                Icon(
                    imageVector = Icons.Filled.Create,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "Viết đánh giá",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    ) { innerPadding ->
        when {
            uiState.summary == null && uiState.isLoading -> {
                ReviewListSkeleton(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                )
            }

            uiState.errorMessage != null -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = uiState.errorMessage.orEmpty(),
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }

            else -> {
                ReviewListBody(
                    uiState = uiState,
                    modifier = Modifier.padding(innerPadding),
                    onRatingFilterSelected = onRatingFilterSelected,
                    onSortSelected = onSortSelected,
                    onLoadMore = onLoadMore,
                    onAuthorClick = onAuthorClick
                )
            }
        }
    }
}

@Composable
private fun ReviewListBody(
    uiState: ReviewListUiState,
    modifier: Modifier = Modifier,
    onRatingFilterSelected: (Int?) -> Unit,
    onSortSelected: (String) -> Unit,
    onLoadMore: () -> Unit,
    onAuthorClick: (Long) -> Unit
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            ReviewSummaryHeader(uiState = uiState)
        }
        item {
            ReviewFilterSection(
                selectedRating = uiState.selectedRating,
                enabled = !uiState.isLoading,
                onRatingSelected = onRatingFilterSelected
            )
        }
        item {
            ReviewSortRow(
                selectedSort = uiState.sort,
                enabled = !uiState.isLoading,
                onSortSelected = onSortSelected
            )
        }

        if (uiState.items.isEmpty() && !uiState.isLoading) {
            item {
                Text(
                    text = "Chưa có đánh giá phù hợp",
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 32.dp),
                    color = OnSurfaceVariant,
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        } else if (uiState.isLoading) {
            items(if (uiState.items.isEmpty()) 5 else 2) {
                TravelPlaceReviewCardSkeleton()
            }
        } else {
            items(uiState.items, key = { it.id }) { review ->
                TravelPlaceReviewCard(
                    review = review,
                    onAuthorClick = { onAuthorClick(review.user.id) }
                )
            }
        }

        if (uiState.isLoadingMore) {
            items(2) {
                TravelPlaceReviewCardSkeleton()
            }
        } else if (uiState.hasMore && !uiState.isLoading) {
            item {
                LaunchedEffect(uiState.page, uiState.selectedRating) {
                    onLoadMore()
                }
            }
        }
    }
}

@Composable
private fun ReviewListTopBar(
    reviewCount: Long,
    onBack: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 12.dp, end = 12.dp, top = 12.dp, bottom = 8.dp)
            .height(48.dp),
        contentAlignment = Alignment.Center
    ) {
        IconButton(
            onClick = onBack,
            modifier = Modifier.align(Alignment.CenterStart)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Quay lại",
                tint = OnSurface
            )
        }
        Text(
            text = "Đánh giá ($reviewCount)",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.ExtraBold,
            color = OnSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun ReviewSummaryHeader(uiState: ReviewListUiState) {
    val summary = uiState.summary
    val total = summary?.reviewCount ?: uiState.totalElements
    val average = summary?.averageRating ?: 0.0

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(24.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier.width(116.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (total > 0) {
                Text(
                    text = String.format("%.1f", average),
                    color = OnSurface,
                    fontSize = 48.sp,
                    lineHeight = 52.sp,
                    fontWeight = FontWeight.ExtraBold
                )
                RatingStars(rating = average, starSize = 22)
            } else {
                Text(
                    text = "Chưa có đánh giá",
                    color = OnSurface,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }
            Text(
                text = ratingLabel(average, total),
                modifier = Modifier.padding(top = 6.dp),
                color = OnSurface,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = "$total đánh giá",
                modifier = Modifier.padding(top = 4.dp),
                color = OnSurfaceVariant,
                style = MaterialTheme.typography.bodyMedium
            )
        }

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            for (rating in 5 downTo 1) {
                RatingDistributionRow(
                    rating = rating,
                    count = uiState.ratingCount(rating),
                    total = total
                )
            }
        }
    }
}

@Composable
private fun RatingDistributionRow(
    rating: Int,
    count: Long,
    total: Long
) {
    val progress = if (total <= 0L) 0f else (count.toFloat() / total.toFloat()).coerceIn(0f, 1f)

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(
            text = "$rating sao",
            modifier = Modifier.width(46.dp),
            color = OnSurface,
            style = MaterialTheme.typography.bodyMedium
        )
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .weight(1f)
                .height(8.dp)
                .clip(RoundedCornerShape(999.dp)),
            color = Color(0xFFFFB300),
            trackColor = SurfaceContainerLow
        )
        Text(
            text = count.toString(),
            modifier = Modifier.width(28.dp),
            color = OnSurface,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Composable
private fun ReviewFilterSection(
    selectedRating: Int?,
    enabled: Boolean,
    onRatingSelected: (Int?) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        RatingFilterChip(
            label = "Tất cả",
            selected = selectedRating == null,
            enabled = enabled,
            onClick = { onRatingSelected(null) }
        )
        for (rating in 5 downTo 1) {
            RatingFilterChip(
                label = "$rating sao",
                selected = selectedRating == rating,
                enabled = enabled,
                onClick = { onRatingSelected(rating) }
            )
        }
    }
}

@Composable
private fun RatingFilterChip(
    label: String,
    selected: Boolean,
    enabled: Boolean,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        enabled = enabled,
        shape = RoundedCornerShape(999.dp),
        color = if (selected) PrimaryBlue.copy(alpha = 0.08f) else Color.White,
        contentColor = if (selected) PrimaryBlue else OnSurface,
        border = androidx.compose.foundation.BorderStroke(
            width = 1.dp,
            color = if (selected) PrimaryBlue else SurfaceContainerLow
        ),
        shadowElevation = 0.dp,
        tonalElevation = 0.dp
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 18.dp, vertical = 10.dp),
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal
        )
    }
}

@Composable
private fun ReviewSortRow(
    selectedSort: String,
    enabled: Boolean,
    onSortSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val options = listOf(
        ReviewSortOption("NEWEST", "Mới nhất"),
        ReviewSortOption("RATING_HIGH", "Xếp hạng cao nhất"),
        ReviewSortOption("RATING_LOW", "Xếp hạng thấp nhất")
    )
    val selectedLabel = options.firstOrNull { it.value == selectedSort }?.label ?: "Mới nhất"

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box {
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .clickable(enabled = enabled) { expanded = true }
                    .padding(horizontal = 4.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Sắp xếp: $selectedLabel",
                    color = OnSurface,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
                Icon(
                    imageVector = Icons.Outlined.KeyboardArrowDown,
                    contentDescription = null,
                    tint = OnSurface,
                    modifier = Modifier.size(18.dp)
                )
            }
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                options.forEach { option ->
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = option.label,
                                color = if (option.value == selectedSort) PrimaryBlue else OnSurface,
                                fontWeight = if (option.value == selectedSort) FontWeight.SemiBold else FontWeight.Normal
                            )
                        },
                        onClick = {
                            expanded = false
                            onSortSelected(option.value)
                        }
                    )
                }
            }
        }
    }
}

private data class ReviewSortOption(
    val value: String,
    val label: String
)

@Composable
fun TravelPlaceReviewCard(
    review: TravelPlaceReviewResponse,
    onAuthorClick: () -> Unit,
    modifier: Modifier = Modifier,
    horizontalPadding: Dp = 20.dp
) {
    val displayName = review.user.name.ifBlank { review.user.username }
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = horizontalPadding),
        shape = RoundedCornerShape(18.dp),
        color = Color.White,
        shadowElevation = 0.dp,
        tonalElevation = 0.dp,
        border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceContainerLow.copy(alpha = 0.8f))
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    ReviewAuthorAvatar(
                        name = displayName,
                        avatarUrl = review.user.avatarUrl,
                        onClick = onAuthorClick
                    )
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Text(
                            text = displayName,
                            modifier = Modifier.clickable(onClick = onAuthorClick),
                            color = OnSurface,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.ExtraBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = formatReviewTimestamp(review.updatedAt ?: review.createdAt),
                            color = OnSurfaceVariant,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
                RatingStars(rating = review.rating.toDouble(), starSize = 18)
            }

            Text(
                text = review.content,
                color = OnSurface,
                style = MaterialTheme.typography.bodyMedium,
                lineHeight = 21.sp
            )
        }
    }
}

@Composable
fun TravelPlaceReviewCardSkeleton(
    modifier: Modifier = Modifier,
    horizontalPadding: Dp = 20.dp
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = horizontalPadding),
        shape = RoundedCornerShape(18.dp),
        color = Color.White,
        shadowElevation = 0.dp,
        tonalElevation = 0.dp,
        border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceContainerLow.copy(alpha = 0.8f))
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    SkeletonBlock(
                        modifier = Modifier.size(46.dp),
                        shape = CircleShape
                    )
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        SkeletonBlock(
                            modifier = Modifier.fillMaxWidth(0.62f).height(14.dp)
                        )
                        SkeletonBlock(
                            modifier = Modifier.fillMaxWidth(0.42f).height(10.dp)
                        )
                    }
                }
                SkeletonBlock(
                    modifier = Modifier.width(72.dp).height(18.dp)
                )
            }

            SkeletonBlock(
                modifier = Modifier.fillMaxWidth(0.92f).height(12.dp)
            )
            SkeletonBlock(
                modifier = Modifier.fillMaxWidth(0.76f).height(12.dp)
            )
        }
    }
}

@Composable
private fun ReviewListSkeleton(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        ReviewSummarySkeleton()
        ReviewFilterSkeleton()
        ReviewSortSkeleton()
        repeat(5) {
            TravelPlaceReviewCardSkeleton(
                modifier = Modifier.padding(horizontal = 0.dp)
            )
        }
    }
}

@Composable
private fun ReviewSummarySkeleton() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(24.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier.width(116.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            SkeletonBlock(
                modifier = Modifier.width(72.dp).height(42.dp)
            )
            Spacer(modifier = Modifier.height(10.dp))
            SkeletonBlock(
                modifier = Modifier.width(88.dp).height(20.dp)
            )
            Spacer(modifier = Modifier.height(6.dp))
            SkeletonBlock(
                modifier = Modifier.width(84.dp).height(14.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            SkeletonBlock(
                modifier = Modifier.width(78.dp).height(14.dp)
            )
        }

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            repeat(5) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    SkeletonBlock(
                        modifier = Modifier.width(46.dp).height(12.dp)
                    )
                    SkeletonBlock(
                        modifier = Modifier.weight(1f).height(8.dp)
                    )
                    SkeletonBlock(
                        modifier = Modifier.width(24.dp).height(12.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun ReviewFilterSkeleton() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        repeat(5) {
            SkeletonBlock(
                modifier = Modifier
                    .width(
                        when (it) {
                            0 -> 72.dp
                            1 -> 88.dp
                            2 -> 78.dp
                            3 -> 84.dp
                            else -> 74.dp
                        }
                    )
                    .height(34.dp),
                shape = RoundedCornerShape(999.dp)
            )
        }
    }
}

@Composable
private fun ReviewSortSkeleton() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.End
    ) {
        SkeletonBlock(
            modifier = Modifier.width(160.dp).height(20.dp)
        )
    }
}

@Composable
private fun ReviewAuthorAvatar(
    name: String,
    avatarUrl: String?,
    onClick: () -> Unit
) {
    val initial = name.trim().firstOrNull()?.uppercaseChar()?.toString() ?: "?"
    val avatarColors = listOf(
        Color(0xFF9DBBFF),
        Color(0xFF86DDB8),
        Color(0xFFA7A0F6),
        Color(0xFFF0B76D),
        Color(0xFF7FC6E8)
    )
    val backgroundColor = avatarColors[initial.first().code % avatarColors.size]

    Box(
        modifier = Modifier
            .size(46.dp)
            .clip(CircleShape)
            .background(backgroundColor)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        if (!avatarUrl.isNullOrBlank()) {
            AsyncImage(
                model = avatarUrl,
                contentDescription = name,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        } else {
            Text(
                text = initial,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
    }
}

@Composable
private fun RatingStars(
    rating: Double,
    starSize: Int
) {
    val roundedRating = rating.roundToInt().coerceIn(0, 5)
    Row(horizontalArrangement = Arrangement.spacedBy(1.dp)) {
        repeat(5) { index ->
            Icon(
                imageVector = Icons.Filled.Star,
                contentDescription = null,
                tint = if (index < roundedRating) Color(0xFFFFB300) else Color(0xFFD6D9DF),
                modifier = Modifier.size(starSize.dp)
            )
        }
    }
}

private fun ReviewListUiState.ratingCount(rating: Int): Long {
    val counts = summary?.ratingCounts ?: return 0L
    return counts[rating.toString()] ?: 0L
}

private fun ratingLabel(averageRating: Double, reviewCount: Long): String {
    if (reviewCount <= 0L) {
        return "Chưa có đánh giá"
    }
    return when {
        averageRating >= 4.5 -> "Tuyệt vời"
        averageRating >= 4.0 -> "Rất tốt"
        averageRating >= 3.0 -> "Tốt"
        averageRating >= 2.0 -> "Trung bình"
        else -> "Cần cải thiện"
    }
}

private fun formatReviewTimestamp(raw: String?): String {
    if (raw.isNullOrBlank()) {
        return "Không rõ thời gian"
    }
    val formatted = runCatching {
        DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")
            .withZone(ZoneId.systemDefault())
            .format(Instant.parse(raw))
    }.getOrDefault(raw)
    return if (formatted.contains("/") && formatted.contains(" ")) {
        formatted.replaceFirst(" ", " • ")
    } else {
        formatted
    }
}
