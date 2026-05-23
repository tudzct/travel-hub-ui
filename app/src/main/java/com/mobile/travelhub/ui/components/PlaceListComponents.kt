package com.mobile.travelhub.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
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
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Send
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Place
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import coil.compose.AsyncImage
import com.mobile.travelhub.R
import com.mobile.travelhub.data.model.TravelPlaceListItemResponse
import com.mobile.travelhub.ui.components.modifiers.shimmerEffect
import com.mobile.travelhub.viewmodels.HomeCommentUiModel
import com.mobile.travelhub.viewmodels.HomePostUiModel
import com.mobile.travelhub.viewmodels.HomeUiState
import com.mobile.travelhub.viewmodels.PlaceListUiState
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlaceListScreenContent(
    placeUiState: PlaceListUiState,
    homeUiState: HomeUiState,
    onPlaceClick: (TravelPlaceListItemResponse) -> Unit,
    onMenuClick: () -> Unit,
    onSearchClick: () -> Unit,
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
    val listState = rememberLazyListState()
    var previousScrollIndex by remember { mutableIntStateOf(0) }
    var previousScrollOffset by remember { mutableIntStateOf(0) }
    var isTopBarVisible by remember { mutableStateOf(true) }
    val topBarContentHeight = 52.dp

    LaunchedEffect(listState) {
        snapshotFlow { listState.firstVisibleItemIndex to listState.firstVisibleItemScrollOffset }
            .collect { (index, offset) ->
                val isAtTop = index == 0 && offset < 8
                val isScrollingDown = index > previousScrollIndex ||
                    (index == previousScrollIndex && offset > previousScrollOffset)

                isTopBarVisible = isAtTop || !isScrollingDown
                previousScrollIndex = index
                previousScrollOffset = offset
            }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        AnimatedVisibility(
            visible = isTopBarVisible,
            enter = slideInVertically(
                animationSpec = tween(durationMillis = 180),
                initialOffsetY = { -it }
            ) + fadeIn(animationSpec = tween(durationMillis = 180)),
            exit = slideOutVertically(
                animationSpec = tween(durationMillis = 180),
                targetOffsetY = { -it }
            ) + fadeOut(animationSpec = tween(durationMillis = 180)),
            modifier = Modifier.align(Alignment.TopCenter)
        ) {
            FeedTopBar(
                onMenuClick = onMenuClick,
                onSearchClick = onSearchClick
            )
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            state = listState,
            contentPadding = PaddingValues(
                top = topBarContentHeight + 18.dp,
                bottom = 112.dp
            ),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            when {
                placeUiState.isLoading && placeUiState.items.isEmpty() -> {
                    item {
                        LocationsRailSkeleton()
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
                            title = "Chưa có địa điểm nào",
                            message = "Dữ liệu địa điểm vẫn chưa được thêm vào hệ thống.",
                            fullScreen = false,
                            onRetry = null
                        )
                    }
                }

                else -> {
                    item {
                        LocationsRail(
                            places = placeUiState.items.take(10),
                            onPlaceClick = onPlaceClick
                        )
                    }
                }
            }
            when {
                homeUiState.isLoading && homeUiState.posts.isEmpty() -> {
                    items(3) {
                        FeedPostCardSkeleton()
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
                        FeedPostCard(
                            post = post,
                            onLikeClick = { onLikeClick(post.id) },
                            onCommentClick = { onCommentClick(post.id) }
                        )
                    }
                }
            }
        }


        if (activeCommentPost != null) {
            HomeCommentsBottomSheet(
                comments = activeComments,
                commentInput = homeUiState.commentInput,
                isCommentsLoading = homeUiState.isCommentsLoading,
                isCommentSubmitting = homeUiState.isCommentSubmitting,
                commentsErrorMessage = homeUiState.commentsErrorMessage,
                commentErrorMessage = homeUiState.commentErrorMessage,
                onDismiss = onDismissCommentSheet,
                onCommentInputChanged = onCommentInputChanged,
                onCommentSubmit = onCommentSubmit
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeCommentsBottomSheet(
    comments: List<HomeCommentUiModel>,
    commentInput: String,
    isCommentsLoading: Boolean,
    isCommentSubmitting: Boolean,
    commentsErrorMessage: String?,
    commentErrorMessage: String?,
    onDismiss: () -> Unit,
    onCommentInputChanged: (String) -> Unit,
    onCommentSubmit: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = Color.White
    ) {
        Text(
            text = "Comments",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(horizontal = 24.dp)
        )
        Spacer(modifier = Modifier.height(12.dp))

        if (isCommentsLoading) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 24.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else if (!commentsErrorMessage.isNullOrBlank()) {
            Text(
                text = commentsErrorMessage,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(horizontal = 24.dp)
            )
        } else if (comments.isEmpty()) {
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
                    items = comments,
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
            SimpleFormTextField(
                value = commentInput,
                onValueChange = onCommentInputChanged,
                placeholder = "Add a comment",
                modifier = Modifier.weight(1f),
                enabled = !isCommentSubmitting,
                singleLine = false,
                maxLines = 3,
                shape = RoundedCornerShape(24.dp),
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White,
                disabledContainerColor = Color(0xFFF4F4F4),
                focusedIndicatorColor = VerdantPrimary
            )
            Spacer(modifier = Modifier.width(8.dp))
            IconButton(
                onClick = onCommentSubmit,
                enabled = !isCommentSubmitting && commentInput.isNotBlank(),
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(VerdantPrimary)
            ) {
                if (isCommentSubmitting) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp,
                        color = Color.White
                    )
                } else {
                    Icon(
                        imageVector = Icons.AutoMirrored.Outlined.Send,
                        modifier = Modifier.size(16.dp),
                        contentDescription = "Send comment",
                        tint = Color.White
                    )
                }
            }
        }

        if (!commentErrorMessage.isNullOrBlank()) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = commentErrorMessage,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun FeedTopBar(
    onMenuClick: () -> Unit,
    onSearchClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val topBarContentHeight = 52.dp

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(topBarContentHeight)
            .background(Color.White)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(topBarContentHeight)
                .align(Alignment.BottomCenter)
                .padding(horizontal = 6.dp)
        ) {
            IconButton(
                onClick = onMenuClick,
                modifier = Modifier.align(Alignment.CenterStart)
            ) {
                Icon(
                    imageVector = Icons.Filled.Menu,
                    contentDescription = "Open menu",
                    tint = VerdantOnSurface
                )
            }
            Text(
                text = "Travel Hub",
                modifier = Modifier.align(Alignment.Center),
                style = MaterialTheme.typography.titleMedium,
                color = VerdantOnSurface,
                fontWeight = FontWeight.ExtraBold
            )

            IconButton(
                onClick = onSearchClick,
                modifier = Modifier.align(Alignment.CenterEnd)
            ) {
                Icon(
                    imageVector = Icons.Outlined.Search,
                    contentDescription = "Search",
                    tint = VerdantOnSurface
                )
            }
        }
    }
}

