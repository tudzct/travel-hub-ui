package com.mobile.travelhub.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
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
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import coil.compose.AsyncImage
import com.mobile.travelhub.data.model.TopTravelerPeriod
import com.mobile.travelhub.data.model.TopTravelerResponse
import com.mobile.travelhub.data.model.TravelPlaceListItemResponse
import com.mobile.travelhub.ui.components.FeaturedLocationCard
import com.mobile.travelhub.ui.components.modifiers.shimmerEffect
import com.mobile.travelhub.ui.theme.OnSurface
import com.mobile.travelhub.ui.theme.OnSurfaceVariant
import com.mobile.travelhub.ui.theme.PrimaryBlue
import com.mobile.travelhub.ui.theme.SurfaceBg
import com.mobile.travelhub.viewmodels.ExploreViewModel
import com.mobile.travelhub.viewmodels.TopTravelersUiState
import com.mobile.travelhub.viewmodels.TopTravelersViewModel

@Composable
fun ExploreScreen(
    activateSearch: Boolean = false,
    refreshTopTravelersKey: Int = 0,
    onSearchClick: () -> Unit = {},
    onAssistantClick: () -> Unit = {},
    onPlaceClick: (TravelPlaceListItemResponse) -> Unit = {},
    onTravelerClick: (Long, Boolean) -> Unit = { _, _ -> },
    onSeeAllTopTravelers: (TopTravelerPeriod) -> Unit = {},
    topTravelersViewModel: TopTravelersViewModel = hiltViewModel(),
    exploreViewModel: ExploreViewModel = hiltViewModel()
) {
    val topTravelersState by topTravelersViewModel.uiState.collectAsState()
    val uiState by exploreViewModel.uiState.collectAsState()

    LaunchedEffect(refreshTopTravelersKey) {
        topTravelersViewModel.loadPreview()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SurfaceBg)
            .verticalScroll(rememberScrollState())
            .padding(top = 10.dp, bottom = 24.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Explore",
                color = OnSurface,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.ExtraBold
            )
            Surface(
                color = Color(0xFFE7F4FC),
                shape = RoundedCornerShape(18.dp),
                modifier = Modifier.clickable(onClick = onAssistantClick)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = PrimaryBlue,
                        modifier = Modifier.size(17.dp)
                    )
                    Text(
                        text = "Trợ lý AI",
                        modifier = Modifier.padding(start = 6.dp),
                        color = OnSurface,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }
            }
        }

        LaunchedEffect(activateSearch) {
            if (activateSearch) {
                onSearchClick()
            }
        }

        SearchField(onClick = onSearchClick)

        if (uiState.recentSearches.isNotEmpty()) {
            SectionLabel(text = "Recent Searches", topPadding = 18.dp)
            HorizontalChipRow(
                items = uiState.recentSearches,
                leadingIcon = true
            )

            SectionDivider()
        }

        SectionTitle(text = "Trending Now")
        HorizontalChipRow(
            items = listOf("#BeachVibes", "#MountainClimbing", "#CityBreaks", "#FoodTour"),
            leadingIcon = false,
            filled = true
        )

        SectionTitle(text = "Featured Locations", topPadding = 24.dp)
        FeaturedLocationsSection(
            locations = uiState.featuredLocations,
            isLoading = uiState.isLoadingFeaturedLocations,
            errorMessage = uiState.featuredLocationsError,
            onRetry = exploreViewModel::loadFeaturedLocations,
            onPlaceClick = onPlaceClick
        )

        TopTravelersPreview(
            state = topTravelersState,
            onPeriodSelected = topTravelersViewModel::loadPreview,
            onRetry = topTravelersViewModel::refresh,
            onTravelerClick = onTravelerClick,
            onToggleFollow = topTravelersViewModel::toggleFollow,
            onSeeAll = { onSeeAllTopTravelers(topTravelersState.period) }
        )
    }
}

