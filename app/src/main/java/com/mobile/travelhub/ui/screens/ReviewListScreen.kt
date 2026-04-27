package com.mobile.travelhub.ui.screens

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.mobile.travelhub.ui.components.ReviewListScreenContent
import com.mobile.travelhub.viewmodels.ReviewListViewModel

@Composable
fun ReviewListScreen(
    placeId: Long,
    onBack: () -> Unit,
    reviewListViewModel: ReviewListViewModel = hiltViewModel()
) {
    val uiState by reviewListViewModel.uiState.collectAsState()

    LaunchedEffect(placeId) {
        reviewListViewModel.load(placeId)
    }

    ReviewListScreenContent(uiState = uiState, onBack = onBack)
}
