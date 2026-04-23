package com.mobile.travelhub.ui.screens

import android.net.Uri
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.MoreHoriz
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Send
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import coil.compose.AsyncImage
import com.mobile.travelhub.R
import com.mobile.travelhub.data.model.TravelPlaceListItemResponse
import com.mobile.travelhub.ui.components.CommentItem
import com.mobile.travelhub.viewmodels.HomePostUiModel
import com.mobile.travelhub.viewmodels.HomeUiState
import com.mobile.travelhub.viewmodels.HomeViewModel
import com.mobile.travelhub.viewmodels.PlaceListUiState
import com.mobile.travelhub.viewmodels.PlaceListViewModel

@Composable
fun PlaceListScreen(
    onPlaceClick: (Long) -> Unit,
    placeListViewModel: PlaceListViewModel = hiltViewModel(),
    homeViewModel: HomeViewModel = hiltViewModel()
) {
    val placeUiState by placeListViewModel.uiState.collectAsState()
    val homeUiState by homeViewModel.uiState.collectAsState()

    PlaceListScreenContent(
        placeUiState = placeUiState,
        homeUiState = homeUiState,
        onPlaceClick = onPlaceClick,
        onKeywordChange = placeListViewModel::onKeywordChange,
        onRetryPlaces = placeListViewModel::refresh,
        onRetryPosts = homeViewModel::refreshPosts,
        onLikeClick = homeViewModel::onLikeClicked,
        onCommentClick = homeViewModel::onCommentClicked,
        onDismissCommentSheet = homeViewModel::onCommentDismissed,
        onCommentInputChanged = homeViewModel::onCommentInputChanged,
        onCommentSubmit = homeViewModel::submitComment
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PlaceListScreenContent(
    placeUiState: PlaceListUiState,
    homeUiState: HomeUiState,
    onPlaceClick: (Long) -> Unit,
    onKeywordChange: (String) -> Unit,
    onRetryPlaces: () -> Unit,
    onRetryPosts: () -> Unit,
    onLikeClick: (Long) -> Unit,
    onCommentClick: (Long) -> Unit,
    onDismissCommentSheet: () -> Unit,
    onCommentInputChanged: (String) -> Unit,
    onCommentSubmit: () -> Unit
) {
    val activeCommentPost = homeUiState.posts.firstOrNull { it.id == homeUiState.activeCommentPostId }
    val activeComments = homeUiState.activeCommentPostId
        ?.let { homeUiState.commentsByPostId[it] }
        .orEmpty()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(top = 18.dp, bottom = 112.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                FeedHeader(
                    keyword = placeUiState.keyword,
                    onKeywordChange = onKeywordChange,
                    resultCount = placeUiState.items.size
                )
            }

            when {
                placeUiState.isLoading && placeUiState.items.isEmpty() -> {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 40.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = VerdantPrimary)
                        }
                    }
                }

                placeUiState.errorMessage != null && placeUiState.items.isEmpty() -> {
                    item {
                        FeedEmptyState(
                            title = "Không thể tải địa điểm",
                            message = placeUiState.errorMessage.orEmpty(),
                            fullScreen = false,
                            onRetry = onRetryPlaces
                        )
                    }
                }

                placeUiState.items.isEmpty() -> {
                    item {
                        FeedEmptyState(
                            title = if (placeUiState.keyword.isBlank()) {
                                "Chưa có địa điểm nào"
                            } else {
                                "Không có địa điểm phù hợp"
                            },
                            message = if (placeUiState.keyword.isBlank()) {
                                "Dữ liệu địa điểm vẫn chưa được thêm vào hệ thống."
                            } else {
                                "Thử từ khóa khác để hiện lại danh sách địa điểm."
                            },
                            fullScreen = false,
                            onRetry = null
                        )
                    }
                }

                else -> {
                    item {
                        LocationsRail(
                            places = placeUiState.items.take(10),
                            onPlaceClick = { onPlaceClick(it.id) }
                        )
                    }
                }
            }

            item {
                PostsSectionHeader()
            }

            when {
                homeUiState.isLoading && homeUiState.posts.isEmpty() -> {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = VerdantPrimary)
                        }
                    }
                }

                homeUiState.errorMessage != null && homeUiState.posts.isEmpty() -> {
                    item {
                        FeedEmptyState(
                            title = "Không thể tải bài viết",
                            message = homeUiState.errorMessage ?: "Failed to load posts",
                            fullScreen = false,
                            onRetry = onRetryPosts
                        )
                    }
                }

                else -> {
                    itemsIndexed(
                        items = homeUiState.posts,
                        key = { index, post -> "${post.id}-$index" }
                    ) { _, post ->
                        PostFeedCard(
                            post = post,
                            onLikeClick = { onLikeClick(post.id) },
                            onCommentClick = { onCommentClick(post.id) }
                        )
                    }
                }
            }
        }

        if (activeCommentPost != null) {
            ModalBottomSheet(
                onDismissRequest = onDismissCommentSheet
            ) {
                Text(
                    text = "Comments",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(horizontal = 24.dp)
                )
                Spacer(modifier = Modifier.height(12.dp))

                if (homeUiState.isCommentsLoading) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                } else if (!homeUiState.commentsErrorMessage.isNullOrBlank()) {
                    Text(
                        text = homeUiState.commentsErrorMessage,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(horizontal = 24.dp)
                    )
                } else if (activeComments.isEmpty()) {
                    Text(
                        text = "No comments yet",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 24.dp)
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 320.dp)
                    ) {
                        items(
                            items = activeComments,
                            key = { it.id }
                        ) { comment ->
                            CommentItem(
                                name = comment.username,
                                comment = comment.content,
                                time = comment.timeAgoLabel,
                                avatarRes = R.drawable.female_avatar_maker
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = homeUiState.commentInput,
                        onValueChange = onCommentInputChanged,
                        placeholder = { Text("Add a comment") },
                        modifier = Modifier.weight(1f),
                        enabled = !homeUiState.isCommentSubmitting,
                        maxLines = 3
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = onCommentSubmit,
                        enabled = !homeUiState.isCommentSubmitting && homeUiState.commentInput.isNotBlank()
                    ) {
                        if (homeUiState.isCommentSubmitting) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text("Post")
                        }
                    }
                }

                if (!homeUiState.commentErrorMessage.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = homeUiState.commentErrorMessage,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
private fun FeedHeader(
    keyword: String,
    onKeywordChange: (String) -> Unit,
    resultCount: Int
) {
    Column(
        modifier = Modifier.padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "TRAVEL HUB",
                    style = MaterialTheme.typography.labelMedium,
                    color = VerdantPrimary,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "Discovery Feed",
                    style = MaterialTheme.typography.headlineMedium,
                    color = VerdantOnSurface,
                    fontWeight = FontWeight.Bold
                )
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = CircleShape,
                    color = VerdantSurfaceContainerHighest,
                    modifier = Modifier.size(40.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = "T",
                            style = MaterialTheme.typography.titleMedium,
                            color = VerdantPrimary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        TextField(
            value = keyword,
            onValueChange = onKeywordChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = {
                Text(
                    text = "Search destinations or provinces",
                    color = VerdantOnSurfaceVariant
                )
            },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Outlined.Search,
                    contentDescription = null,
                    tint = VerdantOnSurfaceVariant
                )
            },
            shape = RoundedCornerShape(22.dp),
            singleLine = true,
            colors = TextFieldDefaults.colors(
                focusedContainerColor = VerdantSurfaceContainerHighest,
                unfocusedContainerColor = VerdantSurfaceContainerHighest,
                disabledContainerColor = VerdantSurfaceContainerHighest,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                disabledIndicatorColor = Color.Transparent,
                focusedTextColor = VerdantOnSurface,
                unfocusedTextColor = VerdantOnSurface,
                cursorColor = VerdantPrimary
            )
        )
    }
}

