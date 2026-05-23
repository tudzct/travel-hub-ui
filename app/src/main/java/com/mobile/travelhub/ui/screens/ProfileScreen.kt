package com.mobile.travelhub.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.outlined.PhotoCamera
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.clickable
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel

import com.mobile.travelhub.R
import com.mobile.travelhub.data.model.UserProfileResponse
import com.mobile.travelhub.ui.components.FeedPostCard
import com.mobile.travelhub.ui.components.FeedPostCardSkeleton
import com.mobile.travelhub.ui.theme.*
import com.mobile.travelhub.viewmodels.HomePostUiModel
import com.mobile.travelhub.viewmodels.ProfileViewModel
import com.mobile.travelhub.viewmodels.ProfilePostsUiState
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
    onBack: (() -> Unit)? = null,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val isViewingOwnProfile = viewingUserId == null

    val profileState by if (isViewingOwnProfile) {
        viewModel.profileState.collectAsState()
    } else {
        viewModel.otherUserProfileState.collectAsState()
    }
    val profilePostsState by viewModel.profilePostsState.collectAsState()
    val unauthorized by viewModel.unauthorized.collectAsState()

    LaunchedEffect(Unit) {
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
                viewModel.loadUserPosts()
            } else {
                viewModel.loadUserPosts(userId)
            }
        },
        onToggleFollow = viewModel::toggleFollowOtherUser
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
    onReloadProfile: () -> Unit,
    onReloadOtherUserProfile: (Long) -> Unit,
    onReloadPosts: (Long?) -> Unit,
    onToggleFollow: (Long, Boolean) -> Unit
) {
    val profileTitle = (profileState as? UiState.Success)
        ?.data
        ?.username
        ?.takeIf { it.isNotBlank() }
        ?: "Profile"
    val scrollState = rememberScrollState()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val coroutineScope = rememberCoroutineScope()
    var showNotifications by remember { mutableStateOf(false) }
    var hideDrawerContentForNavigation by remember { mutableStateOf(false) }

    ModalNavigationDrawer(
        drawerState = drawerState,
        gesturesEnabled = isViewingOwnProfile,
        drawerContent = {
            if (isViewingOwnProfile && !hideDrawerContentForNavigation) {
                ModalDrawerSheet(
                    modifier = Modifier.width(280.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(top = 56.dp)
                    ) {
                        val navigateToHistory = onNavigateToHistory
                        if (navigateToHistory != null) {
                            NavigationDrawerItem(
                                label = { Text("Recently viewed places") },
                                selected = false,
                                onClick = {
                                    hideDrawerContentForNavigation = true
                                    coroutineScope.launch {
                                        drawerState.snapTo(DrawerValue.Closed)
                                        withFrameNanos { }
                                        navigateToHistory()
                                    }
                                },
                                icon = {
                                    Icon(
                                        imageVector = Icons.Outlined.History,
                                        contentDescription = null
                                    )
                                },
                                modifier = Modifier.padding(horizontal = 12.dp)
                            )
                        }
                        NavigationDrawerItem(
                            label = { Text("Logout") },
                            selected = false,
                            onClick = {
                                coroutineScope.launch { drawerState.close() }
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
                            .height(56.dp)
                            .padding(horizontal = 4.dp),
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
                                            contentDescription = "Notifications",
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
                                if (!isViewingOwnProfile) {
                                    IconButton(
                                        onClick = { onBack?.invoke() },
                                        modifier = Modifier.align(Alignment.CenterStart)
                                    ) {
                                        Icon(
                                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                            contentDescription = "Back",
                                            tint = OnSurface
                                        )
                                    }
                                } else {
                                    IconButton(
                                        onClick = {
                                            hideDrawerContentForNavigation = false
                                            coroutineScope.launch { drawerState.open() }
                                        },
                                        modifier = Modifier.align(Alignment.CenterStart)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Menu,
                                            contentDescription = "Open menu",
                                            tint = OnSurface
                                        )
                                    }
                                }

                            }
                        }
                    }
                ) { innerPadding ->
                    Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
                        when (val state = profileState) {
                            is UiState.Loading -> {
                                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center), color = PrimaryBlue)
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
                                        Box(modifier = Modifier.size(80.dp)) {
                                            Image(
                                                painter = painterResource(id = R.drawable.female_avatar_maker),
                                                contentDescription = "Avatar",
                                                modifier = Modifier
                                                    .fillMaxSize()
                                                    .clip(CircleShape)
                                                    .border(2.dp, Color(0xFFE0E0E0), CircleShape),
                                                contentScale = ContentScale.Crop
                                            )
                                            // + icon
                                            Box(
                                                modifier = Modifier
                                                    .align(Alignment.BottomEnd)
                                                    .offset(x = (-4).dp, y = (-4).dp)
                                                    .size(24.dp)
                                                    .background(Color.White, CircleShape)
                                                    .padding(2.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.AddCircle,
                                                    contentDescription = "Add Story",
                                                    tint = PrimaryBlue,
                                                    modifier = Modifier.fillMaxSize()
                                                )
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
                                                    Text(text = "Posts", fontSize = 12.sp, color = Color.Gray)
                                                }
                                                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.clickable { onNavigateToFollowers() }) {
                                                    Text(text = profile.followersCount.toString(), fontWeight = FontWeight.Bold, fontSize = 18.sp)
                                                    Text(text = "Followers", fontSize = 12.sp, color = Color.Gray)
                                                }
                                                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.clickable { onNavigateToFollowing() }) {
                                                    Text(text = profile.followingCount.toString(), fontWeight = FontWeight.Bold, fontSize = 18.sp)
                                                    Text(text = "Following", fontSize = 12.sp, color = Color.Gray)
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
                                                modifier = Modifier.weight(1f).height(36.dp),
                                                shape = RoundedCornerShape(8.dp),
                                                colors = ButtonDefaults.buttonColors(
                                                    containerColor = Color(0xFFEAEAF0),
                                                    contentColor = Color.Black
                                                ),
                                                contentPadding = PaddingValues(0.dp)
                                            ) {
                                                Text("Edit Profile", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
                                            }
                                            Button(
                                                onClick = { /* Share Profile Action */ },
                                                modifier = Modifier.weight(1f).height(36.dp),
                                                shape = RoundedCornerShape(8.dp),
                                                colors = ButtonDefaults.buttonColors(
                                                    containerColor = Color(0xFFEAEAF0),
                                                    contentColor = Color.Black
                                                ),
                                                contentPadding = PaddingValues(0.dp)
                                            ) {
                                                Text("Share Profile", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
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
                                                modifier = Modifier.weight(1f).height(36.dp),
                                                contentPadding = PaddingValues(0.dp)
                                            ) {
                                                Text(
                                                    text = if (profile.isFollowing) "Following" else "Follow",
                                                    style = MaterialTheme.typography.labelMedium,
                                                    fontWeight = FontWeight.SemiBold
                                                )
                                            }

                                            Button(
                                                onClick = {
                                                    viewingUserId?.let(onReloadOtherUserProfile)
                                                    onNavigateToChat?.invoke()
                                                },
                                                shape = RoundedCornerShape(8.dp),
                                                colors = ButtonDefaults.buttonColors(
                                                    containerColor = Color(0xFFEAEAF0),
                                                    contentColor = Color.Black
                                                ),
                                                modifier = Modifier.weight(1f).height(36.dp),
                                                contentPadding = PaddingValues(0.dp)
                                            ) {
                                                Text(
                                                    text = "Message",
                                                    style = MaterialTheme.typography.labelMedium,
                                                    fontWeight = FontWeight.SemiBold
                                                )
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(16.dp))
                                    HorizontalDivider(color = Color(0xFFF0F0F0))

                                    // Posts Section
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(top = 24.dp, bottom = 100.dp)
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
                                                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
                                                    ) {
                                                        Icon(Icons.Default.Refresh, contentDescription = null)
                                                        Text(" Try Again", modifier = Modifier.padding(start = 8.dp))
                                                    }
                                                }
                                            }

                                            profilePostsState.posts.isEmpty() -> {
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
                                                            contentDescription = "No Posts",
                                                            modifier = Modifier.size(40.dp),
                                                            tint = Color.Gray
                                                        )
                                                    }
                                                    Spacer(modifier = Modifier.height(16.dp))
                                                    Text(
                                                        text = "No Posts Yet",
                                                        style = MaterialTheme.typography.titleLarge,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                    Spacer(modifier = Modifier.height(8.dp))
                                                    Text(
                                                        text = "When you share photos, they will appear on your profile.",
                                                        style = MaterialTheme.typography.bodyMedium,
                                                        color = Color.Gray,
                                                        textAlign = TextAlign.Center
                                                    )
                                                    Spacer(modifier = Modifier.height(24.dp))
                                                    if (isViewingOwnProfile) {
                                                        Button(
                                                            onClick = { /* navigate to create post */ },
                                                            shape = RoundedCornerShape(24.dp),
                                                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
                                                        ) {
                                                            Text("Create your first post", fontWeight = FontWeight.Bold)
                                                        }
                                                    }
                                                }
                                            }

                                            else -> {
                                                profilePostsState.posts.forEach { post ->
                                                    FeedPostCard(
                                                        post = post,
                                                        onLikeClick = {},
                                                        onCommentClick = {},
                                                        actionsEnabled = false
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
                        NotificationsPopup(onDismiss = { showNotifications = false })
                    }
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
        Text(text = "Connection Error", fontWeight = FontWeight.Bold, color = SunsetOrange)
        Text(text = message, textAlign = TextAlign.Center, color = OnSurfaceVariant, modifier = Modifier.padding(top = 8.dp, bottom = 24.dp))
        Button(onClick = onRetry, colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)) {
            Icon(Icons.Default.Refresh, contentDescription = null)
            Text(" Try Again", modifier = Modifier.padding(start = 8.dp))
        }
    }
}

