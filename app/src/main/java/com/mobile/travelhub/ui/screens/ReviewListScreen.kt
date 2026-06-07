package com.mobile.travelhub.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.mobile.travelhub.ui.components.ReviewListScreenContent
import com.mobile.travelhub.ui.components.SimpleFormTextField
import com.mobile.travelhub.ui.theme.OnSurface
import com.mobile.travelhub.ui.theme.PrimaryBlue
import com.mobile.travelhub.ui.theme.SurfaceContainerLow
import com.mobile.travelhub.ui.theme.SurfaceContainerLowest
import com.mobile.travelhub.viewmodels.ReviewListViewModel
import com.mobile.travelhub.viewmodels.ReviewUiState
import com.mobile.travelhub.viewmodels.ReviewViewModel

@OptIn(ExperimentalMaterial3Api::class)
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
        ReviewListWriteSheet(
            uiState = reviewUiState,
            onDismiss = { showReviewSheet = false },
            onRatingChange = reviewViewModel::updateRating,
            onContentChange = reviewViewModel::updateContent,
            onSubmit = { reviewViewModel.submit(placeId) }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ReviewListWriteSheet(
    uiState: ReviewUiState,
    onDismiss: () -> Unit,
    onRatingChange: (Int) -> Unit,
    onContentChange: (String) -> Unit,
    onSubmit: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = {
            if (!uiState.isSubmitting) {
                onDismiss()
            }
        },
        dragHandle = { BottomSheetDefaults.DragHandle() },
        containerColor = SurfaceContainerLowest
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Viết đánh giá",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.ExtraBold,
                color = OnSurface
            )
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                repeat(5) { index ->
                    val star = index + 1
                    Icon(
                        imageVector = Icons.Filled.Star,
                        contentDescription = "$star sao",
                        tint = if (star <= uiState.rating) Color(0xFFFFB300) else SurfaceContainerLow,
                        modifier = Modifier
                            .size(34.dp)
                            .clickable(enabled = !uiState.isSubmitting) {
                                onRatingChange(star)
                            }
                    )
                }
            }
            SimpleFormTextField(
                value = uiState.content,
                onValueChange = onContentChange,
                placeholder = "Chia sẻ trải nghiệm của bạn về địa điểm này",
                singleLine = false,
                minLines = 4,
                enabled = !uiState.isSubmitting
            )
            if (!uiState.errorMessage.isNullOrBlank()) {
                Text(
                    text = uiState.errorMessage,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
            }
            Button(
                onClick = onSubmit,
                enabled = !uiState.isSubmitting,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = PrimaryBlue,
                    contentColor = Color.White
                )
            ) {
                if (uiState.isSubmitting) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp,
                        color = Color.White
                    )
                } else {
                    Text(
                        text = "Gửi đánh giá",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}