@Preview
@Composable
fun FeedTopBarPreview(){
    FeedTopBar(onMenuClick = {}, onSearchClick = {})
}

@Composable
private fun LocationsRail(
    places: List<TravelPlaceListItemResponse>,
    onPlaceClick: (TravelPlaceListItemResponse) -> Unit
) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        items(places, key = { it.id }) { place ->
            FeaturedLocationCard(
                country = place.province.name,
                city = place.name,
                imageUrl = place.mainImage,
                modifier = Modifier
                    .width(220.dp)
                    .height(270.dp),
                onClick = { onPlaceClick(place) }
            )
        }
    }

}

@Composable
private fun LocationsRailSkeleton() {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        items(4) {
            FeaturedLocationCardSkeleton(
                modifier = Modifier
                    .width(220.dp)
                    .height(270.dp)
            )
        }
    }
}

@Composable
private fun FeaturedLocationCardSkeleton(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .shimmerEffect()
    ) {
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(14.dp)
                .size(42.dp)
                .clip(CircleShape)
                .shimmerEffect()
        )
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(start = 14.dp, end = 56.dp, bottom = 14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .width(76.dp)
                    .height(10.dp)
                    .clip(RoundedCornerShape(50))
                    .shimmerEffect()
            )
            Box(
                modifier = Modifier
                    .width(124.dp)
                    .height(20.dp)
                    .clip(RoundedCornerShape(50))
                    .shimmerEffect()
            )
        }
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
@OptIn(ExperimentalFoundationApi::class)
fun FeedPostCard(
    post: HomePostUiModel,
    onLikeClick: () -> Unit,
    onCommentClick: () -> Unit,
    actionsEnabled: Boolean = true
) {
    val context = LocalContext.current
    val storageService = stringResource(R.string.storage_service)
        .trim()
        .trim('"')
        .trimEnd('/')

    fun toDisplayUrl(rawUrl: String): String {
        val value = rawUrl.trim()
        if (value.isEmpty()) return ""

        if (value.startsWith("http://", true) || value.startsWith("https://", true)) {
            return value
        }

        val base = storageService
            .trim()
            .trim('"')
            .trimEnd('/')

        return "$base/${value.trimStart('/')}"
    }

    val imageCount = post.imageUrls.size.coerceAtLeast(1)
    val pagerState = rememberPagerState(pageCount = { imageCount })

    Column (modifier = Modifier.background(Color.White)) {

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    painter = painterResource(R.drawable.female_avatar_maker),
                    contentDescription = post.username,
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Text(
                        text = post.username,
                        style = MaterialTheme.typography.titleSmall,
                        color = VerdantOnSurface,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Place,
                            contentDescription = null,
                            modifier = Modifier.size(12.dp),
                            tint = VerdantOnSurfaceVariant
                        )
                        Spacer(modifier = Modifier.width(2.dp))
                        Text(
                            text = post.subtitle,
                            modifier = Modifier
                                .weight(1f)
                                .basicMarquee(),
                            style = MaterialTheme.typography.bodySmall,
                            color = VerdantOnSurfaceVariant,
                            maxLines = 1
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.width(4.dp))

            Text(
                text = post.timeAgoLabel,
                style = MaterialTheme.typography.labelSmall,
                color = VerdantOnSurfaceVariant.copy(alpha = 0.7f)
            )
        }

        Text(
            modifier = Modifier.padding(horizontal = 16.dp),
            text = post.description,
            style = MaterialTheme.typography.bodyMedium,
            maxLines = 3,
            overflow = TextOverflow.Ellipsis,
            color = VerdantOnSurface
        )

        Spacer(modifier = Modifier.height(12.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(5f / 3f)
                .background(Color(0xFFF5F5F5))
        ) {
            if (post.imageUrls.isNotEmpty()) {
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier.fillMaxWidth()
                ) { page ->
                    val resolvedUrl = toDisplayUrl(post.imageUrls[page])
                    AsyncImage(
                        model = resolvedUrl,
                        contentDescription = post.description,
                        modifier = Modifier.fillMaxWidth(),
                        contentScale = ContentScale.Crop
                    )
                }
            }

            if (imageCount > 1) {
                Row(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 16.dp),
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
                                        Color.White
                                    } else {
                                        Color.White.copy(alpha = 0.5f)
                                    }
                                )
                        )
                    }
                }
            }
        }


        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "${post.likeCount} likes",
                    style = MaterialTheme.typography.labelLarge,
                    color = VerdantOnSurface
                )
                Text(
                    text = "${post.commentCount} comments",
                    style = MaterialTheme.typography.labelLarge,
                    color = VerdantOnSurface
                )
            }
