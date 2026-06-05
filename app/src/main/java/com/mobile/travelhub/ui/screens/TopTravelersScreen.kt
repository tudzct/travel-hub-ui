package com.mobile.travelhub.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import coil.compose.AsyncImage
import com.mobile.travelhub.data.model.TopTravelerPeriod
import com.mobile.travelhub.data.model.TopTravelerResponse
import com.mobile.travelhub.ui.theme.OnSurface
import com.mobile.travelhub.ui.theme.OnSurfaceVariant
import com.mobile.travelhub.ui.theme.PrimaryBlue
import com.mobile.travelhub.ui.theme.SurfaceBg
import com.mobile.travelhub.ui.theme.SurfaceContainerLow
import com.mobile.travelhub.viewmodels.TopTravelersViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopTravelersScreen(
    initialPeriod: TopTravelerPeriod,
    onBack: () -> Unit,
    onTravelerClick: (Long, Boolean) -> Unit,
    viewModel: TopTravelersViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()

    LaunchedEffect(initialPeriod) {
        viewModel.loadList(initialPeriod)
    }
    BackHandler(onBack = onBack)

    Scaffold(
        containerColor = SurfaceBg,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Top Travelers",
                        fontWeight = FontWeight.Bold,
                        color = OnSurface
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = OnSurface
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = SurfaceBg)
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            PeriodSelector(
                selectedPeriod = state.period,
                onPeriodSelected = viewModel::loadList,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)
            )

            when {
                state.isLoading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = PrimaryBlue)
                    }
                }

                state.errorMessage != null && state.items.isEmpty() -> {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "Không thể tải danh sách người dùng nổi bật.",
                            color = OnSurfaceVariant
                        )
                        TextButton(onClick = viewModel::refresh) {
                            Text("Thử lại", color = PrimaryBlue)
                        }
                    }
                }

                state.items.isEmpty() -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            text = "Chưa có người dùng nổi bật trong giai đoạn này.",
                            color = OnSurfaceVariant
                        )
                    }
                }

                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (state.actionErrorMessage != null) {
                            item {
                                Text(
                                    text = state.actionErrorMessage.orEmpty(),
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                                    color = MaterialTheme.colorScheme.error,
                                    fontSize = 12.sp
                                )
                            }
                        }
                        itemsIndexed(state.items, key = { _, traveler -> traveler.id }) { index, traveler ->
                            RankedTravelerRow(
                                rank = index + 1,
                                traveler = traveler,
                                followRequestInProgress = traveler.id in state.requestingFollowIds,
                                onClick = { onTravelerClick(traveler.id, traveler.currentUser) },
                                onToggleFollow = { viewModel.toggleFollow(traveler) }
                            )
                        }
                        if (state.page + 1 < state.totalPages) {
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 10.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (state.isLoadingMore) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(24.dp),
                                            strokeWidth = 2.dp,
                                            color = PrimaryBlue
                                        )
                                    } else {
                                        TextButton(onClick = viewModel::loadMore) {
                                            Text("Load more", color = PrimaryBlue)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PeriodSelector(
    selectedPeriod: TopTravelerPeriod,
    onPeriodSelected: (TopTravelerPeriod) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(modifier = modifier, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        TopTravelerPeriod.entries.forEach { period ->
            FilterChip(
                selected = period == selectedPeriod,
                onClick = { if (period != selectedPeriod) onPeriodSelected(period) },
                label = { Text(if (period == TopTravelerPeriod.WEEK) "Week" else "Month") }
            )
        }
    }
}

@Composable
private fun RankedTravelerRow(
    rank: Int,
    traveler: TopTravelerResponse,
    followRequestInProgress: Boolean,
    onClick: () -> Unit,
    onToggleFollow: () -> Unit
) {
    val displayName = traveler.name?.takeIf { it.isNotBlank() } ?: traveler.username
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clickable(onClick = onClick),
        color = SurfaceContainerLow,
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "#$rank",
                modifier = Modifier.width(38.dp),
                fontWeight = FontWeight.Bold,
                color = PrimaryBlue
            )
            AsyncImage(
                model = traveler.avatarUrl,
                contentDescription = displayName,
                modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentScale = ContentScale.Crop
            )
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 12.dp)
            ) {
                Text(
                    text = if (traveler.currentUser) "$displayName (You)" else displayName,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    fontWeight = FontWeight.SemiBold,
                    color = OnSurface
                )
                Text(
                    text = "${traveler.score} points",
                    fontSize = 12.sp,
                    color = OnSurfaceVariant
                )
            }
            if (!traveler.currentUser) {
                Button(
                    onClick = onToggleFollow,
                    enabled = !followRequestInProgress,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (traveler.following) PrimaryBlue else MaterialTheme.colorScheme.surface,
                        contentColor = if (traveler.following) MaterialTheme.colorScheme.onPrimary else PrimaryBlue
                    ),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Text(
                        text = if (traveler.following) "Following" else "Follow",
                        fontSize = 12.sp
                    )
                }
            }
        }
    }
}