@Composable
private fun LocationsRail(
    places: List<TravelPlaceListItemResponse>,
    onPlaceClick: (TravelPlaceListItemResponse) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Locations",
                style = MaterialTheme.typography.titleMedium,
                color = VerdantOnSurface,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Swipe",
                style = MaterialTheme.typography.labelMedium,
                color = VerdantPrimary
            )
        }

        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(places, key = { it.id }) { place ->
                LocationCard(
                    place = place,
                    onClick = { onPlaceClick(place) }
                )
            }
        }
    }
}

@Composable
private fun LocationCard(
    place: TravelPlaceListItemResponse,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .width(104.dp)
            .height(172.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(24.dp),
        color = VerdantSurfaceContainerLowest,
        shadowElevation = 8.dp
    ) {
        Box {
            AsyncImage(
                model = place.mainImage,
                contentDescription = place.name,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                VerdantPrimary.copy(alpha = 0.08f),
                                Color.Transparent,
                                VerdantOnSurface.copy(alpha = 0.76f)
                            )
                        )
                    )
            )
            Surface(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(8.dp),
                shape = CircleShape,
                color = VerdantSurfaceContainerLowest.copy(alpha = 0.94f)
            ) {
                Box(
                    modifier = Modifier.size(30.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = place.province.name.take(1).uppercase(),
                        style = MaterialTheme.typography.labelLarge,
                        color = VerdantPrimary,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(10.dp),
                verticalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                Text(
                    text = place.name,
                    style = MaterialTheme.typography.labelLarge,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = place.province.name,
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White.copy(alpha = 0.82f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun PostsSectionHeader() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Posts",
            style = MaterialTheme.typography.titleLarge,
            color = VerdantOnSurface,
            fontWeight = FontWeight.Bold
        )

    }
}

@Composable
private fun FeedEmptyState(
    title: String,
    message: String,
    fullScreen: Boolean,
    onRetry: (() -> Unit)?
) {
    val containerModifier = if (fullScreen) {
        Modifier.fillMaxSize()
    } else {
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    }

    Box(
        modifier = containerModifier,
        contentAlignment = Alignment.Center
    ) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = VerdantSurfaceContainer,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineSmall,
                    color = VerdantOnSurface,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyLarge,
                    color = VerdantOnSurfaceVariant
                )
                if (onRetry != null) {
                    TextButton(onClick = onRetry) {
                        Text("Retry")
                    }
                }
            }
        }
    }
}

