package com.mobile.travelhub.ui.screens

import androidx.compose.ui.res.stringResource
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.Send
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Tag
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
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.mobile.travelhub.R
import com.mobile.travelhub.data.model.FeedPostResponse
import com.mobile.travelhub.data.model.TravelPlaceListItemResponse
import com.mobile.travelhub.data.model.UserProfileResponse
import com.mobile.travelhub.ui.components.CommentItem
import com.mobile.travelhub.ui.components.CommentSubmitAction
import com.mobile.travelhub.ui.components.FeaturedLocationCard
import com.mobile.travelhub.ui.components.FeedPostCard
import com.mobile.travelhub.ui.components.FeedPostCardSkeleton
import com.mobile.travelhub.ui.components.SimpleFormTextField
import com.mobile.travelhub.ui.components.LoadingContentSkeleton
import com.mobile.travelhub.ui.components.LoadingListSkeleton
import com.mobile.travelhub.ui.components.RetryButton
import com.mobile.travelhub.ui.components.SearchBar
import com.mobile.travelhub.ui.components.SkeletonBlock
import com.mobile.travelhub.ui.components.UserResultCard
import com.mobile.travelhub.ui.components.UserResultCardSkeleton
import com.mobile.travelhub.utils.PostsUtils
import com.mobile.travelhub.viewmodels.HomeCommentUiModel
import com.mobile.travelhub.viewmodels.HomePostUiModel
import com.mobile.travelhub.viewmodels.SearchViewModel
import kotlinx.coroutines.delay

@Composable
fun SearchPage(
    onBack: () -> Unit,
    onUserClick: (Long) -> Unit,
    onPlaceClick: (TravelPlaceListItemResponse) -> Unit,
    viewModel: SearchViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current

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
            .background(MaterialTheme.colorScheme.background)
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
                    contentDescription = stringResource(R.string.ui_b52b36b726),
                    tint = MaterialTheme.colorScheme.onSurface
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
                onRecentSearchClick = viewModel::applyRecentSearch,
                onRecentSearchRemove = viewModel::removeRecentSearch,
                onClearRecentSearches = viewModel::clearRecentSearches,
                onTrendingSearchClick = viewModel::applyRecentSearch
            )
        } else {
            CombinedSearchResults(
                query = uiState.query,
                users = uiState.users,
                places = uiState.places,
                posts = uiState.posts,
                followingRequestUserIds = uiState.followingRequestUserIds,
                likingPostIds = uiState.likingPostIds,
                savingPostIds = uiState.savingPostIds,
                isLoadingUsers = uiState.isLoadingUsers,
                isLoadingPlaces = uiState.isLoadingPlaces,
                isLoadingMorePlaces = uiState.isLoadingMorePlaces,
                isLoadingPosts = uiState.isLoadingPosts,
                usersErrorMessage = uiState.usersErrorMessage,
                placesErrorMessage = uiState.placesErrorMessage,
                placesLoadMoreErrorMessage = uiState.placesLoadMoreErrorMessage,
                postsErrorMessage = uiState.postsErrorMessage,
                hasMorePlaces = uiState.placesPage + 1 < uiState.placesTotalPages,
                onRetry = { viewModel.search(uiState.query) },
                onLoadMorePlaces = viewModel::loadMorePlaces,
                onToggleFollow = viewModel::toggleFollow,
                onUserClick = onUserClick,
                onPlaceClick = onPlaceClick,
                onLikeClick = viewModel::onLikeClicked,
                onSaveClick = viewModel::onSaveClicked,
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
            onCommentSubmit = viewModel::submitComment,
            onAuthorClick = { userId ->
                viewModel.onCommentDismissed()
                onUserClick(userId)
            }
        )
    }
}

@Composable
private fun SearchSuggestionsContent(
    recentSearches: List<String>,
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
                    text = stringResource(R.string.ui_5820a93677),
                    actionText = "Xóa tất cả",
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

//        item(contentType = "trending-title") {
//            SearchSectionTitle(
//                text = stringResource(R.string.ui_3a69c31c04),
//                modifier = Modifier.padding(top = if (recentSearches.isEmpty()) 0.dp else 8.dp)
//            )
//        }
//        items(
//            items = trendingSearches,
//            key = { "trending-$it" },
//            contentType = { "trending-search" }
//        ) { search ->
//            SearchSuggestionRow(
//                text = search,
//                subtitle = "",
//                leadingIcon = SearchLeadingIcon.Tag,
//                onClick = { onTrendingSearchClick(search) }
//            )
//        }
    }
}

