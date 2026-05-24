package com.mobile.travelhub.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.Send
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Tag
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import coil.compose.AsyncImage
import com.mobile.travelhub.R
import com.mobile.travelhub.data.model.FeedPostResponse
import com.mobile.travelhub.data.model.UserProfileResponse
import com.mobile.travelhub.ui.components.CommentItem
import com.mobile.travelhub.ui.components.FeedPostCard
import com.mobile.travelhub.ui.components.FeedPostCardSkeleton
import com.mobile.travelhub.ui.components.SimpleFormTextField
import com.mobile.travelhub.ui.components.modifiers.shimmerEffect
import com.mobile.travelhub.ui.theme.OnSurface
import com.mobile.travelhub.ui.theme.OnSurfaceVariant
import com.mobile.travelhub.ui.theme.OutlineVariant
import com.mobile.travelhub.ui.theme.PrimaryBlue
import com.mobile.travelhub.ui.theme.SurfaceBg
import com.mobile.travelhub.ui.theme.SurfaceContainer
import com.mobile.travelhub.utils.PostsUtils
import com.mobile.travelhub.viewmodels.HomeCommentUiModel
import com.mobile.travelhub.viewmodels.HomePostUiModel
import com.mobile.travelhub.viewmodels.SearchViewModel
import kotlinx.coroutines.delay

