package com.mobile.travelhub.ui.screens

import android.net.Uri
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Send
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import coil.compose.AsyncImage
import com.mobile.travelhub.R
import com.mobile.travelhub.viewmodels.HomePostUiModel
import com.mobile.travelhub.viewmodels.HomeUiState
import com.mobile.travelhub.viewmodels.HomeViewModel
import androidx.compose.ui.text.buildAnnotatedString

@Composable
fun HomeScreen(viewModel: HomeViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsState()

    HomeScreenContent(
        uiState = uiState,
        onRetry = viewModel::refreshPosts
    )
}

@Composable
private fun HomeScreenContent(
    uiState: HomeUiState,
    onRetry: () -> Unit
) {

    Scaffold(
        topBar = {
            TopBar()
        },
        modifier = Modifier.fillMaxSize()
    ) { innerPadding ->
        when {
            uiState.isLoading && uiState.posts.isEmpty() -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            uiState.errorMessage != null && uiState.posts.isEmpty() -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .padding(horizontal = 20.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = uiState.errorMessage ?: "Failed to load posts",
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(onClick = onRetry) {
                        Text("Retry")
                    }
                }
            }

            else -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    itemsIndexed(
                        items = uiState.posts,
                        key = { index, post -> "${post.id}-$index" }
                    ) { _, post ->
                        PostItem(
                            post = post,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun PostItem(
    post: HomePostUiModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val storageService = context.getString(R.string.storage_service)
        .trim()
        .trim('"')
        .trimEnd('/')

    fun toDisplayUrl(rawUrl: String): String {
        val normalized = rawUrl.trim()
        if (normalized.isBlank()) return ""

        val fallbackBase = "http://10.0.2.2:9000"
        val rawBase = if (storageService.isBlank()) fallbackBase else storageService
        val base = if (rawBase.endsWith("/travelhub", ignoreCase = true)) {
            rawBase
        } else {
            "$rawBase/travelhub"
        }

        if (
            normalized.startsWith("http://", ignoreCase = true) ||
            normalized.startsWith("https://", ignoreCase = true)
        ) {
            val parsed = Uri.parse(normalized)
            val host = parsed.host?.lowercase()
            if (host == "localhost" || host == "127.0.0.1") {
                val path = parsed.encodedPath?.trimStart('/').orEmpty()
                val query = parsed.encodedQuery?.let { "?$it" }.orEmpty()
                val normalizedPath = if (path.startsWith("travelhub/", ignoreCase = true)) {
                    path.removePrefix("travelhub/")
                } else {
                    path
                }
                return if (normalizedPath.isBlank()) base else "$base/$normalizedPath$query"
            }
            return normalized
        }

        return "$base/${normalized.trimStart('/')}"
    }

    val imageCount = post.imageUrls.size.coerceAtLeast(1)
    val pagerState = rememberPagerState(pageCount = { imageCount })

    Column(
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(horizontal = 12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Image(
                    painter = painterResource(R.drawable.female_avatar_maker),
                    contentDescription = post.username,
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = post.username,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = post.subtitle,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(360.dp)
                .background(MaterialTheme.colorScheme.surfaceContainerLow)
        ) {
            if (post.imageUrls.isNotEmpty()) {
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier.fillMaxSize()
                ) { page ->
                    val resolvedUrl = toDisplayUrl(post.imageUrls[page])
                    LaunchedEffect(resolvedUrl) {
                        Log.d("HomeScreenImageUrl", "Resolved image url: $resolvedUrl")
                    }
                    AsyncImage(
                        model = resolvedUrl,
                        contentDescription = post.description,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }
            }
        }

        if (imageCount > 1) {
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                repeat(imageCount) { index ->
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 2.dp)
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(
                                if (pagerState.currentPage == index) {
                                    MaterialTheme.colorScheme.onSurface
                                } else {
                                    MaterialTheme.colorScheme.outlineVariant
                                }
                            )
                    )
                }
            }
        }

        Column(modifier = Modifier.padding(horizontal = 12.dp)) {
            Spacer(modifier = Modifier.height(20.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Outlined.FavoriteBorder,
                        contentDescription = "Like",
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(14.dp))
                    Icon(
                        imageVector = Icons.Outlined.ChatBubbleOutline,
                        contentDescription = "Comment",
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(14.dp))
                    Icon(
                        imageVector = Icons.Outlined.Send,
                        contentDescription = "Share",
                        modifier = Modifier.size(22.dp)
                    )
                }
                Icon(
                    imageVector = Icons.Outlined.BookmarkBorder,
                    contentDescription = "Save",
                    modifier = Modifier.size(22.dp)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = post.likeCountLabel,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = buildAnnotatedString {
                    withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                        append(post.username)
                    }
                    append("  ")
                    append(post.description)
                },
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = post.commentCountLabel,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = post.timeAgoLabel,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Spacer(modifier = Modifier.height(20.dp))
    }
}

@Composable
fun TopBar() {
    val tabs = listOf("For You", "Following")
    var selectedTabIndex by remember { mutableStateOf(0) }

    TabRow(
        selectedTabIndex = selectedTabIndex,
        containerColor = MaterialTheme.colorScheme.background,
        contentColor = MaterialTheme.colorScheme.onSurface,
        indicator = { tabPositions ->
            TabRowDefaults.Indicator(
                modifier = Modifier
                    .tabIndicatorOffset(tabPositions[selectedTabIndex]),
                color = MaterialTheme.colorScheme.onSurface,
                height = 2.dp
            )
        },
        divider = {} // remove bottom divider if not needed
    ) {
        tabs.forEachIndexed { index, title ->
            Tab(
                selected = selectedTabIndex == index,
                onClick = { selectedTabIndex = index },
                text = {
                    Text(
                        text = title,
                        fontWeight = if (selectedTabIndex == index)
                            FontWeight.Bold else FontWeight.Normal,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            )
        }
    }
}
