package com.mobile.travelhub.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.mobile.travelhub.R
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.mobile.travelhub.ui.components.ReviewListScreenContent
import com.mobile.travelhub.ui.components.ReviewWriteSheet
import com.mobile.travelhub.viewmodels.ReviewListViewModel
import com.mobile.travelhub.viewmodels.ReviewUiState
import com.mobile.travelhub.viewmodels.ReviewViewModel

@Composable
fun ReviewListScreen(
    placeId: Long,
    onBack: () -> Unit,
    onAuthorClick: (Long) -> Unit,
    onRequireLogin: () -> Unit,
    reviewListViewModel: ReviewListViewModel = hiltViewModel(),
    reviewViewModel: ReviewViewModel = hiltViewModel()
) {
    val uiState by reviewListViewModel.uiState.collectAsState()
    val reviewUiState by reviewViewModel.uiState.collectAsState()
    var showReviewSheet by remember { mutableStateOf(false) }

    LaunchedEffect(placeId) {
        reviewListViewModel.load(placeId)
    }

    LaunchedEffect(showReviewSheet) {
        if (showReviewSheet) {
            reviewViewModel.initialize(null)
        }
    }

    LaunchedEffect(reviewUiState.submittedReview?.id, reviewUiState.submittedReview?.updatedAt) {
        val submittedReview = reviewUiState.submittedReview ?: return@LaunchedEffect
        showReviewSheet = false
        reviewViewModel.consumeSubmittedReview()
        reviewListViewModel.refresh()
    }

    LaunchedEffect(reviewUiState.unauthorized) {
        if (reviewUiState.unauthorized) {
            reviewViewModel.clearUnauthorized()
            showReviewSheet = false
            onRequireLogin()
        }
    }

    ReviewListScreenContent(
        uiState = uiState,
        onBack = onBack,
        onRatingFilterSelected = reviewListViewModel::selectRating,
        onSortSelected = reviewListViewModel::selectSort,
        onLoadMore = reviewListViewModel::loadMore,
        onAuthorClick = onAuthorClick,
        onWriteReview = { showReviewSheet = true }
    )

    if (showReviewSheet) {
        ReviewWriteSheet(
            uiState = reviewUiState,
            titleResId = R.string.ui_b19c813eda,
            placeholderResId = R.string.ui_79d0b72c6c,
            submitTextResId = R.string.ui_96ee09303d,
            onDismiss = { showReviewSheet = false },
            onRatingChange = reviewViewModel::updateRating,
            onContentChange = reviewViewModel::updateContent,
            onSubmit = { reviewViewModel.submit(placeId) }
        )
    }
}
