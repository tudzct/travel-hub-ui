package com.mobile.travelhub.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
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
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Send
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Place
import androidx.compose.material.icons.outlined.Search
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
import androidx.compose.ui.zIndex
import coil.compose.AsyncImage
import com.mobile.travelhub.R
import com.mobile.travelhub.data.model.TravelPlaceListItemResponse
import com.mobile.travelhub.ui.components.modifiers.shimmerEffect
import com.mobile.travelhub.ui.components.layout.MainMenuButton
import com.mobile.travelhub.ui.components.CommentSubmitAction
import com.mobile.travelhub.viewmodels.HomeCommentUiModel
import com.mobile.travelhub.viewmodels.HomePostUiModel
import com.mobile.travelhub.viewmodels.HomeUiState
import com.mobile.travelhub.viewmodels.PlaceListUiState
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.mobile.travelhub.ui.theme.PrimaryBlue

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlaceListScreenContent(
    placeUiState: PlaceListUiState,
    homeUiState: HomeUiState,
    listState: LazyListState,
    onPlaceClick: (TravelPlaceListItemResponse) -> Unit,
    onMenuClick: () -> Unit,
    onSearchClick: () -> Unit,
    onRetryPlaces: () -> Unit,
    onRetryPosts: () -> Unit,
    onLoadMorePosts: () -> Unit,
    onLikeClick: (Long) -> Unit,
    onSaveClick: (Long) -> Unit,
    onCommentClick: (Long) -> Unit,
    onAuthorClick: (Long) -> Unit,
    onDismissCommentSheet: () -> Unit,
    onCommentInputChanged: (String) -> Unit,
    onCommentSubmit: () -> Unit
) {
    val activeCommentPost = homeUiState.posts.firstOrNull { it.id == homeUiState.activeCommentPostId }
    val activeComments = homeUiState.activeCommentPostId
        ?.let { homeUiState.commentsByPostId[it] }
        .orEmpty()
    var previousScrollIndex by remember { mutableIntStateOf(0) }
    var previousScrollOffset by remember { mutableIntStateOf(0) }
    var isTopBarVisible by remember { mutableStateOf(true) }
    val topBarContentHeight = 44.dp
    val topBarTopPadding = 4.dp
    val topBarContentGap = 10.dp

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
            .background(MaterialTheme.colorScheme.surface)
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
            modifier = Modifier
                .align(Alignment.TopCenter)
                .zIndex(1f)
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
                top = topBarTopPadding + topBarContentHeight + topBarContentGap,
                bottom = 112.dp
            ),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            when {
                placeUiState.isLoading -> {
                    item {
                        LocationsRailSection {
                            LocationsRailSkeleton()
                        }
                    }
                }

                placeUiState.errorMessage != null && placeUiState.items.isEmpty() -> {
                    item {
                        FeedEmptyState(
                            title = stringResource(R.string.ui_3682846f79),
                            message = placeUiState.errorMessage.orEmpty(),
                            fullScreen = false,
                            onRetry = onRetryPlaces
                        )
                    }
                }

                placeUiState.items.isEmpty() -> {
                    item {
                        FeedEmptyState(
                            title = stringResource(R.string.ui_cfc854b8a2),
                            message = "Dữ liệu địa điểm vẫn chưa được thêm vào hệ thống.",
                            fullScreen = false,
                            onRetry = null
                        )
                    }
                }

                else -> {
                    item {
                        LocationsRailSection {
                            LocationsRail(
                                places = placeUiState.items.take(10),
                                onPlaceClick = onPlaceClick
                            )
                        }
                    }
                }
            }

            item(key = "locations-posts-spacer") {
                Spacer(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(4.dp)
                        .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f))
                )
            }

            when {
                homeUiState.isLoading -> {
                    items(3) {
                        FeedPostCardSkeleton()
                    }
                }

                homeUiState.errorMessage != null && homeUiState.posts.isEmpty() -> {
                    item {
                        FeedEmptyState(
                            title = stringResource(R.string.ui_d03cf884c0),
                            message = homeUiState.errorMessage ?: "Không thể tải bài viết",
                            fullScreen = false,
                            onRetry = onRetryPosts
                        )
                    }
                }

                else -> {
                    itemsIndexed(
                        items = homeUiState.posts,
                        key = { _, post -> post.id }
                    ) { _, post ->
                        FeedPostCard(
                            post = post,
                            onLikeClick = { onLikeClick(post.id) },
                            onSaveClick = { onSaveClick(post.id) },
                            onCommentClick = { onCommentClick(post.id) },
                            onAuthorClick = { onAuthorClick(post.ownerId) }
                        )
                    }

                    if (homeUiState.isLoadingMore) {
                        item(key = "posts-loading-more") {
                            FeedPostCardSkeleton()
                        }
                    } else if (!homeUiState.loadMoreErrorMessage.isNullOrBlank()) {
                        item(key = "posts-load-more-error") {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 12.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = homeUiState.loadMoreErrorMessage,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.error
                                )
                                RetryButton(onClick = onLoadMorePosts)
                            }
                        }
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
                onCommentSubmit = onCommentSubmit,
                onAuthorClick = { userId ->
                    onDismissCommentSheet()
                    onAuthorClick(userId)
                }
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
    onCommentSubmit: () -> Unit,
    onAuthorClick: (Long) -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Text(
            text = stringResource(R.string.ui_fce06e20e5),
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(horizontal = 24.dp)
        )
        Spacer(modifier = Modifier.height(12.dp))

        if (isCommentsLoading) {
            LoadingListSkeleton(itemCount = 3)
        } else if (!commentsErrorMessage.isNullOrBlank()) {
            Text(
                text = commentsErrorMessage,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(horizontal = 24.dp)
            )
        } else if (comments.isEmpty()) {
            Text(
                text = stringResource(R.string.ui_d14da37946),
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
                        avatarUrl = comment.avatarUrl,
                        avatarRes = R.drawable.female_avatar_maker,
                        onAuthorClick = { onAuthorClick(comment.ownerId) }
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
                placeholder = stringResource(R.string.ui_3e18361540),
                modifier = Modifier.weight(1f),
                enabled = !isCommentSubmitting,
                singleLine = false,
                maxLines = 3
            )
            Spacer(modifier = Modifier.width(8.dp))
            CommentSubmitAction(
                isSubmitting = isCommentSubmitting,
                canSubmit = commentInput.isNotBlank(),
                onSubmit = onCommentSubmit
            )
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

    }
}

