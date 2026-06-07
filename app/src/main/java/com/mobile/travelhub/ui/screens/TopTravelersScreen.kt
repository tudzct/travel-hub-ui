package com.mobile.travelhub.ui.screens

import androidx.compose.ui.res.stringResource
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.mobile.travelhub.data.model.TopTravelerPeriod
import com.mobile.travelhub.ui.components.UserResultCard
import com.mobile.travelhub.ui.components.UserResultCardSkeleton
import com.mobile.travelhub.ui.components.RetryButton
import com.mobile.travelhub.ui.components.PillFilterChip
import com.mobile.travelhub.ui.theme.OnSurface
import com.mobile.travelhub.ui.theme.OnSurfaceVariant
import com.mobile.travelhub.ui.theme.PrimaryBlue
import com.mobile.travelhub.ui.theme.SurfaceBg
import com.mobile.travelhub.viewmodels.TopTravelersViewModel
import com.mobile.travelhub.R

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
                        text = stringResource(R.string.ui_8e40b4b1c6),
                        fontWeight = FontWeight.Bold,
                        color = OnSurface
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.ui_b52b36b726),
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
                    TopTravelerGridSkeleton()
                }

                state.errorMessage != null && state.items.isEmpty() -> {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = stringResource(R.string.ui_6725a49830),
                            color = OnSurfaceVariant
                        )
                        RetryButton(onClick = viewModel::refresh)
                    }
                }

                state.items.isEmpty() -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            text = stringResource(R.string.ui_2ab957043f),
                            color = OnSurfaceVariant
                        )
                    }
                }

                else -> {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        if (state.actionErrorMessage != null) {
                            item(span = { GridItemSpan(maxLineSpan) }) {
                                Text(
                                    text = state.actionErrorMessage.orEmpty(),
                                    modifier = Modifier.padding(vertical = 4.dp),
                                    color = MaterialTheme.colorScheme.error,
                                    fontSize = 12.sp
                                )
                            }
                        }
                        items(state.items, key = { traveler -> traveler.id }) { traveler ->
                            UserResultCard(
                                name = traveler.name.orEmpty(),
                                username = traveler.username,
                                avatarUrl = traveler.avatarUrl,
                                followersCount = traveler.followersCount,
                                isFollowing = traveler.following,
                                isFollowLoading = traveler.id in state.requestingFollowIds,
                                isCurrentUser = traveler.currentUser,
                                onClick = { onTravelerClick(traveler.id, traveler.currentUser) },
                                onFollowClick = { viewModel.toggleFollow(traveler) }
                            )
                        }
                        if (state.page + 1 < state.totalPages) {
                            item(span = { GridItemSpan(maxLineSpan) }) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 10.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (state.isLoadingMore) {
                                        UserResultCardSkeleton()
                                    } else {
                                        TextButton(onClick = viewModel::loadMore) {
                                            Text(stringResource(R.string.ui_dfe60ca92e), color = PrimaryBlue)
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
            PillFilterChip(
                selected = period == selectedPeriod,
                onClick = { if (period != selectedPeriod) onPeriodSelected(period) },
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

@Composable
private fun TopTravelerGridSkeleton(
    modifier: Modifier = Modifier
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(6) {
            UserResultCardSkeleton()
        }
    }
}
