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
import com.mobile.travelhub.data.model.TravelPlaceReviewResponse
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TextButton

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
    var editingReview by remember { mutableStateOf<TravelPlaceReviewResponse?>(null) }
    var reviewToDelete by remember { mutableStateOf<TravelPlaceReviewResponse?>(null) }

    LaunchedEffect(placeId) {
        reviewListViewModel.load(placeId)
    }

    LaunchedEffect(showReviewSheet) {
        if (showReviewSheet) {
            reviewViewModel.initialize(editingReview)
        }
    }

    LaunchedEffect(reviewUiState.submittedReview?.id, reviewUiState.submittedReview?.updatedAt) {
        val submittedReview = reviewUiState.submittedReview ?: return@LaunchedEffect
        showReviewSheet = false
        editingReview = null
        reviewViewModel.consumeSubmittedReview()
        reviewListViewModel.refresh()
    }

    LaunchedEffect(reviewUiState.unauthorized) {
        if (reviewUiState.unauthorized) {
            reviewViewModel.clearUnauthorized()
            showReviewSheet = false
            editingReview = null
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
        onWriteReview = {
            editingReview = uiState.myReview
            showReviewSheet = true
        }
    )

    if (showReviewSheet) {
        ReviewWriteSheet(
            uiState = reviewUiState,
            titleResId = R.string.ui_b19c813eda,
            placeholderResId = R.string.ui_79d0b72c6c,
            submitTextResId = R.string.ui_96ee09303d,
            onDismiss = { 
                showReviewSheet = false
                editingReview = null
            },
            onRatingChange = reviewViewModel::updateRating,
            onContentChange = reviewViewModel::updateContent,
            onSubmit = { reviewViewModel.submit(placeId) },
            onDelete = if (editingReview != null) { { 
                showReviewSheet = false
                reviewToDelete = editingReview
            } } else null
        )
    }

    if (reviewToDelete != null) {
        AlertDialog(
            onDismissRequest = { reviewToDelete = null },
            title = { Text(text = "Xóa đánh giá") },
            text = { Text(text = "Bạn có chắc chắn muốn xóa đánh giá này không? Thao tác này không thể hoàn tác.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        reviewToDelete = null
                        reviewListViewModel.deleteReview(placeId)
                    }
                ) {
                    Text(text = "Xóa", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { reviewToDelete = null }
                ) {
                    Text(text = "Hủy")
                }
            }
        )
    }
}