@Composable
fun SearchPage(
    onBack: () -> Unit,
    onUserClick: (Long) -> Unit,
    viewModel: SearchViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current
    val trendingSearches = listOf("#BeachVibes", "#MountainClimbing", "#CityBreaks", "#FoodTour")

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
        keyboardController?.show()
    }

    LaunchedEffect(uiState.query) {
        if (uiState.query.isBlank()) return@LaunchedEffect
        delay(350)
        viewModel.search(uiState.query)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SurfaceBg)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                    contentDescription = "Back",
                    tint = OnSurface
                )
            }
            SearchInput(
                query = uiState.query,
                onQueryChange = viewModel::updateQuery,
                onClear = { viewModel.updateQuery("") },
                modifier = Modifier
                    .weight(1f)
                    .focusRequester(focusRequester)
            )
        }

        if (uiState.query.isBlank()) {
            SearchSuggestionsContent(
                recentSearches = uiState.recentSearches,
                trendingSearches = trendingSearches,
                onRecentSearchClick = viewModel::applyRecentSearch,
                onRecentSearchRemove = viewModel::removeRecentSearch,
                onClearRecentSearches = viewModel::clearRecentSearches,
                onTrendingSearchClick = viewModel::applyRecentSearch
            )
        } else {
            CombinedSearchResults(
                query = uiState.query,
                users = uiState.users,
                posts = uiState.posts,
                followingRequestUserIds = uiState.followingRequestUserIds,
                likingPostIds = uiState.likingPostIds,
                isLoadingUsers = uiState.isLoadingUsers,
                isLoadingPosts = uiState.isLoadingPosts,
                usersErrorMessage = uiState.usersErrorMessage,
                postsErrorMessage = uiState.postsErrorMessage,
                onRetry = { viewModel.search(uiState.query) },
                onToggleFollow = viewModel::toggleFollow,
                onUserClick = onUserClick,
                onLikeClick = viewModel::onLikeClicked,
                onCommentClick = viewModel::onCommentClicked
            )
        }
    }

    if (uiState.activeCommentPostId != null) {
        SearchCommentsSheet(
            comments = uiState.commentsByPostId[uiState.activeCommentPostId].orEmpty(),
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

@Composable
private fun SearchSuggestionsContent(
    recentSearches: List<String>,
    trendingSearches: List<String>,
    onRecentSearchClick: (String) -> Unit,
    onRecentSearchRemove: (String) -> Unit,
    onClearRecentSearches: () -> Unit,
    onTrendingSearchClick: (String) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        if (recentSearches.isNotEmpty()) {
            item(contentType = "recent-title") {
                SearchSectionHeader(
                    text = "Recent Searches",
                    actionText = "Clear",
                    onActionClick = onClearRecentSearches
                )
            }
            items(
                items = recentSearches,
                key = { "recent-$it" },
                contentType = { "recent-search" }
            ) { search ->
                SearchSuggestionRow(
                    text = search,
                    subtitle = "",
                    leadingIcon = SearchLeadingIcon.History,
                    onClick = { onRecentSearchClick(search) },
                    onRemoveClick = { onRecentSearchRemove(search) }
                )
            }
        }

        item(contentType = "trending-title") {
            SearchSectionTitle(
                text = "Trending Searches",
                modifier = Modifier.padding(top = if (recentSearches.isEmpty()) 0.dp else 8.dp)
            )
        }
        items(
            items = trendingSearches,
            key = { "trending-$it" },
            contentType = { "trending-search" }
        ) { search ->
            SearchSuggestionRow(
                text = search,
                subtitle = "",
                leadingIcon = SearchLeadingIcon.Tag,
                onClick = { onTrendingSearchClick(search) }
            )
        }
    }
}

@Composable
private fun SearchInput(
    query: String,
    onQueryChange: (String) -> Unit,
    onClear: () -> Unit,
    modifier: Modifier = Modifier
) {
    BasicTextField(
        value = query,
        onValueChange = onQueryChange,
        singleLine = true,
        textStyle = MaterialTheme.typography.bodyMedium.copy(
            color = OnSurface,
            fontSize = 14.sp
        ),
        cursorBrush = SolidColor(PrimaryBlue),
        modifier = modifier
            .height(44.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFFEFF2FA)),
        decorationBox = { innerTextField ->
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Outlined.Search,
                    contentDescription = null,
                    tint = OnSurfaceVariant,
                    modifier = Modifier.size(19.dp)
                )
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 10.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    if (query.isEmpty()) {
                        Text(
                            text = "Search posts or users",
                            color = OnSurfaceVariant,
                            fontSize = 14.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    innerTextField()
                }
                if (query.isNotEmpty()) {
                    IconButton(
                        onClick = onClear,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Close,
                            contentDescription = "Clear search",
                            tint = OnSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    )
}
@Composable
private fun CombinedSearchResults(
    query: String,
    users: List<UserProfileResponse>,
    posts: List<FeedPostResponse>,
    followingRequestUserIds: Set<Long>,
    likingPostIds: Set<Long>,
    isLoadingUsers: Boolean,
    isLoadingPosts: Boolean,
    usersErrorMessage: String?,
    postsErrorMessage: String?,
    onRetry: () -> Unit,
    onToggleFollow: (UserProfileResponse) -> Unit,
    onUserClick: (Long) -> Unit,
    onLikeClick: (Long) -> Unit,
    onCommentClick: (Long) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(top = 12.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item(contentType = "users-carousel") {
            UserCarouselSection(
                query = query,
                users = users,
                followingRequestUserIds = followingRequestUserIds,
                isLoading = isLoadingUsers,
                errorMessage = usersErrorMessage,
                onRetry = onRetry,
                onToggleFollow = onToggleFollow,
                onUserClick = onUserClick
            )
        }

        item(contentType = "posts-title") {
            SearchSectionTitle(
                text = "Posts",
                modifier = Modifier.padding(start = 16.dp, top = 6.dp, end = 16.dp)
            )
        }

        when {
            isLoadingPosts -> items(3, contentType = { "post-skeleton" }) { FeedPostCardSkeleton() }
            postsErrorMessage != null -> item(contentType = "posts-error") {
                SearchErrorState(message = postsErrorMessage, onRetry = onRetry)
            }
            posts.isEmpty() -> item(contentType = "posts-empty") {
                EmptySearchState(query = query, resultType = "posts")
            }
            else -> items(
                items = posts,
                key = { it.id },
                contentType = { "post" }
            ) { post ->
                FeedPostCard(
                    post = post.toHomePostUiModel(isLikeLoading = post.id in likingPostIds),
                    onLikeClick = { onLikeClick(post.id) },
                    onCommentClick = { onCommentClick(post.id) }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SearchCommentsSheet(
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

        when {
            isCommentsLoading -> Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 24.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = PrimaryBlue)
            }
            !commentsErrorMessage.isNullOrBlank() -> Text(
                text = commentsErrorMessage,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(horizontal = 24.dp)
            )
            comments.isEmpty() -> Text(
                text = "No comments yet",
                style = MaterialTheme.typography.bodyMedium,
                color = OnSurfaceVariant,
                modifier = Modifier.padding(horizontal = 24.dp)
            )
            else -> LazyColumn(
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
                focusedIndicatorColor = PrimaryBlue
            )
            Spacer(modifier = Modifier.width(8.dp))
            IconButton(
                onClick = onCommentSubmit,
                enabled = !isCommentSubmitting && commentInput.isNotBlank(),
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(PrimaryBlue)
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
private fun UserCarouselSection(
    query: String,
    users: List<UserProfileResponse>,
    followingRequestUserIds: Set<Long>,
    isLoading: Boolean,
    errorMessage: String?,
    onRetry: () -> Unit,
    onToggleFollow: (UserProfileResponse) -> Unit,
    onUserClick: (Long) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        SearchSectionTitle(
            text = "Users",
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        when {
            isLoading -> {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(4, contentType = { "user-skeleton" }) {
                        UserCarouselCardSkeleton()
                    }
                }
            }
            errorMessage != null -> UserCarouselStatusCard(
                message = errorMessage,
                actionLabel = "Retry",
                onActionClick = onRetry
            )
            users.isEmpty() -> EmptySearchState(query = query, resultType = "users")
            else -> {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(
                        items = users,
                        key = { it.id },
                        contentType = { "user" }
                    ) { user ->
                        UserCarouselCard(
                            user = user,
                            isFollowLoading = user.id in followingRequestUserIds,
                            onFollowClick = { onToggleFollow(user) },
                            onClick = { onUserClick(user.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun UserCarouselStatusCard(
    message: String,
    actionLabel: String? = null,
    onActionClick: (() -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(96.dp)
            .padding(horizontal = 16.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(Color.White)
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        SearchIconBubble(type = SearchLeadingIcon.User)
        Text(
            text = message,
            modifier = Modifier
                .weight(1f)
                .padding(start = 12.dp, end = 8.dp),
            color = OnSurfaceVariant,
            style = MaterialTheme.typography.bodyMedium,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
        if (actionLabel != null && onActionClick != null) {
            TextButton(onClick = onActionClick) {
                Text(text = actionLabel, color = PrimaryBlue)
            }
        }
    }
}

@Composable
private fun UserCarouselCardSkeleton() {
    Column(
        modifier = Modifier
            .width(156.dp)
            .height(188.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(Color.White)
            .padding(horizontal = 12.dp, vertical = 14.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(66.dp)
                .clip(CircleShape)
                .shimmerEffect()
        )
        Spacer(modifier = Modifier.height(12.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth(0.78f)
                .height(16.dp)
                .clip(RoundedCornerShape(6.dp))
                .shimmerEffect()
        )
        Spacer(modifier = Modifier.height(8.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth(0.58f)
                .height(13.dp)
                .clip(RoundedCornerShape(6.dp))
                .shimmerEffect()
        )
        Spacer(modifier = Modifier.height(14.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(34.dp)
                .clip(RoundedCornerShape(18.dp))
                .shimmerEffect()
        )
    }
}

@Composable
private fun SearchSectionTitle(
    text: String,
    modifier: Modifier = Modifier
) {
    Text(
        text = text,
        color = OnSurfaceVariant,
        style = MaterialTheme.typography.labelLarge,
        fontWeight = FontWeight.Bold,
        modifier = modifier.padding(top = 4.dp, bottom = 4.dp)
    )
}

@Composable
private fun SearchSectionHeader(
    text: String,
    actionText: String,
    onActionClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 4.dp, bottom = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = text,
            color = OnSurfaceVariant,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold
        )
        TextButton(
            onClick = onActionClick,
            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp),
            modifier = Modifier.height(32.dp)
        ) {
            Text(
                text = actionText,
                color = PrimaryBlue,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun SearchSuggestionRow(
    text: String,
    subtitle: String,
    leadingIcon: SearchLeadingIcon,
    onClick: () -> Unit,
    onRemoveClick: (() -> Unit)? = null
) {
    SearchRowContainer(onClick = onClick) {
        SearchIconBubble(type = leadingIcon)
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(start = 12.dp)
        ) {
            Text(
                text = text,
                color = OnSurface,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        if (onRemoveClick != null) {
            IconButton(
                onClick = onRemoveClick,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.Close,
                    contentDescription = "Remove recent search",
                    tint = OnSurfaceVariant,
                    modifier = Modifier.size(17.dp)
                )
            }
        }
    }
}

@Composable
private fun UserCarouselCard(
    user: UserProfileResponse,
    isFollowLoading: Boolean,
    onFollowClick: () -> Unit,
    onClick: () -> Unit
) {
    val title = user.name.takeIf { it.isNotBlank() } ?: user.username
    val metadata = buildString {
        append(formatFollowerCount(user.followersCount))
        append(" follower")
        if (user.followersCount != 1) append("s")
    }

    Column(
        modifier = Modifier
            .width(156.dp)
            .height(188.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(Color.White)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 14.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        UserResultAvatar(
            avatarUrl = user.avatarUrl,
            name = title
        )
        Spacer(modifier = Modifier.height(10.dp))
        Text(
            text = title,
            color = OnSurface,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Spacer(modifier = Modifier.height(3.dp))
        Text(
            text = metadata,
            color = OnSurfaceVariant,
            style = MaterialTheme.typography.bodySmall,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Spacer(modifier = Modifier.weight(1f))
        FollowButton(
            isFollowing = user.isFollowing,
            isLoading = isFollowLoading,
            onClick = onFollowClick
        )
    }
}

@Composable
private fun UserResultAvatar(
    avatarUrl: String?,
    name: String
) {
    Box(
        modifier = Modifier
            .size(66.dp)
            .clip(CircleShape)
            .border(2.dp, PrimaryBlue, CircleShape)
            .padding(3.dp)
            .clip(CircleShape)
            .background(Color(0xFFEFF2FA)),
        contentAlignment = Alignment.Center
    ) {
        if (avatarUrl.isNullOrBlank()) {
            Icon(
                imageVector = Icons.Outlined.Person,
                contentDescription = null,
                tint = OnSurfaceVariant,
                modifier = Modifier.size(34.dp)
            )
        } else {
            AsyncImage(
                model = avatarUrl,
                contentDescription = "$name avatar",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

@Composable
private fun FollowButton(
    isFollowing: Boolean,
    isLoading: Boolean,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        enabled = !isLoading,
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isFollowing) SurfaceContainer else PrimaryBlue,
            contentColor = if (isFollowing) OnSurface else Color.White,
            disabledContainerColor = if (isFollowing) SurfaceContainer else PrimaryBlue.copy(alpha = 0.62f),
            disabledContentColor = if (isFollowing) OnSurfaceVariant else Color.White.copy(alpha = 0.82f)
        ),
        shape = RoundedCornerShape(18.dp),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 0.dp),
        modifier = Modifier
            .height(38.dp)
            .widthIn(min = 104.dp)
    ) {
        Text(
            text = when {
                isLoading -> "..."
                isFollowing -> "Following"
                else -> "Follow"
            },
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            maxLines = 1
        )
    }
}

@Composable
private fun SearchRowContainer(
    onClick: () -> Unit,
    content: @Composable RowScope.() -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(40.dp)
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        content = content
    )
}

@Composable
private fun SearchIconBubble(type: SearchLeadingIcon) {
    val icon = when (type) {
        SearchLeadingIcon.History -> Icons.Outlined.History
        SearchLeadingIcon.Tag -> Icons.Outlined.Tag
        SearchLeadingIcon.User -> Icons.Outlined.Person
    }
    Box(
        modifier = Modifier
            .size(22.dp),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = PrimaryBlue,
            modifier = Modifier.size(18.dp)
        )
    }
}

@Composable
private fun LoadingSearchState() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 48.dp),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(
            color = PrimaryBlue,
            modifier = Modifier.size(32.dp)
        )
    }
}

@Composable
private fun SearchErrorState(
    message: String,
    onRetry: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 48.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = message,
            color = OnSurface,
            style = MaterialTheme.typography.bodyMedium
        )
        TextButton(onClick = onRetry) {
            Text(text = "Retry", color = PrimaryBlue)
        }
    }
}

@Composable
private fun EmptySearchState(
    query: String,
    resultType: String
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 48.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(54.dp)
                .clip(CircleShape)
                .background(Color(0xFFEFF2FA))
                .border(1.dp, OutlineVariant, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Outlined.Search,
                contentDescription = null,
                tint = OnSurfaceVariant,
                modifier = Modifier.size(24.dp)
            )
        }
        Text(
            text = "No $resultType for \"$query\"",
            modifier = Modifier.padding(top = 14.dp),
            color = OnSurface,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Try another keyword.",
            modifier = Modifier.padding(top = 4.dp),
            color = OnSurfaceVariant,
            fontSize = 13.sp
        )
    }
}

private enum class SearchLeadingIcon {
    History,
    Tag,
    User
}

private fun formatFollowerCount(count: Int): String {
    val safeCount = count.coerceAtLeast(0)
    return when {
        safeCount >= 1_000_000 -> {
            val millions = safeCount / 1_000_000f
            "${"%.1f".format(millions).trimEnd('0').trimEnd('.')}M"
        }
        safeCount >= 1_000 -> {
            val thousands = safeCount / 1_000f
            "${"%.1f".format(thousands).trimEnd('0').trimEnd('.')}K"
        }
        else -> safeCount.toString()
    }
}

private fun FeedPostResponse.toHomePostUiModel(isLikeLoading: Boolean): HomePostUiModel {
    val safeCreatedAt = createdAt ?: updatedAt

    return HomePostUiModel(
        id = id,
        username = owner.username.takeIf { it.isNotBlank() } ?: "unknown",
        subtitle = location?.takeIf { it.isNotBlank() } ?: "STUDIO NULL",
        description = description.takeIf { it.isNotBlank() } ?: "",
        imageUrls = imageUrls.filter { it.isNotBlank() },
        likeCount = likeCount?.coerceAtLeast(0) ?: 0,
        commentCount = commentCount?.coerceAtLeast(0) ?: 0,
        isLiked = likedByCurrentUser == true,
        isLikeLoading = isLikeLoading,
        timeAgoLabel = PostsUtils.formatTimeAgo(safeCreatedAt)
    )
}