@Preview
@Composable
fun ProfileScreenPreview() {
    val sampleProfile = UserProfileResponse(
        id = 1,
        username = "traveler",
        name = "Alex Nguyen",
        bio = "Chasing sunsets and street food.",
        postsCount = 12,
        followersCount = 345,
        followingCount = 180,
        isFollowing = false
    )
    val samplePosts = listOf(
        HomePostUiModel(
            id = 1,
            username = "traveler",
            subtitle = "Hoi An, Viet Nam",
            description = "Golden hour by the river.",
            imageUrls = emptyList(),
            likeCount = 120,
            commentCount = 24,
            isLiked = false,
            isLikeLoading = false,
            timeAgoLabel = "2h"
        )
    )

    TravelHubTheme {
        ProfileScreenContent(
            isViewingOwnProfile = true,
            profileState = UiState.Success(sampleProfile),
            profilePostsState = ProfilePostsUiState(isLoading = false, posts = samplePosts),
            onNavigateToEditProfile = {},
            onNavigateToFollowers = {},
            onNavigateToFollowing = {},
            onNavigateToHistory = {},
            onLogout = {},
            onBack = {},
            viewingUserId = null,
            onNavigateToChat = {},
            onNotificationsClick = {},
            onReloadProfile = {},
            onReloadOtherUserProfile = {},
            onReloadPosts = {},
            onToggleFollow = { _, _ -> }
        )
    }
}