//            Spacer(
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .height(1.dp)
//                    .background(Color(0xFFE0E0E0))
//            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier.weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    IconButton(
                        onClick = onLikeClick,
                        enabled = actionsEnabled && !post.isLikeLoading,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = if (post.isLiked) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                            contentDescription = "Like",
                            tint = if (post.isLiked) MaterialTheme.colorScheme.error else VerdantOnSurface,
                            modifier = Modifier.size(26.dp)
                        )
                    }
                }
                Box(
                    modifier = Modifier.weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    IconButton(
                        onClick = onCommentClick,
                        enabled = actionsEnabled,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.message_circle),
                            contentDescription = "Comment",
                            modifier = Modifier.size(24.dp),
                            tint = VerdantOnSurface
                        )
                    }
                }
                Box(
                    modifier = Modifier.weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.BookmarkBorder,
                        contentDescription = "Save",
                        modifier = Modifier.size(26.dp),
                        tint = VerdantOnSurface
                    )
                }
                Box(
                    modifier = Modifier.weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(R.drawable.share_fat_bold),
                        contentDescription = "Share",
                        modifier = Modifier.size(24.dp),
                        tint = VerdantOnSurface
                    )
                }
            }
        }

        Spacer(
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp)
                .background(Color(0xFFE0E0E0))
        )
    }
}