@Composable
private fun FeedTopBar(
    onMenuClick: () -> Unit,
    onSearchClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val topBarContentHeight = 44.dp

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .padding(top = 4.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(topBarContentHeight)
                .padding(horizontal = 6.dp)
        ) {
//            MainMenuButton(
//                onClick = onMenuClick,
//                modifier = Modifier.align(Alignment.CenterStart)
//            )
            Text(
                text = stringResource(R.string.ui_a59ca6e82e),
                modifier = Modifier.align(Alignment.Center),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.ExtraBold
            )

            IconButton(
                onClick = onSearchClick,
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .size(40.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.Search,
                    contentDescription = stringResource(R.string.ui_bce0641417),
                    tint = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.size(22.dp)
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
private fun LocationsRailSection(
    content: @Composable () -> Unit
) {
    Column {
        Text(
            text = stringResource(R.string.you_might_like),
            modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 10.dp),
            color = MaterialTheme.colorScheme.onSurface,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.ExtraBold
        )
        content()
    }
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
                averageRating = place.averageRating,
                reviewCount = place.reviewCount,
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
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (onRetry != null) {
                    RetryButton(onClick = onRetry)
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
    onSaveClick: () -> Unit,
    onCommentClick: () -> Unit,
    onAuthorClick: (() -> Unit)? = null,
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
    val authorClick = onAuthorClick

    Column(modifier = Modifier.background(MaterialTheme.colorScheme.surface)) {

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier
                    .weight(1f)
                    .then(
                        if (authorClick != null) {
                            Modifier.clickable(onClick = authorClick)
                        } else {
                            Modifier
                        }
                    ),
                verticalAlignment = Alignment.CenterVertically
            ) {
                AsyncImage(
                    model = post.ownerAvatarUrl?.let(::toDisplayUrl),
                    placeholder = painterResource(R.drawable.female_avatar_maker),
                    error = painterResource(R.drawable.female_avatar_maker),
                    fallback = painterResource(R.drawable.female_avatar_maker),
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
                        color = MaterialTheme.colorScheme.onSurface,
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
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.width(2.dp))
                        Text(
                            text = post.subtitle,
                            modifier = Modifier
                                .weight(1f)
                                .basicMarquee(),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.width(4.dp))

            Text(
                text = post.timeAgoLabel,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )
        }

        ExpandableDescription(
            description = post.description,
            modifier = Modifier.padding(horizontal = 16.dp),
            title = null,
            collapsedMaxLines = 3,
            textStyle = MaterialTheme.typography.bodyMedium,
            textColor = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(12.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(5f / 3f)
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f))
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
                                                MaterialTheme.colorScheme.onSurface
                                            } else {
                                                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                            }
                                        )
                                )
                    }
                }
            }
        }

        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    IconButton(
                        onClick = onLikeClick,
                        enabled = actionsEnabled && !post.isLikeLoading,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = if (post.isLiked) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                            contentDescription = stringResource(R.string.ui_c7e02c95fe),
                            tint = if (post.isLiked) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(26.dp)
                        )
                    }
                    Text(
                        text = stringResource(R.string.like_count, post.likeCount),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    IconButton(
                        onClick = onCommentClick,
                        enabled = actionsEnabled,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.message_circle),
                            contentDescription = stringResource(R.string.ui_153d7a58b3),
                            modifier = Modifier.size(24.dp),
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    Text(
                        text = stringResource(R.string.comment_count, post.commentCount),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    IconButton(
                        onClick = onSaveClick,
                        enabled = actionsEnabled && !post.isSaveLoading,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = if (post.isSaved) Icons.Filled.Bookmark else Icons.Outlined.BookmarkBorder,
                            contentDescription = if (post.isSaved) "Unsave" else "Save",
                            modifier = Modifier.size(26.dp),
                            tint = if (post.isSaved) PrimaryBlue else MaterialTheme.colorScheme.onSurface
                        )
                    }
                    Text(
                        text = stringResource(R.string.save_count, post.saveCount),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }

        Spacer(
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp)
                .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f))
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
                verticalAlignment = Alignment.Top
            ) {
                repeat(2) {
                    Column(
                        modifier = Modifier.weight(1f),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(26.dp)
                                .clip(CircleShape)
                                .shimmerEffect()
                        )
                        Box(
                            modifier = Modifier
                                .width(72.dp)
                                .height(10.dp)
                                .clip(RoundedCornerShape(50))
                                .shimmerEffect()
                        )
                    }
                }
                Box(
                    modifier = Modifier.weight(1f),
                    contentAlignment = Alignment.TopCenter
                ) {
                    Box(
                        modifier = Modifier
                            .size(26.dp)
                            .clip(CircleShape)
                            .shimmerEffect()
                    )
                }
            }
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
                .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f))
        )
    }
}

@Preview
@Composable
fun FeedPostCardPreview() {
    FeedPostCard(
        post = HomePostUiModel(
            id = 1L,
            ownerId = 2L,
            username = "Duc Duong Hoang",
            ownerAvatarUrl = null,
            subtitle = stringResource(R.string.ui_8d95e76955),
            description = "A calm afternoon by the river.",
            imageUrls = listOf("sample.jpg"),
            likeCount = 142,
            commentCount = 4,
            saveCount = 28,
            isLiked = true,
            isLikeLoading = false,
            isSaved = false,
            isSaveLoading = false,
            timeAgoLabel = "2h"
        ),
        onLikeClick = {},
        onSaveClick = {},
        onCommentClick = {}
    )
}


private val VerdantPrimary = Color(0xFF1677F2)
