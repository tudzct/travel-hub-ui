package com.mobile.travelhub.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mobile.travelhub.R
import com.mobile.travelhub.ui.components.PostGrid
import com.mobile.travelhub.ui.components.PrimaryProfileButton
import com.mobile.travelhub.ui.components.ProfileHeader
import com.mobile.travelhub.ui.components.ProfileStats
import com.mobile.travelhub.ui.components.SecondaryProfileButton
import com.mobile.travelhub.ui.viewmodels.ProfileViewModel
import com.mobile.travelhub.ui.viewmodels.UiState

@Composable
fun ProfileScreen(
    onNavigateToEditProfile: () -> Unit,
    onNavigateToFollowers: () -> Unit,
    onNavigateToFollowing: () -> Unit,
    onNavigateToHistory: (() -> Unit)? = null,
    viewingUserId: Long? = null,
    onNavigateToChat: (() -> Unit)? = null,
    onBack: (() -> Unit)? = null,
    viewModel: ProfileViewModel = viewModel()
) {
    val isViewingOwnProfile = viewingUserId == null
    
    val profileState by if (isViewingOwnProfile) {
        viewModel.profileState.collectAsState()
    } else {
        viewModel.otherUserProfileState.collectAsState()
    }
    
    val scrollState = rememberScrollState()

    LaunchedEffect(Unit) {
        if (isViewingOwnProfile) {
            viewModel.loadUserProfile()
        } else {
            viewingUserId?.let { viewModel.loadOtherUserProfile(it) }
        }
    }

    val showTopBar = !isViewingOwnProfile
    
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            if (showTopBar) {
                Surface(
                    color = MaterialTheme.colorScheme.background
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .padding(horizontal = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Start
                    ) {
                        IconButton(onClick = { onBack?.invoke() }) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back"
                            )
                        }
                        Text(
                            text = "PROFILE",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            when (val state = profileState) {
                is UiState.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                is UiState.Error -> {
                    Column(
                        modifier = Modifier.align(Alignment.Center).padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Lỗi kết nối API:",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.error,
                            fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                        )
                        Text(
                            text = state.message,
                            color = MaterialTheme.colorScheme.error,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(top = 8.dp, bottom = 24.dp)
                        )
                        Button(onClick = { 
                            if (isViewingOwnProfile) {
                                viewModel.loadUserProfile()
                            } else {
                                viewingUserId?.let { viewModel.loadOtherUserProfile(it) }
                            }
                        }) {
                            Icon(Icons.Default.Refresh, contentDescription = "Thử lại")
                            Text(" Thử lại", modifier = Modifier.padding(start = 8.dp))
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

                        if (isViewingOwnProfile) {
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
                                }
                            }
                        } else {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 24.dp, vertical = 12.dp),
                                horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(12.dp)
                            ) {
                                Button(
                                    onClick = { 
                                        viewingUserId?.let { 
                                            viewModel.toggleFollowOtherUser(it, profile.isFollowing)
                                        }
                                    },
                                    shape = RoundedCornerShape(8.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (profile.isFollowing) 
                                            MaterialTheme.colorScheme.surfaceVariant 
                                        else 
                                            MaterialTheme.colorScheme.primary,
                                        contentColor = if (profile.isFollowing) 
                                            MaterialTheme.colorScheme.primary 
                                        else 
                                            MaterialTheme.colorScheme.onPrimary
                                    ),
                                    modifier = Modifier
                                        .weight(1f)
                                        .fillMaxWidth()
                                ) {
                                    Text(
                                        text = if (profile.isFollowing) "Unfollow" else "Follow",
                                        style = MaterialTheme.typography.labelMedium
                                    )
                                }
                                
                                Button(
                                    onClick = {
                                        viewingUserId?.let { viewModel.loadOtherUserProfile(it) }
                                        onNavigateToChat?.invoke()
                                    },
                                    shape = RoundedCornerShape(8.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.primary,
                                        contentColor = MaterialTheme.colorScheme.onPrimary
                                    ),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text(
                                        text = "Chat",
                                        style = MaterialTheme.typography.labelMedium
                                    )
                                }
                            }
                        }

                        ProfileStats(
                            postsCount = profile.postsCount,
                            followersCount = profile.followersCount,
                            followingCount = profile.followingCount,
                            onPostsClick = {
                                if (isViewingOwnProfile) {
                                    onNavigateToEditProfile()
                                } else {
                                    viewingUserId?.let { viewModel.loadOtherUserProfile(it) }
                                }
                            },
                            onFollowersClick = onNavigateToFollowers,
                            onFollowingClick = onNavigateToFollowing
                        )

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                                .padding(top = 16.dp, bottom = 100.dp)
                        ) {
                            Text(
                                text = "Posts",
                                style = MaterialTheme.typography.labelMedium,
                                modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            
                            val posts = List(12) { R.drawable.ic_launcher_foreground }
                            PostGrid(
                                posts = posts,
                                onPostClick = { onNavigateToEditProfile() }
                            )
                        }
                    }
                }
                else -> {}
            }
        }
    }
}