@Composable
private fun FeaturedLocationsSection(
    locations: List<TravelPlaceListItemResponse>,
    isLoading: Boolean,
    errorMessage: String?,
    onRetry: () -> Unit,
    onPlaceClick: (TravelPlaceListItemResponse) -> Unit
) {
    when {
        isLoading -> FeaturedLocationsSkeleton()

        errorMessage != null -> {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Không thể tải địa điểm nổi bật.",
                    color = OnSurfaceVariant,
                    fontSize = 13.sp
                )
                TextButton(onClick = onRetry) {
                    Text("Thử lại", color = PrimaryBlue)
                }
            }
        }

        locations.isEmpty() -> {
            Text(
                text = "Chưa có địa điểm nổi bật.",
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                color = OnSurfaceVariant,
                fontSize = 13.sp
            )
        }

        else -> {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                locations.forEach { location ->
                    FeaturedLocationCard(
                        country = location.province.name,
                        city = location.name,
                        imageUrl = location.mainImage,
                        averageRating = location.averageRating,
                        reviewCount = location.reviewCount,
                        modifier = Modifier
                            .width(220.dp)
                            .height(270.dp),
                        onClick = { onPlaceClick(location) }
                    )
                }
            }
        }
    }
}

@Composable
private fun FeaturedLocationsSkeleton() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        repeat(3) {
            Box(
                modifier = Modifier
                    .width(220.dp)
                    .height(270.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .shimmerEffect()
            )
        }
    }
}

@Composable
private fun SearchField(
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .padding(horizontal = 16.dp, vertical = 14.dp)
            .fillMaxWidth()
            .height(42.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFFEFF2FA))
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Outlined.Search,
            contentDescription = null,
            tint = OnSurfaceVariant,
            modifier = Modifier.size(19.dp)
        )
        Text(
            text = "Search destinations, people, or hashtags",
            modifier = Modifier
                .weight(1f)
                .padding(start = 10.dp),
            color = OnSurfaceVariant,
            fontSize = 14.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun SectionLabel(text: String, topPadding: androidx.compose.ui.unit.Dp = 0.dp) {
    Text(
        text = text,
        modifier = Modifier.padding(start = 16.dp, top = topPadding, bottom = 10.dp),
        color = OnSurfaceVariant,
        style = MaterialTheme.typography.labelLarge,
        fontWeight = FontWeight.Bold
    )
}

@Composable
private fun SectionTitle(text: String, topPadding: androidx.compose.ui.unit.Dp = 20.dp) {
    Text(
        text = text,
        modifier = Modifier.padding(start = 16.dp, top = topPadding, bottom = 10.dp),
        color = OnSurface,
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.ExtraBold
    )
}

@Composable
private fun SectionTitleRow(
    title: String,
    action: String,
    topPadding: androidx.compose.ui.unit.Dp,
    onActionClick: () -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 16.dp, top = topPadding, bottom = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            color = OnSurface,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.ExtraBold
        )
        TextButton(onClick = onActionClick) {
            Text(
                text = action,
                color = PrimaryBlue,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun HorizontalChipRow(
    items: List<String>,
    leadingIcon: Boolean,
    filled: Boolean = false
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items.forEach { item ->
            ExploreChip(
                text = item,
                leadingIcon = leadingIcon,
                filled = filled
            )
        }
    }
}

@Composable
private fun ExploreChip(text: String, leadingIcon: Boolean, filled: Boolean) {
    val background = if (filled) Color(0xFFE1E3EA) else Color(0xFFF3F6FC)
    val borderColor = if (filled) Color.Transparent else Color(0xFFD6DAE6)

    Row(
        modifier = Modifier
            .height(30.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(background)
            .border(1.dp, borderColor, RoundedCornerShape(16.dp))
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (leadingIcon) {
            Icon(
                imageVector = Icons.Outlined.CalendarMonth,
                contentDescription = null,
                tint = OnSurfaceVariant,
                modifier = Modifier.size(14.dp)
            )
            Text(
                text = text,
                modifier = Modifier.padding(start = 6.dp),
                color = OnSurfaceVariant,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold
            )
        } else {
            Text(
                text = text,
                color = OnSurfaceVariant,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun SectionDivider() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 18.dp)
            .height(1.dp)
            .background(Color(0xFFE4E7EF))
    )
}

@Composable
private fun TopTravelersPreview(
    state: TopTravelersUiState,
    onPeriodSelected: (TopTravelerPeriod) -> Unit,
    onRetry: () -> Unit,
    onTravelerClick: (Long, Boolean) -> Unit,
    onToggleFollow: (TopTravelerResponse) -> Unit,
    onSeeAll: () -> Unit
) {
    SectionTitleRow(
        title = "Top Travelers",
        action = "See All",
        topPadding = 36.dp,
        onActionClick = onSeeAll
    )
    TopTravelerPeriodSelector(
        selectedPeriod = state.period,
        onPeriodSelected = onPeriodSelected,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
    )
    when {
        state.isLoading -> {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 18.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(28.dp),
                    color = PrimaryBlue,
                    strokeWidth = 2.dp
                )
            }
        }

        state.errorMessage != null -> {
            Column(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Không thể tải danh sách người dùng nổi bật.",
                    color = OnSurfaceVariant,
                    fontSize = 13.sp
                )
                TextButton(onClick = onRetry) {
                    Text("Thử lại", color = PrimaryBlue)
                }
            }
        }

        state.items.isEmpty() -> {
            Text(
                text = "Chưa có người dùng nổi bật trong giai đoạn này.",
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp),
                color = OnSurfaceVariant,
                fontSize = 13.sp
            )
        }

        else -> {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                horizontalArrangement = Arrangement.spacedBy(26.dp)
            ) {
                state.items.forEach { traveler ->
                    TravelerItem(
                        traveler = traveler,
                        followRequestInProgress = traveler.id in state.requestingFollowIds,
                        onClick = { onTravelerClick(traveler.id, traveler.currentUser) },
                        onToggleFollow = { onToggleFollow(traveler) }
                    )
                }
            }
        }
    }
}

