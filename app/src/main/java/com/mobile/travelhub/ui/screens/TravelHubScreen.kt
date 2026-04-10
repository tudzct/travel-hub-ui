package com.mobile.travelhub.ui.screens

import androidx.activity.compose.LocalOnBackPressedDispatcherOwner
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.DirectionsWalk
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.ChatBubble
import androidx.compose.material.icons.outlined.DirectionsWalk
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Menu
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.ShoppingBag
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.composables.explorenavigationicon
import com.mobile.travelhub.navigation.NavGraph
import com.mobile.travelhub.navigation.Screen
import com.mobile.travelhub.ui.components.layout.BottomNavItem
import com.mobile.travelhub.ui.components.layout.RoundedTopNavigationBar
import com.mobile.travelhub.viewmodels.AuthViewModel
import com.mobile.travelhub.viewmodels.OnboardingViewModel

@Composable
fun TravelHubScreen(
    authViewModel: AuthViewModel = hiltViewModel(),
    onboardingViewModel: OnboardingViewModel = hiltViewModel()
) {
    val navController = rememberNavController()
    val authUiState by authViewModel.uiState.collectAsState()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val navItems = listOf(
        BottomNavItem(screen = Screen.Home, icon = Icons.Outlined.Home, contentDescription = "Home"),
        BottomNavItem(
            screen = Screen.Trips,
            icon = explorenavigationicon,
            contentDescription = "Explore"
        ),
        BottomNavItem(
            screen = Screen.Trips,
            icon = Icons.AutoMirrored.Outlined.DirectionsWalk,
            contentDescription = "Trips"
        ),
        BottomNavItem(screen = Screen.Chat, icon = Icons.Outlined.ChatBubble, contentDescription = "Chat AI", badgeCount = 3),
        BottomNavItem(screen = Screen.Profile, icon = Icons.Outlined.AccountCircle, contentDescription = "Profile")
    )

    val startDestination = Screen.OnboardingTripType.route
    val showBottomBar = Screen.fromRoute(currentRoute)?.showBottomBar == true

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
            NavGraph(
                navController = navController,
                innerPadding = innerPadding,
                startDestination = startDestination,
                authUiState = authUiState,
                onLogin = authViewModel::login,
                onRegister = authViewModel::register,
                onClearAuthError = authViewModel::clearError,
                onboardingViewModel = onboardingViewModel
            )
        }

        if (showBottomBar) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .navigationBarsPadding()
            ) {
                RoundedTopNavigationBar(
                    items = navItems,
                    navController = navController
                )
            }
        }
    }
}
