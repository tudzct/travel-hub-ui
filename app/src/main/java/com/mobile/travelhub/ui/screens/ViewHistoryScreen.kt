package com.mobile.travelhub.ui.screens

import androidx.compose.ui.res.stringResource
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import coil.compose.AsyncImage
import com.mobile.travelhub.ui.components.LoadingListSkeleton
import com.mobile.travelhub.ui.theme.OnSurface
import com.mobile.travelhub.ui.theme.OnSurfaceVariant
import com.mobile.travelhub.ui.theme.PrimaryBlue
import com.mobile.travelhub.ui.theme.SurfaceBg
import com.mobile.travelhub.viewmodels.HistoryViewModel
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import com.mobile.travelhub.R

@Composable
fun ViewHistoryScreen(
    onBack: () -> Unit,
    onPlaceClick: (Long) -> Unit,
    onRequireLogin: () -> Unit,
    historyViewModel: HistoryViewModel = hiltViewModel()
) {
    val uiState by historyViewModel.uiState.collectAsState()

    LaunchedEffect(uiState.unauthorized) {
        if (uiState.unauthorized) {
            historyViewModel.clearUnauthorized()
            onRequireLogin()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Start
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier
                    .size(44.dp)
                    .background(
                        color = Color(0xFFF2F4F7),
                        shape = CircleShape
                    )
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = stringResource(R.string.ui_b52b36b726),
                    tint = OnSurface
                )
            }
        }
        Text(
            text = stringResource(R.string.ui_89871a4aee),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = OnSurface
        )

        when {
            uiState.isLoading -> {
                LoadingListSkeleton(itemCount = 5)
            }

            uiState.errorMessage != null && uiState.items.isEmpty() -> {
                Text(
                    text = uiState.errorMessage.orEmpty(),
                    color = Color(0xFFC93C3C)
                )
            }

            uiState.items.isEmpty() -> {
                Text(
                    text = stringResource(R.string.ui_cd42bab7f4),
                    color = OnSurfaceVariant
                )
            }

            else -> {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    items(uiState.items, key = { "${it.placeId}-${it.viewedAt}" }) { item ->
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onPlaceClick(item.placeId) },
                            shape = RoundedCornerShape(28.dp),
                            color = Color(0xFFF3EEF9),
                            shadowElevation = 0.dp
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                horizontalArrangement = Arrangement.spacedBy(14.dp)
                            ) {
                                AsyncImage(
                                    model = item.mainImage,
                                    contentDescription = item.placeName,
                                    modifier = Modifier
                                        .size(width = 132.dp, height = 112.dp)
                                        .clip(RoundedCornerShape(20.dp)),
                                    contentScale = ContentScale.Crop
                                )
                                Column(
                                    modifier = Modifier
                                        .weight(1f)
                                        .heightIn(min = 112.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text(
                                        text = item.placeName,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.SemiBold,
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis,
                                        color = OnSurface
                                    )
                                    Text(
                                        text = item.provinceName,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = PrimaryBlue
                                    )
                                    Spacer(modifier = Modifier.weight(1f))
                                    Text(
                                        text = formatViewedAt(item.viewedAt),
                                        style = MaterialTheme.typography.bodySmall,
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

private fun formatViewedAt(raw: String?): String {
    if (raw.isNullOrBlank()) {
        return "Không rõ thời gian"
    }
    return runCatching {
        val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")
            .withZone(ZoneId.systemDefault())
        formatter.format(Instant.parse(raw))
    }.getOrDefault(raw)
}
