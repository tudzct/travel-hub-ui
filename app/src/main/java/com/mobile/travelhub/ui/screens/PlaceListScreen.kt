package com.mobile.travelhub.ui.screens

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.MoreHoriz
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import coil.compose.AsyncImage
import com.mobile.travelhub.data.model.TravelPlaceListItemResponse
import com.mobile.travelhub.viewmodels.PlaceListViewModel

@Composable
fun PlaceListScreen(
    onPlaceClick: (Long) -> Unit,
    onCreatePlace: () -> Unit,
    placeListViewModel: PlaceListViewModel = hiltViewModel()
) {
    val uiState by placeListViewModel.uiState.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(VerdantSurface)
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(top = 18.dp, bottom = 112.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                FeedHeader(
                    keyword = uiState.keyword,
                    onKeywordChange = placeListViewModel::onKeywordChange,
                    resultCount = uiState.items.size,
                    isAdmin = uiState.isAdmin,
                    onCreatePlace = onCreatePlace
                )
            }

            when {
                uiState.isLoading && uiState.items.isEmpty() -> {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 40.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = VerdantPrimary)
                        }
                    }
                }

                uiState.errorMessage != null && uiState.items.isEmpty() -> {
                    item {
                        FeedEmptyState(
                            title = "Không thể tải địa điểm",
                            message = uiState.errorMessage.orEmpty(),
                            fullScreen = false
                        )
                    }
                }

                uiState.items.isEmpty() -> {
                    item {
                        FeedEmptyState(
                            title = "Không có địa điểm phù hợp",
                            message = "Thử từ khóa khác để hiện lại danh sách địa điểm.",
                            fullScreen = false
                        )
                    }
                }

                else -> {
                    item {
                        LocationsRail(
                            places = uiState.items.take(10),
                            onPlaceClick = { onPlaceClick(it.id) }
                        )
                    }

                    items(count = maxOf(uiState.items.size, 3)) { index ->
                        FeedPostPlaceholderCard(index = index + 1)
                    }
                }
            }
        }
    }
}

@Composable
private fun FeedHeader(
    keyword: String,
    onKeywordChange: (String) -> Unit,
    resultCount: Int,
    isAdmin: Boolean,
    onCreatePlace: () -> Unit
) {
    Column(
        modifier = Modifier.padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "TRAVEL HUB",
                    style = MaterialTheme.typography.labelMedium,
                    color = VerdantPrimary,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "Discovery Feed",
                    style = MaterialTheme.typography.headlineMedium,
                    color = VerdantOnSurface,
                    fontWeight = FontWeight.Bold
                )
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (isAdmin) {
                    IconButton(onClick = onCreatePlace) {
                        Surface(
                            shape = CircleShape,
                            color = VerdantPrimary,
                            modifier = Modifier.size(40.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Outlined.Add,
                                    contentDescription = "Add place",
                                    tint = Color.White
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Surface(
                    shape = CircleShape,
                    color = VerdantSurfaceContainerHighest,
                    modifier = Modifier.size(40.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = "T",
                            style = MaterialTheme.typography.titleMedium,
                            color = VerdantPrimary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        TextField(
            value = keyword,
            onValueChange = onKeywordChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = {
                Text(
                    text = "Search destinations or provinces",
                    color = VerdantOnSurfaceVariant
                )
            },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Outlined.Search,
                    contentDescription = null,
                    tint = VerdantOnSurfaceVariant
                )
            },
            shape = RoundedCornerShape(22.dp),
            singleLine = true,
            colors = TextFieldDefaults.colors(
                focusedContainerColor = VerdantSurfaceContainerHighest,
                unfocusedContainerColor = VerdantSurfaceContainerHighest,
                disabledContainerColor = VerdantSurfaceContainerHighest,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                disabledIndicatorColor = Color.Transparent,
                focusedTextColor = VerdantOnSurface,
                unfocusedTextColor = VerdantOnSurface,
                cursorColor = VerdantPrimary
            )
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (resultCount == 1) "1 location in feed" else "$resultCount locations in feed",
                style = MaterialTheme.typography.bodyMedium,
                color = VerdantOnSurfaceVariant
            )
            if (isAdmin) {
                StatusPill(
                    text = "Admin",
                    containerColor = VerdantPrimary.copy(alpha = 0.12f),
                    contentColor = VerdantPrimary
                )
            }
        }
    }
}

@Composable
private fun LocationsRail(
    places: List<TravelPlaceListItemResponse>,
    onPlaceClick: (TravelPlaceListItemResponse) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Locations",
                style = MaterialTheme.typography.titleMedium,
                color = VerdantOnSurface,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Swipe",
                style = MaterialTheme.typography.labelMedium,
                color = VerdantPrimary
            )
        }

        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(places, key = { it.id }) { place ->
                LocationCard(
                    place = place,
                    onClick = { onPlaceClick(place) }
                )
            }
        }
    }
}