@Composable
private fun TopTravelerPeriodSelector(
    selectedPeriod: TopTravelerPeriod,
    onPeriodSelected: (TopTravelerPeriod) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        TopTravelerPeriod.entries.forEach { period ->
            FilterChip(
                selected = selectedPeriod == period,
                onClick = { if (selectedPeriod != period) onPeriodSelected(period) },
                label = {
                    Text(if (period == TopTravelerPeriod.WEEK) "Week" else "Month")
                }
            )
        }
    }
}

@Composable
private fun TravelerItem(
    traveler: TopTravelerResponse,
    followRequestInProgress: Boolean,
    onClick: () -> Unit,
    onToggleFollow: () -> Unit
) {
    val displayName = traveler.name?.takeIf { it.isNotBlank() } ?: traveler.username
    Column(
        modifier = Modifier
            .width(72.dp)
            .clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        AsyncImage(
            model = traveler.avatarUrl,
            contentDescription = displayName,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(54.dp)
                .clip(CircleShape)
                .border(
                    width = if (traveler.following) 2.dp else 0.dp,
                    color = if (traveler.following) PrimaryBlue else Color.Transparent,
                    shape = CircleShape
                )
        )
        Text(
            text = if (traveler.currentUser) "You" else displayName,
            modifier = Modifier.padding(top = 7.dp),
            color = OnSurface,
            fontSize = 11.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        if (!traveler.currentUser) {
            Button(
                onClick = onToggleFollow,
                enabled = !followRequestInProgress,
                modifier = Modifier
                    .padding(top = 6.dp)
                    .height(24.dp),
                shape = RoundedCornerShape(14.dp),
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (traveler.following) PrimaryBlue else Color(0xFFF3F5FA),
                    contentColor = if (traveler.following) Color.White else OnSurfaceVariant
                )
            ) {
                Text(
                    text = if (traveler.following) "Following" else "Follow",
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
