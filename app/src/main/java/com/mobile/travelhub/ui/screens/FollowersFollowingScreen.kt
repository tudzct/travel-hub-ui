package com.mobile.travelhub.ui.screens

import androidx.compose.ui.res.stringResource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.Person
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
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.mobile.travelhub.R
import com.mobile.travelhub.ui.components.UserListItem
import com.mobile.travelhub.ui.components.LoadingListSkeleton
import com.mobile.travelhub.viewmodels.ProfileViewModel
import com.mobile.travelhub.viewmodels.UiState

@Composable
fun FollowersFollowingScreen(
    initialTabIndex: Int = 0,
    viewingUserId: Long? = null,
    onBack: () -> Unit,
    onNavigateToUserProfile: (Long?) -> Unit,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val showFollowers = initialTabIndex == 0
    val isViewingOwnProfile = viewingUserId == null
    val currentUserId = viewModel.getCurrentUserId()
    val viewedUserId = viewingUserId ?: viewModel.getCurrentUserId()

    val profileState by if (isViewingOwnProfile) {
        viewModel.profileState.collectAsState()
    } else {
        viewModel.otherUserProfileState.collectAsState()
    }
    val followersState by viewModel.followersState.collectAsState()
    val followingState by viewModel.followingState.collectAsState()

    LaunchedEffect(viewedUserId, showFollowers) {
        if (isViewingOwnProfile) {
            viewModel.loadUserProfile()
        } else {
            viewModel.loadOtherUserProfile(viewedUserId)
        }
        if (showFollowers) {
            viewModel.loadFollowers(viewedUserId)
        } else {
            viewModel.loadFollowing(viewedUserId)
        }
    }

    val titleName = stringResource(if (showFollowers) R.string.profile_followers_title else R.string.profile_following_title)
    val profileUsername = (profileState as? UiState.Success)?.data?.username?.trim()

    Scaffold(
        topBar = {
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
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.ui_b52b36b726))
                    }
                    Column {
                        Text(
                            text = titleName,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        if (!profileUsername.isNullOrBlank()) {
                            Text(
                                text = "@$profileUsername",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Medium,
                                letterSpacing = 1.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                val currentState = if (showFollowers) followersState else followingState
                
                when (currentState) {
                    is UiState.Loading -> {
                        LoadingListSkeleton(
                            modifier = Modifier.fillMaxSize(),
                            itemCount = 6
                        )
                    }
                    is UiState.Error -> {
                        Text(
                            text = currentState.message,
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }
                    is UiState.Success -> {
                        val users = currentState.data
                        if (users.isEmpty()) {
                            EmptyConnectionsState(
                                showFollowers = showFollowers,
                                isViewingOwnProfile = isViewingOwnProfile,
                                username = profileUsername,
                                modifier = Modifier.fillMaxSize()
                            )
                        } else {
                            LazyColumn(modifier = Modifier.fillMaxSize()) {
                                items(users) { user ->
                                    UserListItem(
                                        name = user.name,
                                        handle = user.username,
                                        avatarUrl = user.avatarUrl,
                                        isFollowing = user.isFollowing,
                                        showFollowButton = user.id != currentUserId,
                                        onClick = {
                                            onNavigateToUserProfile(user.id.takeIf { it != currentUserId })
                                        },
                                        onFollowToggle = {
                                            viewModel.toggleFollow(
                                                targetUserId = user.id,
                                                isCurrentlyFollowing = user.isFollowing,
                                                connectionsOwnerUserId = viewedUserId
                                            )
                                        }
                                    )
                                }
                            }
                        }
                    }
                    else -> {}
                }
            }
        }
    }
}

@Composable
private fun EmptyConnectionsState(
    showFollowers: Boolean,
    isViewingOwnProfile: Boolean,
    username: String?,
    modifier: Modifier = Modifier
) {
    val displayUsername = username?.takeIf { it.isNotBlank() }?.let { "@$it" } ?: "Người dùng này"
    val title = when {
        showFollowers && isViewingOwnProfile -> "Bạn chưa có người theo dõi"
        showFollowers -> "$displayUsername chưa có người theo dõi"
        isViewingOwnProfile -> "Bạn chưa theo dõi ai"
        else -> "$displayUsername chưa theo dõi ai"
    }
    val message = if (showFollowers) {
        "Khi có người theo dõi, danh sách sẽ xuất hiện tại đây."
    } else {
        "Những tài khoản được theo dõi sẽ xuất hiện tại đây."
    }

    Column(
        modifier = modifier.padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Outlined.Person,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center
        )
        Text(
            text = message,
            modifier = Modifier.padding(top = 8.dp),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}