@Composable
private fun LocationCard(
    place: TravelPlaceListItemResponse,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .width(104.dp)
            .height(172.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(24.dp),
        color = VerdantSurfaceContainerLowest,
        shadowElevation = 8.dp
    ) {
        Box {
            AsyncImage(
                model = place.mainImage,
                contentDescription = place.name,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                VerdantPrimary.copy(alpha = 0.08f),
                                Color.Transparent,
                                VerdantOnSurface.copy(alpha = 0.76f)
                            )
                        )
                    )
            )
            Surface(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(8.dp),
                shape = CircleShape,
                color = VerdantSurfaceContainerLowest.copy(alpha = 0.94f)
            ) {
                Box(
                    modifier = Modifier.size(30.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = place.province.name.take(1).uppercase(),
                        style = MaterialTheme.typography.labelLarge,
                        color = VerdantPrimary,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(10.dp),
                verticalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                Text(
                    text = place.name,
                    style = MaterialTheme.typography.labelLarge,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = place.province.name,
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White.copy(alpha = 0.82f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun FeedPostPlaceholderCard(
    index: Int
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp),
        shape = RoundedCornerShape(26.dp),
        color = VerdantSurfaceContainerLowest,
        shadowElevation = 10.dp
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = CircleShape,
                        color = VerdantSurfaceContainerHighest,
                        modifier = Modifier.size(42.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = index.toString(),
                                style = MaterialTheme.typography.titleMedium,
                                color = VerdantPrimary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(
                            text = "Post card placeholder",
                            style = MaterialTheme.typography.titleMedium,
                            color = VerdantOnSurface,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Reserved for the posts module",
                            style = MaterialTheme.typography.bodySmall,
                            color = VerdantOnSurfaceVariant
                        )
                    }
                }
                Icon(
                    imageVector = Icons.Outlined.MoreHoriz,
                    contentDescription = null,
                    tint = VerdantOnSurfaceVariant
                )
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp)
                    .padding(horizontal = 16.dp)
                    .clip(RoundedCornerShape(22.dp))
                    .background(VerdantSurfaceContainer)
            ) {
                Column(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Post content will be attached here",
                        style = MaterialTheme.typography.titleMedium,
                        color = VerdantOnSurface,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "Locations above are ready for place detail and review flow.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = VerdantOnSurfaceVariant
                    )
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                StatusPill(
                    text = "Post shell",
                    containerColor = VerdantSurfaceContainer,
                    contentColor = VerdantOnSurfaceVariant
                )
                StatusPill(
                    text = "Ready for integration",
                    containerColor = VerdantPrimary.copy(alpha = 0.12f),
                    contentColor = VerdantPrimary
                )
            }
        }
    }
}

@Composable
private fun StatusPill(
    text: String,
    containerColor: Color,
    contentColor: Color
) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = containerColor
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
            style = MaterialTheme.typography.labelMedium,
            color = contentColor
        )
    }
}

@Composable
private fun FeedEmptyState(
    title: String,
    message: String,
    fullScreen: Boolean
) {
    val containerModifier = if (fullScreen) {
        Modifier.fillMaxSize()
    } else {
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    }

    Box(
        modifier = containerModifier,
        contentAlignment = Alignment.Center
    ) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = VerdantSurfaceContainer,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineSmall,
                    color = VerdantOnSurface,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyLarge,
                    color = VerdantOnSurfaceVariant
                )
            }
        }
    }
}

private val VerdantPrimary = Color(0xFF006B2C)
private val VerdantSurface = Color(0xFFF4FCF0)
private val VerdantSurfaceContainer = Color(0xFFEFF6EA)
private val VerdantSurfaceContainerHighest = Color(0xFFDDE5D9)
private val VerdantSurfaceContainerLowest = Color(0xFFFFFFFF)
private val VerdantOnSurface = Color(0xFF171D16)
private val VerdantOnSurfaceVariant = Color(0xFF3E4A3D)
