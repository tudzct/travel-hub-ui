package com.mobile.travelhub.ui.screens

import androidx.activity.compose.LocalOnBackPressedDispatcherOwner
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.ChatBubble
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.composables.explorenavigationicon
import com.mobile.travelhub.navigation.NavGraph
import com.mobile.travelhub.navigation.Screen
import com.mobile.travelhub.ui.components.layout.BottomNavItem
import com.mobile.travelhub.ui.components.layout.RoundedTopNavigationBar
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
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
            screen = Screen.CreatePost,
            icon = Icons.Outlined.Add,
            contentDescription = "Create Post"
        ),
        BottomNavItem(screen = Screen.Chat, icon = Icons.Outlined.ChatBubble, contentDescription = "Chat AI", badgeCount = 3),
        BottomNavItem(screen = Screen.Profile, icon = Icons.Outlined.AccountCircle, contentDescription = "Profile")
    )

    val startDestination = when {
        !authUiState.isAuthenticated -> Screen.Login.route
        !authUiState.isOnboarded -> Screen.OnboardingTripType.route
        else -> Screen.Home.route
    }
    val showBottomBar = Screen.fromRoute(currentRoute)?.showBottomBar == true

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            if (showBottomBar) {
                RoundedTopNavigationBar(
                    items = navItems,
                    navController = navController
                )
            }
        }
    ) { innerPadding ->
        NavGraph(
            navController = navController,
            innerPadding = innerPadding,
            startDestination = startDestination,
            authUiState = authUiState,
            onLogin = authViewModel::login,
            onRegister = authViewModel::register,
            onClearAuthError = authViewModel::clearError,
            onLogout = authViewModel::logout,
            onCompleteOnboarding = authViewModel::completeOnboarding,
            onboardingViewModel = onboardingViewModel
        )
    }
}
