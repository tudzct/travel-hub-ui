package com.mobile.travelhub.ui.screens

import androidx.compose.ui.res.stringResource
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.outlined.AddBox
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.GridView
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.outlined.PhotoCamera
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DrawerState
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.clickable
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import coil.compose.AsyncImage

import com.mobile.travelhub.R
import com.mobile.travelhub.data.model.UserProfileResponse
import com.mobile.travelhub.data.userMessage
import com.mobile.travelhub.ui.components.FeedPostCard
import com.mobile.travelhub.ui.components.FeedPostCardSkeleton
import com.mobile.travelhub.ui.components.HomeCommentsBottomSheet
import com.mobile.travelhub.ui.components.InlineLoadingSkeleton
import com.mobile.travelhub.ui.components.LoadingContentSkeleton
import com.mobile.travelhub.ui.components.SimpleFormTextField
import com.mobile.travelhub.ui.components.TravelHubAvatar
import com.mobile.travelhub.ui.components.TravelHubDrawerContent
import com.mobile.travelhub.ui.components.layout.MainMenuButton
import com.mobile.travelhub.ui.theme.*
import com.mobile.travelhub.viewmodels.HomePostUiModel
import com.mobile.travelhub.viewmodels.ProfileViewModel
import com.mobile.travelhub.viewmodels.ProfilePostsUiState
import com.mobile.travelhub.viewmodels.ProfilePostsTab
import com.mobile.travelhub.viewmodels.UiState
import kotlinx.coroutines.launch

