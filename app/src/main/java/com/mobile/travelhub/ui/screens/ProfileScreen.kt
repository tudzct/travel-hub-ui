package com.mobile.travelhub.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.outlined.PhotoCamera
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.clickable
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel


import com.mobile.travelhub.R
import com.mobile.travelhub.ui.components.FeedPostCard
import com.mobile.travelhub.ui.theme.*
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
                        // Top Row: Avatar and Stats
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

                            // Stats
                            Row(
                                modifier = Modifier.weight(1f),
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

                        // Bio section
                        Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                            Text(
                                text = profile.name,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                            if (!profile.bio.isNullOrBlank()) {
                                Spacer(modifier = Modifier.height(4.dp))
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
                                    onClick = { viewingUserId?.let { viewModel.toggleFollowOtherUser(it, profile.isFollowing) } },
                                    shape = RoundedCornerShape(24.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (profile.isFollowing) Color(0xFFEAEAF0) else PrimaryBlue,
                                        contentColor = if (profile.isFollowing) Color.Black else Color.White
                                    ),
                                    modifier = Modifier.weight(1f).height(36.dp),
                                    contentPadding = PaddingValues(0.dp)
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
                                        text = if (profile.isFollowing) "Following" else "Follow",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                                
                                Button(
                                    onClick = {
                                        viewingUserId?.let { viewModel.loadOtherUserProfile(it) }
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