@Composable
private fun SearchInput(
    query: String,
    onQueryChange: (String) -> Unit,
    onClear: () -> Unit,
    modifier: Modifier = Modifier
) {
    SearchBar(
        value = query,
        onValueChange = onQueryChange,
        placeholder = stringResource(R.string.ui_e87eee7e22),
        modifier = modifier,
        trailingContent = if (query.isNotEmpty()) {
            {
                IconButton(
                    onClick = onClear,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Close,
                        contentDescription = stringResource(R.string.ui_67300d0fed),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        } else {
            null
        }
    )
}
@Composable
private fun CombinedSearchResults(
    query: String,
    users: List<UserProfileResponse>,
    places: List<TravelPlaceListItemResponse>,
    posts: List<FeedPostResponse>,
    followingRequestUserIds: Set<Long>,
    likingPostIds: Set<Long>,
    savingPostIds: Set<Long>,
    isLoadingUsers: Boolean,
    isLoadingPlaces: Boolean,
    isLoadingMorePlaces: Boolean,
    isLoadingPosts: Boolean,
    usersErrorMessage: String?,
    placesErrorMessage: String?,
    placesLoadMoreErrorMessage: String?,
    postsErrorMessage: String?,
    hasMorePlaces: Boolean,
    onRetry: () -> Unit,
    onLoadMorePlaces: () -> Unit,
    onToggleFollow: (UserProfileResponse) -> Unit,
    onUserClick: (Long) -> Unit,
    onPlaceClick: (TravelPlaceListItemResponse) -> Unit,
    onLikeClick: (Long) -> Unit,
    onSaveClick: (Long) -> Unit,
    onCommentClick: (Long) -> Unit
) {
    val showCombinedEmptyState =
        !isLoadingPlaces &&
            !isLoadingMorePlaces &&
            !isLoadingUsers &&
            !isLoadingPosts &&
            placesErrorMessage == null &&
            usersErrorMessage == null &&
            postsErrorMessage == null &&
            places.isEmpty() &&
            users.isEmpty() &&
            posts.isEmpty()

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(top = 12.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        if (showCombinedEmptyState) {
            item(contentType = "combined-empty") {
                Box(
                    modifier = Modifier.padding(16.dp)
                ) {
                    EmptySearchState(
                        query = query,
                        resultType = stringResource(R.string.result_type_combined_search)
                    )
                }
            }
        } else {
            item(contentType = "places-carousel") {
                PlaceCarouselSection(
                    query = query,
                    places = places,
                    isLoading = isLoadingPlaces,
                    isLoadingMore = isLoadingMorePlaces,
                    errorMessage = placesErrorMessage,
                    loadMoreErrorMessage = placesLoadMoreErrorMessage,
                    hasMore = hasMorePlaces,
                    onRetry = onRetry,
                    onLoadMore = onLoadMorePlaces,
                    onPlaceClick = onPlaceClick
                )
            }

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
                    text = stringResource(R.string.ui_a0ca0c3198),
                    modifier = Modifier.padding(start = 16.dp, top = 6.dp, end = 16.dp)
                )
            }

            when {
                isLoadingPosts -> items(3, contentType = { "post-skeleton" }) { FeedPostCardSkeleton() }
                postsErrorMessage != null -> item(contentType = "posts-error") {
                    SearchErrorState(message = postsErrorMessage, onRetry = onRetry)
                }
                posts.isEmpty() -> item(contentType = "posts-empty") {
                    EmptySearchState(
                        query = query,
                        resultType = stringResource(R.string.result_type_posts)
                    )
                }
                else -> items(
                    items = posts,
                    key = { it.id },
                    contentType = { "post" }
                ) { post ->
                    FeedPostCard(
                        post = post.toHomePostUiModel(
                            isLikeLoading = post.id in likingPostIds,
                            isSaveLoading = post.id in savingPostIds
                        ),
                        onLikeClick = { onLikeClick(post.id) },
                        onSaveClick = { onSaveClick(post.id) },
                        onCommentClick = { onCommentClick(post.id) },
                        onAuthorClick = { onUserClick(post.owner.id) },
                        showBottomDivider = false
                    )
                }
            }
        }
    }
}