@Composable
fun ProfileScreen(
    onNavigateToEditProfile: () -> Unit,
    onNavigateToFollowers: () -> Unit,
    onNavigateToFollowing: () -> Unit,
    onNavigateToHistory: (() -> Unit)? = null,
    onLogout: (() -> Unit)? = null,
    onRequireLogin: (() -> Unit)? = null,
    viewingUserId: Long? = null,
    onNavigateToChat: (() -> Unit)? = null,
    onNotificationsClick: (() -> Unit)? = null,
    onPostNotificationClick: (Long) -> Unit = {},
    onFollowNotificationClick: (Long) -> Unit = {},
    onNavigateToUserProfile: (Long) -> Unit = {},
    onNavigateToCreatePost: () -> Unit = {},
    onBack: (() -> Unit)? = null,
    drawerState: DrawerState? = null,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val isViewingOwnProfile = viewingUserId == null
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val profileState by if (isViewingOwnProfile) {
        viewModel.profileState.collectAsState()
    } else {
        viewModel.otherUserProfileState.collectAsState()
    }
    val profilePostsState by viewModel.profilePostsState.collectAsState()
    val unauthorized by viewModel.unauthorized.collectAsState()
    val changePasswordState by viewModel.changePasswordState.collectAsState()

    LaunchedEffect(isViewingOwnProfile, viewingUserId) {
        if (isViewingOwnProfile) {
            viewModel.loadUserProfile()
            viewModel.loadUserPosts()
        } else {
            viewingUserId?.let {
                viewModel.loadOtherUserProfile(it)
                viewModel.loadUserPosts(it)
            }
        }
    }

    LaunchedEffect(unauthorized) {
        if (unauthorized && isViewingOwnProfile) {
            viewModel.clearUnauthorized()
            onRequireLogin?.invoke()
        }
    }

    val onAvatarSelected: (Uri) -> Unit = onAvatarSelected@{ uri ->
        if (!isViewingOwnProfile) return@onAvatarSelected
        val currentProfile = (profileState as? UiState.Success)?.data ?: return@onAvatarSelected
        coroutineScope.launch {
            try {
                val bytes = context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
                    ?: throw IllegalStateException("Không thể đọc ảnh đã chọn")
                val mimeType = context.contentResolver.getType(uri) ?: "image/jpeg"
                val fileName = uri.lastPathSegment
                    ?.substringAfterLast('/')
                    ?.takeIf { it.isNotBlank() }
                    ?: "avatar.jpg"
                val uploadedUrl = viewModel.uploadAvatar(
                    imageBytes = bytes,
                    mimeType = mimeType,
                    fileName = fileName
                )
                viewModel.updateProfile(
                    name = currentProfile.name,
                    username = currentProfile.username,
                    bio = currentProfile.bio.orEmpty(),
                    dob = currentProfile.dateOfBirth.orEmpty(),
                    gender = currentProfile.gender.orEmpty(),
                    location = currentProfile.location.orEmpty(),
                    avatarUrl = uploadedUrl
                )
            } catch (e: Exception) {
                Toast.makeText(
                    context,
                    e.userMessage(context.getString(R.string.image_upload_failed)),
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    ProfileScreenContent(
        isViewingOwnProfile = isViewingOwnProfile,
        profileState = profileState,
        profilePostsState = profilePostsState,
        onNavigateToEditProfile = onNavigateToEditProfile,
        onNavigateToFollowers = onNavigateToFollowers,
        onNavigateToFollowing = onNavigateToFollowing,
        onNavigateToHistory = onNavigateToHistory,
        onLogout = onLogout,
        onBack = onBack,
        viewingUserId = viewingUserId,
        onNavigateToChat = onNavigateToChat,
        onNotificationsClick = onNotificationsClick,
        onPostNotificationClick = onPostNotificationClick,
        onFollowNotificationClick = onFollowNotificationClick,
        onNavigateToUserProfile = onNavigateToUserProfile,
        onNavigateToCreatePost = onNavigateToCreatePost,
        onReloadProfile = {
            if (isViewingOwnProfile) {
                viewModel.loadUserProfile()
            } else {
                viewingUserId?.let(viewModel::loadOtherUserProfile)
            }
        },
        onReloadOtherUserProfile = viewModel::loadOtherUserProfile,
        onReloadPosts = { userId ->
            if (userId == null) {
                when (profilePostsState.selectedTab) {
                    ProfilePostsTab.POSTS -> viewModel.loadUserPosts()
                    ProfilePostsTab.SAVED -> viewModel.loadUserSavedPosts()
                    ProfilePostsTab.LIKED -> viewModel.loadUserLikedPosts()
                }
            } else {
                viewModel.loadUserPosts(userId)
            }
        },
        onProfileTabSelected = { tab ->
            viewModel.selectProfilePostsTab(tab)
        },
        onToggleFollow = viewModel::toggleFollowOtherUser,
        onLikeClick = viewModel::onLikeClicked,
        onSaveClick = viewModel::onSaveClicked,
        onCommentClick = viewModel::onCommentClicked,
        onCommentDismissed = viewModel::onCommentDismissed,
        onCommentInputChanged = viewModel::onCommentInputChanged,
        onCommentSubmit = viewModel::submitComment,
        onAvatarSelected = onAvatarSelected,
        changePasswordState = changePasswordState,
        onChangePassword = viewModel::changePassword,
        onClearChangePasswordState = viewModel::clearChangePasswordState,
        drawerState = drawerState
    )
}

@Composable
private fun ProfileScreenContent(
    isViewingOwnProfile: Boolean,
    profileState: UiState<UserProfileResponse>,
    profilePostsState: ProfilePostsUiState,
    onNavigateToEditProfile: () -> Unit,
    onNavigateToFollowers: () -> Unit,
    onNavigateToFollowing: () -> Unit,
    onNavigateToHistory: (() -> Unit)?,
    onLogout: (() -> Unit)?,
    onBack: (() -> Unit)?,
    viewingUserId: Long?,
    onNavigateToChat: (() -> Unit)?,
    onNotificationsClick: (() -> Unit)?,
    onPostNotificationClick: (Long) -> Unit,
    onFollowNotificationClick: (Long) -> Unit,
    onNavigateToUserProfile: (Long) -> Unit,
    onNavigateToCreatePost: () -> Unit,
    onReloadProfile: () -> Unit,
    onReloadOtherUserProfile: (Long) -> Unit,
    onReloadPosts: (Long?) -> Unit,
    onProfileTabSelected: (ProfilePostsTab) -> Unit,
    onToggleFollow: (Long, Boolean) -> Unit,
    onLikeClick: (Long) -> Unit,
    onSaveClick: (Long) -> Unit,
    onCommentClick: (Long) -> Unit,
    onCommentDismissed: () -> Unit,
    onCommentInputChanged: (String) -> Unit,
    onCommentSubmit: () -> Unit,
    onAvatarSelected: (Uri) -> Unit,
    changePasswordState: UiState<Boolean>,
    onChangePassword: (String, String, String) -> Unit,
    onClearChangePasswordState: () -> Unit,
    drawerState: DrawerState?
) {
    val profileTitle = (profileState as? UiState.Success)
        ?.data
        ?.username
        ?.takeIf { it.isNotBlank() }
        ?: "Profile"
    val scrollState = rememberScrollState()
    val activeDrawerState = drawerState ?: rememberDrawerState(initialValue = DrawerValue.Closed)
    val coroutineScope = rememberCoroutineScope()
    val avatarPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        uri?.let(onAvatarSelected)
    }
    var showNotifications by remember { mutableStateOf(false) }
    var showChangePasswordDialog by remember { mutableStateOf(false) }
    var hideDrawerContentForNavigation by remember { mutableStateOf(false) }

    LaunchedEffect(changePasswordState) {
        if (changePasswordState is UiState.Success) {
            showChangePasswordDialog = false
        }
    }

    ModalNavigationDrawer(
        drawerState = activeDrawerState,
        gesturesEnabled = isViewingOwnProfile,
        scrimColor = Color.Black.copy(alpha = 0.38f),
        drawerContent = {
            if (isViewingOwnProfile && !hideDrawerContentForNavigation) {
                TravelHubDrawerContent(
                    profile = (profileState as? UiState.Success)?.data,
                    onProfileClick = {
                        coroutineScope.launch { activeDrawerState.close() }
                    },
                    onEditProfileClick = {
                        coroutineScope.launch { activeDrawerState.close() }
                        onNavigateToEditProfile()
                    },
                    onChangePasswordClick = {
                        onClearChangePasswordState()
                        showChangePasswordDialog = true
                        coroutineScope.launch { activeDrawerState.close() }
                    },
                    onLogoutClick = {
                        coroutineScope.launch { activeDrawerState.close() }
                        onLogout?.invoke()
                    }
                )
            }
        }
    ) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = Color.White,
            topBar = {
                Surface(
                    color = Color.White
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .padding(horizontal = 18.dp),
                        contentAlignment = Alignment.Center
                    ) {
                                if (isViewingOwnProfile) {
                                    IconButton(
                                        onClick = {
                                            showNotifications = true
                                            onNotificationsClick?.invoke()
                                        },
                                        modifier = Modifier.align(Alignment.CenterEnd)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Outlined.Notifications,
                                            contentDescription = stringResource(R.string.ui_753a22b2eb),
                                            tint = Color.Black,
                                            modifier = Modifier.size(24.dp)
                                        )
                                    }
                                }
                                Text(
                                    text = profileTitle,
                                    modifier = Modifier.padding(horizontal = 56.dp),
                                    fontWeight = FontWeight.ExtraBold,
                                    style = MaterialTheme.typography.titleLarge,
                                    color = Color.Black,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                if (onBack != null) {
                                    IconButton(
                                        onClick = { onBack.invoke() },
                                        modifier = Modifier.align(Alignment.CenterStart)
                                    ) {
                                        Icon(
                                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                            contentDescription = stringResource(R.string.ui_b52b36b726),
                                            tint = Color.Black,
                                            modifier = Modifier.size(24.dp)
                                        )
                                    }
                                } else {
                                    MainMenuButton(
                                        onClick = {
                                            hideDrawerContentForNavigation = false
                                            coroutineScope.launch { activeDrawerState.open() }
                                        },
                                        modifier = Modifier.align(Alignment.CenterStart)
                                    )
                                }

                            }
                        }
                    }
                ) { innerPadding ->
                    Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
                        when (val state = profileState) {
                            is UiState.Loading -> {
                                LoadingContentSkeleton(modifier = Modifier.fillMaxSize())
                            }
                            is UiState.Error -> {
                                ErrorLayout(message = state.message) {
                                    onReloadProfile()
                                    onReloadPosts(if (isViewingOwnProfile) null else viewingUserId)
                                }
                            }
                            is UiState.Success -> {
                                val profile = state.data
                                val displayName = profile.name.ifBlank { profile.username }
                                val avatarUrl = profile.avatarUrl?.takeIf { it.isNotBlank() }

                                Column(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .verticalScroll(scrollState)
                                ) {
                                    ProfileHeaderSection(
                                        profile = profile,
                                        displayName = displayName,
                                        avatarUrl = avatarUrl,
                                        isViewingOwnProfile = isViewingOwnProfile,
                                        onAvatarClick = {
                                            avatarPickerLauncher.launch(
                                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                            )
                                        }
                                    )

                                    Spacer(modifier = Modifier.height(12.dp))

                                    ProfileStatsCard(
                                        profile = profile,
                                        onFollowersClick = onNavigateToFollowers,
                                        onFollowingClick = onNavigateToFollowing
                                    )

                                    Spacer(modifier = Modifier.height(14.dp))

                                    if (isViewingOwnProfile) {
                                        ProfileEditRow(onClick = onNavigateToEditProfile)
                                    } else {
                                        ProfileFollowButton(
                                            isFollowing = profile.isFollowing,
                                            onClick = {
                                                viewingUserId?.let {
                                                    onToggleFollow(it, profile.isFollowing)
                                                }
                                            }
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(16.dp))

                                    if (isViewingOwnProfile) {
                                        ProfilePostsTabRow(
                                            selectedTab = profilePostsState.selectedTab,
                                            onTabSelected = onProfileTabSelected
                                        )
                                    }

                                    // Posts Section
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(top = if (isViewingOwnProfile) 2.dp else 12.dp)
                                    ) {
                                        when {
                                            profilePostsState.isLoading -> {
                                                repeat(3) {
                                                    FeedPostCardSkeleton()
                                                }
                                            }

                                            !profilePostsState.errorMessage.isNullOrBlank() -> {
                                                Column(
                                                    modifier = Modifier.padding(horizontal = 24.dp),
                                                    verticalArrangement = Arrangement.spacedBy(12.dp)
                                                ) {
                                                    Text(
                                                        text = profilePostsState.errorMessage.orEmpty(),
                                                        color = MaterialTheme.colorScheme.error
                                                    )
                                                    Button(
                                                        onClick = {
                                                            onReloadPosts(if (isViewingOwnProfile) null else viewingUserId)
                                                        },
                                                        colors = ButtonDefaults.buttonColors(
                                                            containerColor = PrimaryBlue,
                                                            contentColor = Color.White
                                                        )
                                                    ) {
                                                        Icon(Icons.Default.Refresh, contentDescription = null)
                                                        Text(stringResource(R.string.ui_d3bc9864fe), modifier = Modifier.padding(start = 8.dp))
                                                    }
                                                }
                                            }

                                            profilePostsState.posts.isEmpty() -> {
                                                ProfileEmptyPostsState(
                                                    selectedTab = profilePostsState.selectedTab,
                                                    isViewingOwnProfile = isViewingOwnProfile,
                                                    onNavigateToCreatePost = onNavigateToCreatePost
                                                )
                                            }

                                            else -> {
                                                profilePostsState.posts.forEach { post ->
                                                    FeedPostCard(
                                                        post = post,
                                                        onLikeClick = { onLikeClick(post.id) },
                                                        onSaveClick = { onSaveClick(post.id) },
                                                        onCommentClick = { onCommentClick(post.id) },
                                                        onAuthorClick = { onNavigateToUserProfile(post.ownerId) }
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            else -> {}
                        }
                    }
                    if (showNotifications) {
                        NotificationsPopup(
                            onDismiss = { showNotifications = false },
                            onPostNotificationClick = { postId ->
                                showNotifications = false
                                onPostNotificationClick(postId)
                            },
                            onFollowNotificationClick = { userId ->
                                showNotifications = false
                                onFollowNotificationClick(userId)
                            }
                        )
                    }
                    if (profilePostsState.activeCommentPostId != null) {
                        HomeCommentsBottomSheet(
                            comments = profilePostsState
                                .commentsByPostId[profilePostsState.activeCommentPostId]
                                .orEmpty(),
                            commentInput = profilePostsState.commentInput,
                            isCommentsLoading = profilePostsState.isCommentsLoading,
                            isCommentSubmitting = profilePostsState.isCommentSubmitting,
                            commentsErrorMessage = profilePostsState.commentsErrorMessage,
                            commentErrorMessage = profilePostsState.commentErrorMessage,
                            onDismiss = onCommentDismissed,
                            onCommentInputChanged = onCommentInputChanged,
                            onCommentSubmit = onCommentSubmit
                        )
                    }
                    if (showChangePasswordDialog) {
                        ChangePasswordDialog(
                            state = changePasswordState,
                            onDismiss = {
                                showChangePasswordDialog = false
                                onClearChangePasswordState()
                            },
                            onSubmit = onChangePassword
                        )
                    }
                }
            }
        }

@Composable
private fun ProfileHeaderSection(
    profile: UserProfileResponse,
    displayName: String,
    avatarUrl: String?,
    isViewingOwnProfile: Boolean,
    onAvatarClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 22.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(modifier = Modifier.size(72.dp)) {
            TravelHubAvatar(
                avatarUrl = avatarUrl,
                contentDescription = stringResource(R.string.ui_7631b26ea8),
                modifier = Modifier.fillMaxSize(),
                borderWidth = 0.dp
            )
            if (isViewingOwnProfile) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .offset(x = (-4).dp, y = (-1).dp)
                        .size(22.dp)
                        .shadow(4.dp, CircleShape)
                        .background(ProfileBlue, CircleShape)
                        .border(3.dp, Color.White, CircleShape)
                        .clickable(onClick = onAvatarClick),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.AddCircle,
                        contentDescription = stringResource(R.string.ui_60f2e98ebe),
                        tint = Color.White,
                        modifier = Modifier.size(17.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.width(18.dp))

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = displayName,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.ExtraBold,
                color = Color.Black,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(5.dp))
            Text(
                text = "@${profile.username}",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = ProfileMuted,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun ProfileStatsCard(
    profile: UserProfileResponse,
    onFollowersClick: () -> Unit,
    onFollowingClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 22.dp)
            .shadow(5.dp, RoundedCornerShape(12.dp), ambientColor = Color(0x10000000), spotColor = Color(0x0D000000)),
        color = Color.White,
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, Color(0xFFF0F2F7))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ProfileStatItem(
                value = profile.postsCount,
                label = "Bài viết",
                modifier = Modifier.weight(1f)
            )
            ProfileStatDivider()
            ProfileStatItem(
                value = profile.followersCount,
                label = "Người theo dõi",
                modifier = Modifier
                    .weight(1f)
                    .clickable(onClick = onFollowersClick)
            )
            ProfileStatDivider()
            ProfileStatItem(
                value = profile.followingCount,
                label = "Đang theo dõi",
                modifier = Modifier
                    .weight(1f)
                    .clickable(onClick = onFollowingClick)
            )
        }
    }
}

@Composable
private fun ProfileStatItem(
    value: Int,
    label: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(horizontal = 2.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = value.toString(),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.ExtraBold,
            color = Color.Black
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Medium,
            color = ProfileMuted,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun ProfileStatDivider() {
    Box(
        modifier = Modifier
            .height(50.dp)
            .width(1.dp)
            .background(Color(0xFFE4E7EF))
    )
}

@Composable
private fun ProfileEditRow(onClick: () -> Unit) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 22.dp)
            .height(50.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(14.dp),
        color = Color(0xFFF1F7FF)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Outlined.Settings,
                contentDescription = null,
                tint = ProfileBlue,
                modifier = Modifier.size(23.dp)
            )
            Spacer(modifier = Modifier.width(14.dp))
            Text(
                text = "Chỉnh sửa hồ sơ",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = ProfileInk,
                modifier = Modifier.weight(1f)
            )
            Icon(
                imageVector = Icons.Outlined.ChevronRight,
                contentDescription = null,
                tint = ProfileInk,
                modifier = Modifier.size(23.dp)
            )
        }
    }
}

@Composable
private fun ProfileFollowButton(
    isFollowing: Boolean,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 22.dp)
            .height(42.dp),
        shape = RoundedCornerShape(14.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isFollowing) Color(0xFFEFF3FA) else ProfileBlue,
            contentColor = if (isFollowing) ProfileInk else Color.White
        )
    ) {
        Text(
            text = if (isFollowing) "Đang theo dõi" else "Theo dõi",
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.ExtraBold
        )
    }
}

