package com.mobile.travelhub.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

import androidx.lifecycle.viewmodel.compose.viewModel

import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel


import com.mobile.travelhub.R
import com.mobile.travelhub.ui.components.FeedPostCard
import com.mobile.travelhub.ui.components.PrimaryProfileButton
import com.mobile.travelhub.ui.components.ProfileHeader
import com.mobile.travelhub.ui.components.ProfileStats

import com.mobile.travelhub.ui.viewmodels.ProfileViewModel
import com.mobile.travelhub.ui.viewmodels.UiState
import com.mobile.travelhub.ui.theme.*

import com.mobile.travelhub.ui.theme.*
import com.mobile.travelhub.ui.components.SecondaryProfileButton
import com.mobile.travelhub.ui.theme.SurfaceContainerLow
import com.mobile.travelhub.viewmodels.ProfileViewModel
import com.mobile.travelhub.viewmodels.UiState


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

    val scrollState = rememberScrollState()

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


    val showTopBar = !isViewingOwnProfile


    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = SurfaceBg,
        topBar = {
            if (!isViewingOwnProfile) {
                Surface(
                    color = SurfaceBg
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .padding(horizontal = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = { onBack?.invoke() }) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = OnSurface
                            )
                        }
                        Text(
                            text = "PROFILE",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 14.sp,
                            letterSpacing = 1.sp,
                            color = OnSurface
                        )
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

                        if (isViewingOwnProfile) viewModel.loadUserProfile() 
                        else viewingUserId?.let { viewModel.loadOtherUserProfile(it) }

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
                }
                is UiState.Success -> {
                    val profile = state.data
                    
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(scrollState)
                    ) {
                        ProfileHeader(
                            name = profile.name,
                            handle = "@${profile.username}",
                            bio = profile.bio ?: "Traveler & Explorer.",
                            avatarRes = R.drawable.ic_launcher_foreground
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        if (isViewingOwnProfile) {

                            Box(modifier = Modifier.padding(horizontal = 24.dp)) {
                                PrimaryProfileButton(
                                    text = "Edit Profile",
                                    onClick = onNavigateToEditProfile,
                                    modifier = Modifier.fillMaxWidth()
                                )

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 24.dp)
                            ) {
                                Column(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    PrimaryProfileButton(
                                        text = "Edit Profile",
                                        onClick = onNavigateToEditProfile,
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                    onNavigateToHistory?.let { navigate ->
                                        SecondaryProfileButton(
                                            text = "View Place History",
                                            onClick = navigate,
                                            modifier = Modifier.fillMaxWidth()
                                        )
                                    }
                                    onLogout?.let { logout ->
                                        SecondaryProfileButton(
                                            text = "Đăng xuất",
                                            onClick = logout,
                                            modifier = Modifier.fillMaxWidth()
                                        )
                                    }
                                }

                            }
                        } else {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 24.dp),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Button(
                                    onClick = { viewingUserId?.let { viewModel.toggleFollowOtherUser(it, profile.isFollowing) } },
                                    shape = RoundedCornerShape(24.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (profile.isFollowing) SurfaceContainerLow else PrimaryBlue,
                                        contentColor = if (profile.isFollowing) PrimaryBlue else Color.White
                                    ),
                                    modifier = Modifier.weight(1f).height(48.dp)
                                ) {

                                    Text(text = if (profile.isFollowing) "Unfollow" else "Follow", fontWeight = FontWeight.Bold)
                                }
                                
                                Button(
                                    onClick = { onNavigateToChat?.invoke() },
                                    shape = RoundedCornerShape(24.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                                    modifier = Modifier.weight(1f).height(48.dp)
                                ) {
                                    Text(text = "Chat", fontWeight = FontWeight.Bold)

                                    Text(
                                        text = if (profile.isFollowing) "Unfollow" else "Follow",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                
                                Button(
                                    onClick = {
                                        viewingUserId?.let { viewModel.loadOtherUserProfile(it) }
                                        onNavigateToChat?.invoke()
                                    },
                                    shape = RoundedCornerShape(8.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = PrimaryBlue,
                                        contentColor = MaterialTheme.colorScheme.onPrimary
                                    ),
                                    modifier = Modifier.weight(1f).height(48.dp)
                                ) {
                                    Text(
                                        text = "Chat",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold
                                    )

                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(32.dp))

                        ProfileStats(
                            postsCount = profile.postsCount,
                            followersCount = profile.followersCount,
                            followingCount = profile.followingCount,
                            onPostsClick = { /* No-op */ },
                            onFollowersClick = onNavigateToFollowers,
                            onFollowingClick = onNavigateToFollowing
                        )

                        Spacer(modifier = Modifier.height(32.dp))

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(SurfaceContainerLow)
                                .padding(top = 24.dp, bottom = 100.dp)
                        ) {
                            Text(

                                text = "GALLERY",
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 13.sp,
                                modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp),
                                color = OnSurfaceVariant
                            )
                            
                            val posts = List(12) { R.drawable.ic_launcher_foreground }
                            PostGrid(
                                posts = posts,
                                onPostClick = { /* View post details */ }
                            )

                                text = "POSTS",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.ExtraBold,
                                modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            when {
                                profilePostsState.isLoading -> {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 24.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        CircularProgressIndicator(color = PrimaryBlue)
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
                                            if (isViewingOwnProfile) {
                                                viewModel.loadUserPosts()
                                            } else {
                                                viewingUserId?.let(viewModel::loadUserPosts)
                                            }
                                        },
                                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
                                        ) {
                                            Icon(Icons.Default.Refresh, contentDescription = null)
                                            Text(" Try Again", modifier = Modifier.padding(start = 8.dp))
                                        }
                                    }
                                }

                                profilePostsState.posts.isEmpty() -> {
                                    Text(
                                        text = "Chưa có bài viết nào.",
                                        modifier = Modifier.padding(horizontal = 24.dp),
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
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
