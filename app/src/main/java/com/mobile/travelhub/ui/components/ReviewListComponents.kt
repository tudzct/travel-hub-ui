package com.mobile.travelhub.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.ui.res.stringResource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.mobile.travelhub.ui.theme.OnSurface
import com.mobile.travelhub.ui.theme.OnSurfaceVariant
import com.mobile.travelhub.ui.theme.PrimaryBlue
import com.mobile.travelhub.ui.theme.SurfaceBg
import com.mobile.travelhub.viewmodels.ReviewListUiState
import com.mobile.travelhub.R

@Composable
fun ReviewListScreenContent(
    uiState: ReviewListUiState,
    onBack: () -> Unit
) {
    Scaffold(
        containerColor = SurfaceBg,
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier
                        .size(44.dp)
                        .background(Color(0xFFF2F4F7), CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = stringResource(R.string.ui_b52b36b726),
                        tint = OnSurface
                    )
                }
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = stringResource(R.string.ui_30333d8358),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = OnSurface
                    )
                    Text(
                        text = "${uiState.items.size} đánh giá",
                        style = MaterialTheme.typography.bodyMedium,
                        color = OnSurfaceVariant
                    )
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            when {
                uiState.isLoading -> {
                    LoadingListSkeleton(itemCount = 4)
                }

                uiState.errorMessage != null -> {
                    Text(
                        text = uiState.errorMessage.orEmpty(),
                        color = Color(0xFFC93C3C)
                    )
                }

                uiState.items.isEmpty() -> {
                    Text(
                        text = stringResource(R.string.ui_faac2d3623),
                        color = OnSurfaceVariant
                    )
                }

                else -> {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        items(uiState.items, key = { it.id }) { review ->
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(24.dp),
                                color = Color(0xFFF3EEF9)
                            ) {
                                Row(
                                    modifier = Modifier.padding(14.dp),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    if (review.user.avatarUrl.isNullOrBlank()) {
                                        Box(
                                            modifier = Modifier
                                                .size(48.dp)
                                                .background(Color(0xFFE6F1FB), CircleShape),
                                            contentAlignment = androidx.compose.ui.Alignment.Center
                                        ) {
                                            Text(
                                                text = review.user.name.ifBlank { review.user.username }.take(1).uppercase(),
                                                color = PrimaryBlue,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    } else {
                                        AsyncImage(
                                            model = review.user.avatarUrl,
                                            contentDescription = review.user.name,
                                            modifier = Modifier
                                                .size(48.dp)
                                                .background(Color(0xFFE6F1FB), CircleShape)
                                        )
                                    }
                                    Column(
                                        modifier = Modifier.weight(1f),
                                        verticalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Column(
                                                modifier = Modifier.weight(1f),
                                                verticalArrangement = Arrangement.spacedBy(2.dp)
                                            ) {
                                                Text(
                                                    text = review.user.name.ifBlank { review.user.username },
                                                    style = MaterialTheme.typography.titleMedium,
                                                    fontWeight = FontWeight.SemiBold,
                                                    color = OnSurface,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                                Text(
                                                    text = "@${review.user.username}",
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = OnSurfaceVariant
                                                )
                                            }
                                            Surface(
                                                shape = RoundedCornerShape(999.dp),
                                                color = Color(0xFFE6F1FB)
                                            ) {
                                                Row(
                                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Filled.Star,
                                                        contentDescription = null,
                                                        tint = Color(0xFFFFB800),
                                                        modifier = Modifier.size(14.dp)
                                                    )
                                                    Text(
                                                        text = stringResource(R.string.rating_out_of_five, review.rating),
                                                        style = MaterialTheme.typography.labelLarge,
                                                        color = PrimaryBlue
                                                    )
                                                }
                                            }
                                        }
                                        Text(
                                            text = review.content,
                                            style = MaterialTheme.typography.bodyLarge,
                                            color = OnSurfaceVariant
                                        )
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