@Composable
private fun ProfileEmptyPostsState(
    selectedTab: ProfilePostsTab,
    isViewingOwnProfile: Boolean,
    onNavigateToCreatePost: () -> Unit
) {
    val emptyTitle = when (selectedTab) {
        ProfilePostsTab.POSTS -> "Chưa có bài đăng"
        ProfilePostsTab.SAVED -> "Chưa có bài đăng đã lưu"
        ProfilePostsTab.LIKED -> "Chưa có bài đăng đã thích"
    }
    val emptyMessage = when (selectedTab) {
        ProfilePostsTab.POSTS -> "Bạn chưa chia sẻ khoảnh khắc nào."
        ProfilePostsTab.SAVED -> "Bài viết mà bạn đã lưu sẽ hiển thị ở đây."
        ProfilePostsTab.LIKED -> "Bài viết mà bạn đã thích sẽ hiển thị ở đây."
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 22.dp, vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(86.dp)
                .background(Color(0xFFEFF6FF), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Outlined.Image,
                contentDescription = null,
                modifier = Modifier.size(44.dp),
                tint = Color(0xFF7DA3DD)
            )
        }
        Spacer(modifier = Modifier.height(20.dp))
        Text(
            text = emptyTitle,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.ExtraBold,
            color = ProfileInk,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(7.dp))
        Text(
            text = emptyMessage,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = ProfileMuted,
            textAlign = TextAlign.Center
        )
        if (isViewingOwnProfile && selectedTab == ProfilePostsTab.POSTS) {
            Spacer(modifier = Modifier.height(18.dp))
            Button(
                onClick = onNavigateToCreatePost,
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = ProfileBlue,
                    contentColor = Color.White
                ),
                modifier = Modifier
                    .height(44.dp)
                    .padding(horizontal = 18.dp),
                contentPadding = PaddingValues(horizontal = 18.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.AddBox,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "Tạo bài viết đầu tiên",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

private val ProfileBlue = Color(0xFF1677F2)
private val ProfileInk = Color(0xFF1F2937)
private val ProfileMuted = Color(0xFF6B7280)

@Composable
fun ChangePasswordDialog(
    state: UiState<Boolean>,
    onDismiss: () -> Unit,
    onSubmit: (String, String, String) -> Unit
) {
    var currentPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var currentPasswordVisible by remember { mutableStateOf(false) }
    var newPasswordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }
    val isLoading = state is UiState.Loading
    val errorMessage = (state as? UiState.Error)?.message
    val canSubmit = currentPassword.isNotBlank() &&
        newPassword.isNotBlank() &&
        confirmPassword.isNotBlank() &&
        !isLoading

    Dialog(
        onDismissRequest = {
            if (!isLoading) onDismiss()
        },
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            shape = RoundedCornerShape(28.dp),
            color = SurfaceContainerLowest
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(18.dp)
            ) {
                Text(
                    text = stringResource(R.string.ui_d4e1de2330),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = OnSurface
                )
                PasswordDialogField(
                    value = currentPassword,
                    onValueChange = { currentPassword = it },
                    label = stringResource(R.string.ui_9a3c6341b1),
                    passwordVisible = currentPasswordVisible,
                    onPasswordVisibilityChange = {
                        currentPasswordVisible = !currentPasswordVisible
                    }
                )
                PasswordDialogField(
                    value = newPassword,
                    onValueChange = { newPassword = it },
                    label = stringResource(R.string.ui_4267a600ce),
                    passwordVisible = newPasswordVisible,
                    onPasswordVisibilityChange = {
                        newPasswordVisible = !newPasswordVisible
                    }
                )
                PasswordDialogField(
                    value = confirmPassword,
                    onValueChange = { confirmPassword = it },
                    label = stringResource(R.string.ui_2766fdd4ce),
                    passwordVisible = confirmPasswordVisible,
                    onPasswordVisibilityChange = {
                        confirmPasswordVisible = !confirmPasswordVisible
                    }
                )
                if (!errorMessage.isNullOrBlank()) {
                    Text(
                        text = errorMessage,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = onDismiss,
                        enabled = !isLoading,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = SurfaceContainerLow,
                            contentColor = OnSurfaceVariant
                        )
                    ) {
                        Text(stringResource(R.string.ui_34ca764caf))
                    }
                    Button(
                        onClick = { onSubmit(currentPassword, newPassword, confirmPassword) },
                        enabled = canSubmit,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = PrimaryBlue,
                            contentColor = Color.White
                        )
                    ) {
                        if (isLoading) {
                            InlineLoadingSkeleton(modifier = Modifier.size(18.dp))
                        } else {
                            Text(stringResource(R.string.ui_a306970e8b))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PasswordDialogField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    passwordVisible: Boolean,
    onPasswordVisibilityChange: () -> Unit
) {
    SimpleFormTextField(
        value = value,
        onValueChange = onValueChange,
        placeholder = label,
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
        visualTransformation = if (passwordVisible) {
            VisualTransformation.None
        } else {
            PasswordVisualTransformation()
        },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
        trailingIcon = {
            IconButton(onClick = onPasswordVisibilityChange) {
                Icon(
                    imageVector = if (passwordVisible) {
                        Icons.Outlined.Visibility
                    } else {
                        Icons.Outlined.VisibilityOff
                    },
                    contentDescription = if (passwordVisible) {
                        "Ẩn mật khẩu"
                    } else {
                        "Hiện mật khẩu"
                    },
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                )
            }
        }
    )
}

	@Composable
private fun ProfilePostsTabRow(
    selectedTab: ProfilePostsTab,
    onTabSelected: (ProfilePostsTab) -> Unit
) {
    val tabs = listOf(
        ProfilePostsTab.POSTS to (Icons.Outlined.GridView to "Bài viết"),
        ProfilePostsTab.SAVED to (Icons.Outlined.BookmarkBorder to "Đã lưu"),
        ProfilePostsTab.LIKED to (Icons.Outlined.FavoriteBorder to "Đã thích")
    )
    val selectedIndex = tabs.indexOfFirst { it.first == selectedTab }.coerceAtLeast(0)

    TabRow(
        selectedTabIndex = selectedIndex,
        containerColor = Color.White,
        contentColor = ProfileBlue,
        indicator = { tabPositions ->
            Box(
                modifier = Modifier
                    .tabIndicatorOffset(tabPositions[selectedIndex])
                    .height(3.dp)
                    .padding(horizontal = 0.dp)
                    .background(ProfileBlue)
            )
        },
        divider = {
            HorizontalDivider(color = Color(0xFFE8EBF2), thickness = 1.dp)
        }
    ) {
        tabs.forEach { (tab, iconInfo) ->
            val (icon, contentDescription) = iconInfo
            Tab(
                selected = tab == selectedTab,
                onClick = { onTabSelected(tab) },
                modifier = Modifier.height(50.dp),
                icon = {
                    Icon(
                        imageVector = icon,
                        contentDescription = contentDescription,
                        tint = if (tab == selectedTab) ProfileBlue else ProfileInk,
                        modifier = Modifier.size(24.dp)
                    )
                }
            )
        }
    }
}

@Composable
fun ErrorLayout(message: String, onRetry: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = stringResource(R.string.ui_aca851b5d6), fontWeight = FontWeight.Bold, color = SunsetOrange)
        Text(text = message, textAlign = TextAlign.Center, color = OnSurfaceVariant, modifier = Modifier.padding(top = 8.dp, bottom = 24.dp))
        Button(
            onClick = onRetry,
            colors = ButtonDefaults.buttonColors(
                containerColor = PrimaryBlue,
                contentColor = Color.White
            )
        ) {
            Icon(Icons.Default.Refresh, contentDescription = null)
            Text(stringResource(R.string.ui_d3bc9864fe), modifier = Modifier.padding(start = 8.dp))
        }
    }
}

//@Preview
//@Composable
//fun ProfileScreenPreview() {
//    val sampleProfile = UserProfileResponse(
//        id = 1,
//        username = "traveler",
//        name = "Alex Nguyen",
//        bio = "Chasing sunsets and street food.",
//        postsCount = 12,
//        followersCount = 345,
//        followingCount = 180,
//        isFollowing = false
//    )
//    val samplePosts = listOf(
//        HomePostUiModel(
//            id = 1,
//            username = "traveler",
//            subtitle = stringResource(R.string.ui_c487afb3ee),
//            description = "Golden hour by the river.",
//            imageUrls = emptyList(),
//            likeCount = 120,
//            commentCount = 24,
//            isLiked = false,
//            isLikeLoading = false,
//            timeAgoLabel = "2h"
//        )
//    )
//
//    TravelHubTheme {
//        ProfileScreenContent(
//            isViewingOwnProfile = true,
//            profileState = UiState.Success(sampleProfile),
//            profilePostsState = ProfilePostsUiState(isLoading = false, posts = samplePosts),
//            onNavigateToEditProfile = {},
//            onNavigateToFollowers = {},
//            onNavigateToFollowing = {},
//            onNavigateToHistory = {},
//            onLogout = {},
//            onBack = {},
//            viewingUserId = null,
//            onNavigateToChat = {},
//            onNotificationsClick = {},
//            onPostNotificationClick = {},
//            onFollowNotificationClick = {},
//            onReloadProfile = {},
//            onReloadOtherUserProfile = {},
//            onReloadPosts = {},
//            onToggleFollow = { _, _ -> }
//        )
//    }
//}
