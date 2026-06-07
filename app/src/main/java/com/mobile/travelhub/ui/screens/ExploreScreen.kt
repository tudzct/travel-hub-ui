package com.mobile.travelhub.ui.screens

import androidx.compose.ui.res.stringResource
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.mobile.travelhub.data.model.TopTravelerPeriod
import com.mobile.travelhub.data.model.TopTravelerResponse
import com.mobile.travelhub.data.model.TravelPlaceListItemResponse
import com.mobile.travelhub.ui.components.FeaturedLocationCard
import com.mobile.travelhub.ui.components.PillFilterChip
import com.mobile.travelhub.ui.components.RetryButton
import com.mobile.travelhub.ui.components.SearchBar
import com.mobile.travelhub.ui.components.UserResultCard
import com.mobile.travelhub.ui.components.UserResultCardSkeleton
import com.mobile.travelhub.ui.components.modifiers.shimmerEffect
import com.mobile.travelhub.ui.theme.OnSurface
import com.mobile.travelhub.ui.theme.OnSurfaceVariant
import com.mobile.travelhub.ui.theme.PrimaryBlue
import com.mobile.travelhub.ui.theme.SurfaceBg
import com.mobile.travelhub.viewmodels.ExploreViewModel
import com.mobile.travelhub.viewmodels.TopTravelersUiState
import com.mobile.travelhub.viewmodels.TopTravelersViewModel
import com.mobile.travelhub.R

@Composable
fun ExploreScreen(
    activateSearch: Boolean = false,
    refreshTopTravelersKey: Int = 0,
    onSearchActiveChange: (Boolean) -> Unit = {},
    onSearchUserClick: (Long) -> Unit = {},
    onAssistantClick: () -> Unit = {},
    onPlaceClick: (TravelPlaceListItemResponse) -> Unit = {},
    onTravelerClick: (Long, Boolean) -> Unit = { _, _ -> },
    onSeeAllTopTravelers: (TopTravelerPeriod) -> Unit = {},
    topTravelersViewModel: TopTravelersViewModel = hiltViewModel(),
    exploreViewModel: ExploreViewModel = hiltViewModel()
) {
    val topTravelersState by topTravelersViewModel.uiState.collectAsState()
    val uiState by exploreViewModel.uiState.collectAsState()
    var isSearchExpanded by rememberSaveable { mutableStateOf(activateSearch) }

    LaunchedEffect(refreshTopTravelersKey) {
        topTravelersViewModel.loadPreview()
    }

    LaunchedEffect(activateSearch) {
        if (activateSearch) {
            isSearchExpanded = true
        }
    }

    LaunchedEffect(isSearchExpanded) {
        onSearchActiveChange(isSearchExpanded)
    }

    DisposableEffect(Unit) {
        onDispose { onSearchActiveChange(false) }
    }

    BackHandler(enabled = isSearchExpanded) {
        isSearchExpanded = false
    }

    AnimatedContent(
        targetState = isSearchExpanded,
        transitionSpec = {
            (
                fadeIn(tween(220)) + scaleIn(
                    initialScale = 0.96f,
                    animationSpec = tween(220)
                )
            ).togetherWith(
                fadeOut(tween(150)) + scaleOut(
                    targetScale = 0.98f,
                    animationSpec = tween(150)
                )
            )
        },
        label = "explore-search-expand"
    ) { searchExpanded ->
        if (searchExpanded) {
            SearchPage(
                onBack = { isSearchExpanded = false },
                onUserClick = onSearchUserClick,
                onPlaceClick = onPlaceClick
            )
        } else {
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
                        text = stringResource(R.string.ui_b965ae66fc),
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
                                text = stringResource(R.string.ui_62894af0b2),
                                modifier = Modifier.padding(start = 6.dp),
                                color = OnSurface,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }
                    }
                }

                SearchField(onClick = { isSearchExpanded = true })

//        if (uiState.recentSearches.isNotEmpty()) {
//            SectionLabel(text = stringResource(R.string.ui_5820a93677), topPadding = 18.dp)
//            HorizontalChipRow(
//                items = uiState.recentSearches,
//                leadingIcon = true
//            )
//
//            SectionDivider()
//        }

                SectionTitle(text = stringResource(R.string.ui_758588c39a), topPadding = 24.dp)
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
                    text = stringResource(R.string.ui_625c9f2021),
                    color = OnSurfaceVariant,
                    fontSize = 13.sp
                )
                RetryButton(onClick = onRetry)
            }
        }

        locations.isEmpty() -> {
            Text(
                text = stringResource(R.string.ui_6e8c44726f),
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
    SearchBar(
        value = "",
        placeholder = stringResource(R.string.ui_e87eee7e22),
        onClick = onClick,
        modifier = Modifier
            .padding(horizontal = 16.dp, vertical = 14.dp)
    )
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
        title = stringResource(R.string.ui_8e40b4b1c6),
        action = "Xem tất cả",
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
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(4, contentType = { "top-traveler-skeleton" }) {
                    UserResultCardSkeleton()
                }
            }
        }

        state.errorMessage != null -> {
            Column(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = stringResource(R.string.ui_6725a49830),
                    color = OnSurfaceVariant,
                    fontSize = 13.sp
                )
                RetryButton(onClick = onRetry)
            }
        }

        state.items.isEmpty() -> {
            Text(
                text = stringResource(R.string.ui_2ab957043f),
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp),
                color = OnSurfaceVariant,
                fontSize = 13.sp
            )
        }

        else -> {
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(
                    items = state.items,
                    key = { it.id },
                    contentType = { "top-traveler" }
                ) { traveler ->
                    UserResultCard(
                        name = traveler.name.orEmpty(),
                        username = traveler.username,
                        avatarUrl = traveler.avatarUrl,
                        followersCount = traveler.followersCount,
                        isFollowing = traveler.following,
                        isFollowLoading = traveler.id in state.requestingFollowIds,
                        isCurrentUser = traveler.currentUser,
                        onClick = { onTravelerClick(traveler.id, traveler.currentUser) },
                        onFollowClick = { onToggleFollow(traveler) }
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
            PillFilterChip(
                selected = selectedPeriod == period,
                onClick = { if (selectedPeriod != period) onPeriodSelected(period) },
                label = stringResource(
                    if (period == TopTravelerPeriod.WEEK) {
                        R.string.top_travelers_week
                    } else {
                        R.string.top_travelers_month
                    }
                )
            )
        }
    }
}
