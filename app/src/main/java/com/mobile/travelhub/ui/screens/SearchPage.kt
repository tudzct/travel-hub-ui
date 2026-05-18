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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Tag
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import com.mobile.travelhub.data.model.FeedPostResponse
import com.mobile.travelhub.data.model.UserProfileResponse
import com.mobile.travelhub.ui.components.FeedPostCard
import com.mobile.travelhub.ui.components.FeedPostCardSkeleton
import com.mobile.travelhub.ui.theme.OnSurface
import com.mobile.travelhub.ui.theme.OnSurfaceVariant
import com.mobile.travelhub.ui.theme.OutlineVariant
import com.mobile.travelhub.ui.theme.PrimaryBlue
import com.mobile.travelhub.ui.theme.SurfaceBg
import com.mobile.travelhub.ui.theme.SurfaceContainer
import com.mobile.travelhub.utils.PostsUtils
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
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current
    val recentSearches = listOf("Bali", "Paris", "Tokyo", "New York")
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
            SearchSuggestions(
                recentSearches = recentSearches,
                trendingSearches = trendingSearches,
                onSuggestionClick = viewModel::updateQuery
            )
        } else {
            SearchTabs(
                selectedTabIndex = selectedTabIndex,
                onTabSelected = { selectedTabIndex = it }
            )

            when (selectedTabIndex) {
                0 -> PostResults(
                    query = uiState.query,
                    posts = uiState.posts,
                    isLoading = uiState.isLoadingPosts,
                    errorMessage = uiState.postsErrorMessage,
                    onRetry = { viewModel.search(uiState.query) }
                )
                else -> UserResults(
                    query = uiState.query,
                    users = uiState.users,
                    followingRequestUserIds = uiState.followingRequestUserIds,
                    isLoading = uiState.isLoadingUsers,
                    errorMessage = uiState.usersErrorMessage,
                    onRetry = { viewModel.search(uiState.query) },
                    onToggleFollow = viewModel::toggleFollow,
                    onUserClick = onUserClick
                )
            }
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
private fun SearchSuggestions(
    recentSearches: List<String>,
    trendingSearches: List<String>,
    onSuggestionClick: (String) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item { SearchSectionTitle("Recent Searches") }
        items(recentSearches) { text ->
            SearchSuggestionRow(
                text = text,
                subtitle = "Search again",
                leadingIcon = SearchLeadingIcon.History,
                onClick = { onSuggestionClick(text) }
            )
        }
        item {
            Spacer(modifier = Modifier.height(10.dp))
            SearchSectionTitle("Trending Now")
        }
        items(trendingSearches) { text ->
            SearchSuggestionRow(
                text = text,
                subtitle = "Explore posts and users",
                leadingIcon = SearchLeadingIcon.Tag,
                onClick = { onSuggestionClick(text.removePrefix("#")) }
            )
        }
    }
}

@Composable
private fun SearchTabs(
    selectedTabIndex: Int,
    onTabSelected: (Int) -> Unit
) {
    val tabs = listOf("Posts", "Users")
    TabRow(
        selectedTabIndex = selectedTabIndex,
        containerColor = SurfaceBg,
        contentColor = PrimaryBlue,
        indicator = { tabPositions ->
            TabRowDefaults.SecondaryIndicator(
                modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
                color = PrimaryBlue
            )
        },
        divider = {}
    ) {
        tabs.forEachIndexed { index, title ->
            Tab(
                selected = selectedTabIndex == index,
                onClick = { onTabSelected(index) },
                text = {
                    Text(
                        text = title.uppercase(),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = if (selectedTabIndex == index) FontWeight.Bold else FontWeight.Normal,
                        color = if (selectedTabIndex == index) PrimaryBlue else OnSurfaceVariant
                    )
                }
            )
        }
    }
}

@Composable
private fun PostResults(
    query: String,
    posts: List<FeedPostResponse>,
    isLoading: Boolean,
    errorMessage: String?,
    onRetry: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(top = 12.dp, bottom = 24.dp)
    ) {
        when {
            isLoading -> items(3) { FeedPostCardSkeleton() }
            errorMessage != null -> item { SearchErrorState(message = errorMessage, onRetry = onRetry) }
            posts.isEmpty() -> item { EmptySearchState(query = query, resultType = "posts") }
            else -> items(posts, key = { it.id }) { post ->
                FeedPostCard(
                    post = post.toHomePostUiModel(),
                    onLikeClick = {},
                    onCommentClick = {},
                    actionsEnabled = false
                )
            }
        }
    }
}

@Composable
private fun UserResults(
    query: String,
    users: List<UserProfileResponse>,
    followingRequestUserIds: Set<Long>,
    isLoading: Boolean,
    errorMessage: String?,
    onRetry: () -> Unit,
    onToggleFollow: (UserProfileResponse) -> Unit,
    onUserClick: (Long) -> Unit
) {
    SearchResultList {
        when {
            isLoading -> item { LoadingSearchState() }
            errorMessage != null -> item { SearchErrorState(message = errorMessage, onRetry = onRetry) }
            users.isEmpty() -> item { EmptySearchState(query = query, resultType = "users") }
            else -> items(users, key = { it.id }) { user ->
                UserSearchResultRow(
                    user = user,
                    isFollowLoading = user.id in followingRequestUserIds,
                    onFollowClick = { onToggleFollow(user) },
                    onClick = { onUserClick(user.id) }
                )
            }
        }
    }
}

@Composable
private fun SearchResultList(
    content: androidx.compose.foundation.lazy.LazyListScope.() -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        content = content
    )
}

@Composable
private fun SearchSectionTitle(text: String) {
    Text(
        text = text,
        color = OnSurfaceVariant,
        style = MaterialTheme.typography.labelLarge,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(top = 4.dp, bottom = 4.dp)
    )
}

@Composable
private fun SearchSuggestionRow(
    text: String,
    subtitle: String,
    leadingIcon: SearchLeadingIcon,
    onClick: () -> Unit
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
    }
}

@Composable
private fun UserSearchResultRow(
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

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        UserResultAvatar(
            avatarUrl = user.avatarUrl,
            name = title
        )
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(start = 14.dp, end = 12.dp)
        ) {
            Text(
                text = title,
                color = OnSurface,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = metadata,
                color = OnSurfaceVariant,
                style = MaterialTheme.typography.bodySmall,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
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
            .height(32.dp)
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

private fun FeedPostResponse.toHomePostUiModel(): HomePostUiModel {
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
        isLikeLoading = false,
        timeAgoLabel = PostsUtils.formatTimeAgo(safeCreatedAt)
    )
}