@Composable
private fun PostFeedCard(
    post: HomePostUiModel,
    onLikeClick: () -> Unit,
    onCommentClick: () -> Unit
) {
    PostItemContent(
        post = post,
        onLikeClick = onLikeClick,
        onCommentClick = onCommentClick
    )
}

@Composable
private fun PostItemContent(
    post: HomePostUiModel,
    onLikeClick: () -> Unit,
    onCommentClick: () -> Unit
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

    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Image(
                    painter = painterResource(R.drawable.female_avatar_maker),
                    contentDescription = post.username,
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        text = post.username,
                        style = MaterialTheme.typography.titleMedium,
                        color = VerdantOnSurface,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = post.subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = VerdantOnSurfaceVariant
                    )
                }
            }
            Icon(
                imageVector = Icons.Outlined.MoreHoriz,
                contentDescription = null,
                tint = VerdantOnSurfaceVariant
            )
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(360.dp)
                .background(Color(0xFFF5F5F5))
        ) {
            if (post.imageUrls.isNotEmpty()) {
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier.fillMaxSize()
                ) { page ->
                    val resolvedUrl = toDisplayUrl(post.imageUrls[page])
                    LaunchedEffect(resolvedUrl) {
                        Log.d("PlaceListPostImageUrl", "Resolved image url: $resolvedUrl")
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
                                    VerdantOnSurface
                                } else {
                                    VerdantSurfaceContainerHighest
                                }
                            )
                    )
                }
            }
        }

        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onLikeClick,
                        enabled = !post.isLikeLoading,
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = if (post.isLiked) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                            contentDescription = "Like",
                            tint = if (post.isLiked) MaterialTheme.colorScheme.error else VerdantOnSurface,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    IconButton(
                        onClick = onCommentClick,
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.ChatBubbleOutline,
                            contentDescription = "Comment",
                            modifier = Modifier.size(22.dp),
                            tint = VerdantOnSurface
                        )
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Icon(
                        imageVector = Icons.Outlined.Send,
                        contentDescription = "Share",
                        modifier = Modifier.size(22.dp),
                        tint = VerdantOnSurface
                    )
                }
                Icon(
                    imageVector = Icons.Outlined.BookmarkBorder,
                    contentDescription = "Save",
                    modifier = Modifier.size(22.dp),
                    tint = VerdantOnSurface
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "${post.likeCount} likes",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = VerdantOnSurface
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
                overflow = TextOverflow.Ellipsis,
                color = VerdantOnSurface
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = if (post.commentCount <= 0) "View all comments" else "View all ${post.commentCount} comments",
                style = MaterialTheme.typography.bodySmall,
                color = VerdantOnSurfaceVariant
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = post.timeAgoLabel,
                style = MaterialTheme.typography.labelSmall,
                color = VerdantOnSurfaceVariant
            )
        }

        Spacer(
            modifier = Modifier
                .fillMaxWidth()
                .height(10.dp)
                .background(Color(0xFFF3F3F3))
        )
    }
}

private val VerdantPrimary = Color(0xFF006B2C)
private val VerdantSurface = Color.White
private val VerdantSurfaceContainer = Color(0xFFEFF6EA)
private val VerdantSurfaceContainerHighest = Color(0xFFDDE5D9)
private val VerdantSurfaceContainerLowest = Color(0xFFFFFFFF)
private val VerdantOnSurface = Color(0xFF171D16)
private val VerdantOnSurfaceVariant = Color(0xFF3E4A3D)