@Composable
private fun PlaceCarouselSection(
    query: String,
    places: List<TravelPlaceListItemResponse>,
    isLoading: Boolean,
    isLoadingMore: Boolean,
    errorMessage: String?,
    loadMoreErrorMessage: String?,
    hasMore: Boolean,
    onRetry: () -> Unit,
    onLoadMore: () -> Unit,
    onPlaceClick: (TravelPlaceListItemResponse) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        SearchSectionTitle(
            text = "Địa điểm",
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        when {
            isLoading -> {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(4, contentType = { "place-skeleton" }) {
                        SearchPlaceSkeletonCard()
                    }
                }
            }
            errorMessage != null -> UserCarouselStatusCard(
                message = errorMessage,
                onActionClick = onRetry
            )
            places.isEmpty() -> EmptySearchState(
                query = query,
                resultType = "địa điểm"
            )
            else -> {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(
                        items = places,
                        key = { it.id },
                        contentType = { "place" }
                    ) { place ->
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
                    if (isLoadingMore) {
                        item(contentType = "place-loading-more") {
                            SearchPlaceSkeletonCard()
                        }
                    } else if (loadMoreErrorMessage != null) {
                        item(contentType = "place-load-more-error") {
                            PlaceCarouselStatusCard(
                                message = loadMoreErrorMessage,
                                onActionClick = onLoadMore
                            )
                        }
                    } else if (hasMore) {
                        item(contentType = "place-load-trigger") {
                            LaunchedEffect(query, places.size) {
                                onLoadMore()
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SearchPlaceSkeletonCard() {
    SkeletonBlock(
        modifier = Modifier
            .width(220.dp)
            .height(270.dp),
        shape = RoundedCornerShape(10.dp)
    )
}

@Composable
private fun PlaceCarouselStatusCard(
    message: String,
    onActionClick: (() -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .width(260.dp)
            .height(96.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surface)
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        SearchIconBubble(type = SearchLeadingIcon.Place)
        Text(
            text = message,
            modifier = Modifier
                .weight(1f)
                .padding(start = 12.dp, end = 8.dp),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodyMedium,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
        if (onActionClick != null) {
            RetryButton(onClick = onActionClick)
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

        when {
            isCommentsLoading -> Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 24.dp),
                contentAlignment = Alignment.Center
            ) {
                LoadingListSkeleton(itemCount = 3)
            }
            !commentsErrorMessage.isNullOrBlank() -> Text(
                text = commentsErrorMessage,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(horizontal = 24.dp)
            )
            comments.isEmpty() -> Text(
                text = stringResource(R.string.ui_d14da37946),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
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
                        avatarUrl = comment.avatarUrl,
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

        Spacer(modifier = Modifier.height(24.dp))
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
            text = stringResource(R.string.ui_57f2b181d0),
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        when {
            isLoading -> {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(4, contentType = { "user-skeleton" }) {
                        UserResultCardSkeleton()
                    }
                }
            }
            errorMessage != null -> UserCarouselStatusCard(
                message = errorMessage,
                onActionClick = onRetry
            )
            users.isEmpty() -> EmptySearchState(
                query = query,
                resultType = stringResource(R.string.result_type_users)
            )
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
                        UserResultCard(
                            name = user.name,
                            username = user.username,
                            avatarUrl = user.avatarUrl,
                            followersCount = user.followersCount,
                            isFollowing = user.isFollowing,
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
    onActionClick: (() -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(96.dp)
            .padding(horizontal = 16.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surface)
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        SearchIconBubble(type = SearchLeadingIcon.User)
        Text(
            text = message,
            modifier = Modifier
                .weight(1f)
                .padding(start = 12.dp, end = 8.dp),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodyMedium,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
        if (onActionClick != null) {
            RetryButton(onClick = onActionClick)
        }
    }
}

@Composable
private fun SearchSectionTitle(
    text: String,
    modifier: Modifier = Modifier
) {
        Text(
            text = text,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
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
            color = MaterialTheme.colorScheme.onSurfaceVariant,
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
                color = MaterialTheme.colorScheme.primary,
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
                color = MaterialTheme.colorScheme.onSurface,
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
                    contentDescription = stringResource(R.string.ui_dc6650d176),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(17.dp)
                )
            }
        }
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
        SearchLeadingIcon.Place -> Icons.Outlined.Search
    }
    Box(
        modifier = Modifier
            .size(22.dp),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
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
        LoadingContentSkeleton(modifier = Modifier.fillMaxWidth())
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
            color = MaterialTheme.colorScheme.onSurface,
            style = MaterialTheme.typography.bodyMedium
        )
        RetryButton(onClick = onRetry)
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
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .border(1.dp, MaterialTheme.colorScheme.outlineVariant, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Outlined.Search,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(24.dp)
            )
        }
        Text(
            text = stringResource(R.string.no_search_results, resultType, query),
            modifier = Modifier.padding(top = 14.dp),
            color = MaterialTheme.colorScheme.onSurface,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = stringResource(R.string.ui_60c49e0641),
            modifier = Modifier.padding(top = 4.dp),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 13.sp
        )
    }
}

private enum class SearchLeadingIcon {
    History,
    Tag,
    User,
    Place
}

private fun FeedPostResponse.toHomePostUiModel(
    isLikeLoading: Boolean,
    isSaveLoading: Boolean
): HomePostUiModel {
    val safeCreatedAt = createdAt ?: updatedAt

    return HomePostUiModel(
        id = id,
        ownerId = owner.id,
        username = owner.username.takeIf { it.isNotBlank() } ?: "unknown",
        ownerAvatarUrl = owner.avatarUrl?.takeIf { it.isNotBlank() },
        subtitle = location?.takeIf { it.isNotBlank() } ?: "STUDIO NULL",
        description = description.takeIf { it.isNotBlank() } ?: "",
        imageUrls = imageUrls.filter { it.isNotBlank() },
        likeCount = likeCount?.coerceAtLeast(0) ?: 0,
        commentCount = commentCount?.coerceAtLeast(0) ?: 0,
        saveCount = saveCount?.coerceAtLeast(0) ?: 0,
        isLiked = likedByCurrentUser == true,
        isLikeLoading = isLikeLoading,
        isSaved = savedByCurrentUser == true,
        isSaveLoading = isSaveLoading,
        timeAgoLabel = PostsUtils.formatTimeAgo(safeCreatedAt)
    )
}
