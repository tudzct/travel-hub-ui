package com.mobile.travelhub.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.mobile.travelhub.ui.components.FeedPostCard
import com.mobile.travelhub.ui.components.FeedPostCardSkeleton
import com.mobile.travelhub.ui.components.HomeCommentsBottomSheet
import com.mobile.travelhub.ui.components.LoadingContentSkeleton
import com.mobile.travelhub.viewmodels.PostDetailViewModel

@Composable
fun PostDetailScreen(
    onBack: () -> Unit,
    onAuthorClick: (Long) -> Unit,
    viewModel: PostDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            PostDetailTopBar(onBack = onBack)

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                when {
                    uiState.isLoading && uiState.post == null -> {
                        item { FeedPostCardSkeleton() }
                    }

                    uiState.errorMessage != null && uiState.post == null -> {
                        item {
                            PostDetailError(
                                message = uiState.errorMessage ?: "Không thể tải bài viết",
                                onRetry = viewModel::refreshPost
                            )
                        }
                    }

                    uiState.post != null -> {
                        val post = requireNotNull(uiState.post)
                        item {
                            FeedPostCard(
                                post = post,
                                onLikeClick = viewModel::onLikeClicked,
                                onSaveClick = viewModel::onSaveClicked,
                                onCommentClick = viewModel::onCommentClicked,
                                onAuthorClick = { onAuthorClick(post.ownerId) }
                            )
                        }
                    }
                }
            }
        }

        if (uiState.isLoading && uiState.post != null) {
            LoadingContentSkeleton(modifier = Modifier.fillMaxSize())
        }

        if (uiState.isCommentSheetVisible) {
            HomeCommentsBottomSheet(
                comments = uiState.comments,
                commentInput = uiState.commentInput,
                isCommentsLoading = uiState.isCommentsLoading,
                isCommentSubmitting = uiState.isCommentSubmitting,
                commentsErrorMessage = uiState.commentsErrorMessage,
                commentErrorMessage = uiState.commentErrorMessage,
                onDismiss = viewModel::onCommentDismissed,
                onCommentInputChanged = viewModel::onCommentInputChanged,
                onCommentSubmit = viewModel::submitComment
            )
        }
    }
}

@Composable
private fun PostDetailTopBar(onBack: () -> Unit) {
    Surface(
        color = Color.White,
        shadowElevation = 1.dp
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .padding(horizontal = 4.dp),
            contentAlignment = Alignment.Center
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier.align(Alignment.CenterStart)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back"
                )
            }
            Text(
                text = "Post",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun PostDetailError(
    message: String,
    onRetry: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "Không thể tải bài viết",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        TextButton(onClick = onRetry) {
            Text("Thử lại")
        }
    }
}
