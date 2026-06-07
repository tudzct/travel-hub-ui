package com.mobile.travelhub.ui.screens

import androidx.compose.ui.res.stringResource
import android.net.Uri
import android.widget.Toast
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
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.outlined.PhotoCamera
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
        drawerContent = {
            if (isViewingOwnProfile && !hideDrawerContentForNavigation) {
                ModalDrawerSheet(
                    modifier = Modifier.width(280.dp),
                    drawerContainerColor = Color.White
                ) {
                    Column(
                        modifier = Modifier.padding(top = 56.dp)
                    ) {
                        NavigationDrawerItem(
                            label = { Text(stringResource(R.string.ui_d4e1de2330)) },
                            selected = false,
                            onClick = {
                                onClearChangePasswordState()
                                showChangePasswordDialog = true
                                coroutineScope.launch { activeDrawerState.close() }
                            },
                            icon = {
                                Icon(
                                    imageVector = Icons.Outlined.Lock,
                                    contentDescription = null
                                )
                            },
                            modifier = Modifier.padding(horizontal = 12.dp)
                        )
                        NavigationDrawerItem(
                            label = { Text(stringResource(R.string.ui_e43d612e11)) },
                            selected = false,
                            onClick = {
                                coroutineScope.launch { activeDrawerState.close() }
                                onLogout?.invoke()
                            },
                            icon = {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                                    contentDescription = null
                                )
                            },
                            modifier = Modifier.padding(horizontal = 12.dp)
                        )
                    }
                }
            }
        }
    ) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = MaterialTheme.colorScheme.background,
            topBar = {
                Surface(
                    color = MaterialTheme.colorScheme.background
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .padding(horizontal = 6.dp),
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
                                            tint = OnSurface
                                        )
                                    }
                                }
                                Text(
                                    text = profileTitle,
                                    modifier = Modifier.padding(horizontal = 56.dp),
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 16.sp,
                                    color = OnSurface,
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
                                            tint = OnSurface
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
                                    // Top Row: Avatar, full name, and stats
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 16.dp, vertical = 16.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        // Avatar
                                        Box(
                                            modifier = Modifier
                                                .size(80.dp)
                                        ) {
                                            if (avatarUrl != null) {
                                                AsyncImage(
                                                    model = avatarUrl,
                                                    contentDescription = stringResource(R.string.ui_7631b26ea8),
                                                    modifier = Modifier
                                                        .fillMaxSize()
                                                        .clip(CircleShape)
                                                        .border(2.dp, Color(0xFFE0E0E0), CircleShape),
                                                    contentScale = ContentScale.Crop
                                                )
                                            } else {
                                                Image(
                                                    painter = painterResource(id = R.drawable.female_avatar_maker),
                                                    contentDescription = stringResource(R.string.ui_7631b26ea8),
                                                    modifier = Modifier
                                                        .fillMaxSize()
                                                        .clip(CircleShape)
                                                        .border(2.dp, Color(0xFFE0E0E0), CircleShape),
                                                    contentScale = ContentScale.Crop
                                                )
                                            }
                                            if (isViewingOwnProfile) {
                                                Box(
                                                    modifier = Modifier
                                                        .align(Alignment.BottomEnd)
                                                        .offset(x = (-4).dp, y = (-4).dp)
                                                        .size(24.dp)
                                                        .background(Color.White, CircleShape)
                                                        .clickable {
                                                            avatarPickerLauncher.launch(
                                                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                                            )
                                                        }
                                                        .padding(2.dp)
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.AddCircle,
                                                        contentDescription = stringResource(R.string.ui_60f2e98ebe),
                                                        tint = PrimaryBlue,
                                                        modifier = Modifier.fillMaxSize()
                                                    )
                                                }
                                            }
                                        }

                                        Spacer(modifier = Modifier.width(24.dp))

                                        Column(
                                            modifier = Modifier.weight(1f),
                                            verticalArrangement = Arrangement.Center
                                        ) {
                                            Text(
                                                text = displayName,
                                                style = MaterialTheme.typography.titleMedium,
                                                fontWeight = FontWeight.Bold,
                                                color = OnSurface,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )

                                            Spacer(modifier = Modifier.height(10.dp))

                                            // Stats
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                                    Text(text = profile.postsCount.toString(), fontWeight = FontWeight.Bold, fontSize = 18.sp)
                                                    Text(text = stringResource(R.string.ui_a0ca0c3198), fontSize = 12.sp, color = Color.Gray)
                                                }
                                                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.clickable { onNavigateToFollowers() }) {
                                                    Text(text = profile.followersCount.toString(), fontWeight = FontWeight.Bold, fontSize = 18.sp)
                                                    Text(text = stringResource(R.string.ui_78eaabf4a6), fontSize = 12.sp, color = Color.Gray)
                                                }
                                                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.clickable { onNavigateToFollowing() }) {
                                                    Text(text = profile.followingCount.toString(), fontWeight = FontWeight.Bold, fontSize = 18.sp)
                                                    Text(text = stringResource(R.string.ui_90eeb10083), fontSize = 12.sp, color = Color.Gray)
                                                }
                                            }
                                        }
                                    }

                                    // Bio section
                                    if (!profile.bio.isNullOrBlank()) {
                                        Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                                            Text(
                                                text = profile.bio,
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = Color.DarkGray
                                            )
                                        }
                                    }

                                    // Location section
                                    if (!profile.location.isNullOrBlank()) {
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Row(
                                            modifier = Modifier.padding(horizontal = 16.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.LocationOn,
                                                contentDescription = stringResource(R.string.ui_d219c68101),
                                                tint = Color.Gray,
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = profile.location,
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = Color.Gray
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(16.dp))

                                    // Action Buttons
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 16.dp),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        if (isViewingOwnProfile) {
                                            Button(
                                                onClick = onNavigateToEditProfile,
                                                modifier = Modifier.fillMaxWidth().height(36.dp),
                                                shape = RoundedCornerShape(8.dp),
                                                colors = ButtonDefaults.buttonColors(
                                                    containerColor = Color(0xFFEAEAF0),
                                                    contentColor = Color.Black
                                                ),
                                                contentPadding = PaddingValues(0.dp)
                                            ) {
                                                Text(stringResource(R.string.ui_cd280a41f7), style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
                                            }
                                        } else {
                                            Button(
                                                onClick = {
                                                    viewingUserId?.let {
                                                        onToggleFollow(it, profile.isFollowing)
                                                    }
                                                },
                                                shape = RoundedCornerShape(8.dp),
                                                colors = ButtonDefaults.buttonColors(
                                                    containerColor = if (profile.isFollowing) Color(0xFFEAEAF0) else PrimaryBlue,
                                                    contentColor = if (profile.isFollowing) Color.Black else Color.White
                                                ),
                                                modifier = Modifier.fillMaxWidth().height(36.dp),
                                                contentPadding = PaddingValues(0.dp)
                                            ) {
                                                Text(
                                                    text = if (profile.isFollowing) "Following" else "Follow",
                                                    style = MaterialTheme.typography.labelMedium,
                                                    fontWeight = FontWeight.SemiBold
                                                )
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(16.dp))
                                    HorizontalDivider(color = Color(0xFFF0F0F0))

                                    if (isViewingOwnProfile) {
                                        ProfilePostsTabRow(
                                            selectedTab = profilePostsState.selectedTab,
                                            onTabSelected = onProfileTabSelected
                                        )
                                        HorizontalDivider(color = Color(0xFFF0F0F0))
                                    }

                                    // Posts Section
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(top = if (isViewingOwnProfile) 12.dp else 24.dp)
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
                                                val emptyTitle = when (profilePostsState.selectedTab) {
                                                    ProfilePostsTab.POSTS -> "Chưa có bài đăng"
                                                    ProfilePostsTab.SAVED -> "Chưa có bài đăng đã lưu"
                                                    ProfilePostsTab.LIKED -> "Chưa có bài đăng đã thích"
                                                }
                                                val emptyMessage = when (profilePostsState.selectedTab) {
                                                    ProfilePostsTab.POSTS -> ""
                                                    ProfilePostsTab.SAVED -> "Bài viết mà bạn đã lưu sẽ hiển thị ở đây."
                                                    ProfilePostsTab.LIKED -> "Bài viết mà bạn đã thích sẽ hiển thị ở đây."
                                                }
                                                Column(
                                                    modifier = Modifier.fillMaxWidth().padding(horizontal = 32.dp, vertical = 32.dp),
                                                    horizontalAlignment = Alignment.CenterHorizontally
                                                ) {
                                                    Box(
                                                        modifier = Modifier
                                                            .size(80.dp)
                                                            .border(1.dp, Color.Gray, CircleShape),
                                                        contentAlignment = Alignment.Center
                                                    ) {
                                                        Icon(
                                                            imageVector = Icons.Outlined.PhotoCamera,
                                                            contentDescription = stringResource(R.string.ui_1a3a388dd1),
                                                            modifier = Modifier.size(40.dp),
                                                            tint = Color.Gray
                                                        )
                                                    }
                                                    Spacer(modifier = Modifier.height(16.dp))
                                                    Text(
                                                        text = emptyTitle,
                                                        style = MaterialTheme.typography.titleLarge,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                    Spacer(modifier = Modifier.height(8.dp))
                                                    Text(
                                                        text = emptyMessage,
                                                        style = MaterialTheme.typography.bodyMedium,
                                                        color = Color.Gray,
                                                        textAlign = TextAlign.Center
                                                    )
                                                    Spacer(modifier = Modifier.height(24.dp))
                                                    if (isViewingOwnProfile && profilePostsState.selectedTab == ProfilePostsTab.POSTS) {
                                                        Button(
                                                            onClick = onNavigateToCreatePost,
                                                            shape = RoundedCornerShape(24.dp),
                                                            colors = ButtonDefaults.buttonColors(
                                                                containerColor = PrimaryBlue,
                                                                contentColor = Color.White
                                                            )
                                                        ) {
                                                            Text(stringResource(R.string.ui_454f9a145d), fontWeight = FontWeight.Bold)
                                                        }
                                                    }
                                                }
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
fun ChangePasswordDialog(
    state: UiState<Boolean>,
    onDismiss: () -> Unit,
    onSubmit: (String, String, String) -> Unit
) {
    var currentPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
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
                    label = stringResource(R.string.ui_9a3c6341b1)
                )
                PasswordDialogField(
                    value = newPassword,
                    onValueChange = { newPassword = it },
                    label = stringResource(R.string.ui_4267a600ce)
                )
                PasswordDialogField(
                    value = confirmPassword,
                    onValueChange = { confirmPassword = it },
                    label = stringResource(R.string.ui_2766fdd4ce)
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
    label: String
) {
    SimpleFormTextField(
        value = value,
        onValueChange = onValueChange,
        placeholder = label,
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
        visualTransformation = PasswordVisualTransformation(),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
    )
}

	@Composable
	private fun ProfilePostsTabRow(
    selectedTab: ProfilePostsTab,
    onTabSelected: (ProfilePostsTab) -> Unit
) {
    val tabs = listOf(
        ProfilePostsTab.POSTS to (Icons.Outlined.PhotoCamera to "Posts"),
        ProfilePostsTab.SAVED to (Icons.Outlined.BookmarkBorder to "Saved"),
        ProfilePostsTab.LIKED to (Icons.Outlined.FavoriteBorder to "Liked")
    )
    val selectedIndex = tabs.indexOfFirst { it.first == selectedTab }.coerceAtLeast(0)

    TabRow(
        selectedTabIndex = selectedIndex,
        containerColor = MaterialTheme.colorScheme.background,
        contentColor = PrimaryBlue
    ) {
        tabs.forEach { (tab, iconInfo) ->
            val (icon, contentDescription) = iconInfo
            Tab(
                selected = tab == selectedTab,
                onClick = { onTabSelected(tab) },
                icon = {
                    Icon(
                        imageVector = icon,
                        contentDescription = contentDescription,
                        tint = Color.Black
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