@Composable
fun FeedPostCardSkeleton(
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .shimmerEffect()
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(7.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .width(112.dp)
                            .height(13.dp)
                            .clip(RoundedCornerShape(50))
                            .shimmerEffect()
                    )
                    Box(
                        modifier = Modifier
                            .width(156.dp)
                            .height(10.dp)
                            .clip(RoundedCornerShape(50))
                            .shimmerEffect()
                    )
                }
            }
            Box(
                modifier = Modifier
                    .width(44.dp)
                    .height(10.dp)
                    .clip(RoundedCornerShape(50))
                    .shimmerEffect()
            )
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(5f / 3f)
                .shimmerEffect()
        )

        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    repeat(3) {
                        Box(
                            modifier = Modifier
                                .size(26.dp)
                                .clip(CircleShape)
                                .shimmerEffect()
                        )
                    }
                }
                Box(
                    modifier = Modifier
                        .size(26.dp)
                        .clip(CircleShape)
                        .shimmerEffect()
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Box(
                modifier = Modifier
                    .width(84.dp)
                    .height(12.dp)
                    .clip(RoundedCornerShape(50))
                    .shimmerEffect()
            )
            Spacer(modifier = Modifier.height(8.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.92f)
                    .height(12.dp)
                    .clip(RoundedCornerShape(50))
                    .shimmerEffect()
            )
            Spacer(modifier = Modifier.height(6.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.62f)
                    .height(12.dp)
                    .clip(RoundedCornerShape(50))
                    .shimmerEffect()
            )
        }

        Spacer(
            modifier = Modifier
                .fillMaxWidth()
                .height(3.dp)
                .background(Color(0xFFE0E0E0))
        )
    }
}

@Preview
@Composable
fun FeedPostCardPreview() {
    FeedPostCard(
        post = HomePostUiModel(
            id = 1L,
            username = "Duc Duong Hoang",
            subtitle = "Da Nang, Viet Nam",
            description = "A calm afternoon by the river.",
            imageUrls = listOf("sample.jpg"),
            likeCount = 142,
            commentCount = 4,
            isLiked = true,
            isLikeLoading = false,
            timeAgoLabel = "2h"
        ),
        onLikeClick = {},
        onCommentClick = {}
    )
}


private val VerdantPrimary = Color(0xFF60B2E5)
private val VerdantSurfaceContainer = Color(0xFFEFF6EA)
private val VerdantOnSurface = Color(0xFF171D16)
private val VerdantOnSurfaceVariant = Color(0xFF3E4A3D)
