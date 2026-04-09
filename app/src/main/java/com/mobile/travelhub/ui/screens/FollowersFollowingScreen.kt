package com.mobile.travelhub.ui.screens

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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mobile.travelhub.R
import com.mobile.travelhub.ui.components.UserListItem
import com.mobile.travelhub.ui.viewmodels.ProfileViewModel
import com.mobile.travelhub.ui.viewmodels.UiState

@Composable
fun FollowersFollowingScreen(
    initialTabIndex: Int = 0,
    viewingUserId: Long? = null,
    onBack: () -> Unit,
    onNavigateToUserProfile: (Long?) -> Unit,
    viewModel: ProfileViewModel = viewModel()
) {
    var selectedTabIndex by remember { mutableIntStateOf(initialTabIndex) }
    val tabs = listOf("Followers", "Following")
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

    LaunchedEffect(viewedUserId) {
        if (isViewingOwnProfile) {
            viewModel.loadUserProfile()
        } else {
            viewModel.loadOtherUserProfile(viewedUserId)
        }
        viewModel.loadFollowers(viewedUserId)
        viewModel.loadFollowing(viewedUserId)
    }

    val titleName = if (profileState is UiState.Success) {
        (profileState as UiState.Success).data.name.uppercase()
    } else {
        "LOADING..."
    }

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
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                    Text(
                        text = titleName,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            TabRow(
                selectedTabIndex = selectedTabIndex,
                containerColor = MaterialTheme.colorScheme.background,
                contentColor = MaterialTheme.colorScheme.primary,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
                        color = MaterialTheme.colorScheme.primary
                    )
                },
                divider = {}
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTabIndex == index,
                        onClick = { selectedTabIndex = index },
                        text = {
                            Text(
                                text = title.uppercase(),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = if (selectedTabIndex == index) FontWeight.Bold else FontWeight.Normal,
                                color = if (selectedTabIndex == index) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    )
                }
            }

            Box(modifier = Modifier.fillMaxSize()) {
                val currentState = if (selectedTabIndex == 0) followersState else followingState
                
                when (currentState) {
                    is UiState.Loading -> {
                        CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
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
                        LazyColumn(modifier = Modifier.fillMaxSize()) {
                            items(users) { user ->
                                UserListItem(
                                    name = user.name,
                                    handle = user.username,
                                    avatarRes = R.drawable.ic_launcher_foreground,
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
                    else -> {}
                }
            }
        }
    }
}
