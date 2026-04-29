package com.mobile.travelhub.ui.screens

import androidx.activity.compose.LocalOnBackPressedDispatcherOwner
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.DirectionsWalk
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.DirectionsWalk
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.TravelExplore
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
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
        BottomNavItem(
            screen = Screen.Home,
            icon = Icons.Outlined.Home,
            label = "Home"
        ),
        BottomNavItem(
            screen = Screen.Trips,
            icon = Icons.Outlined.TravelExplore,
            label = "Explore"
        ),
        BottomNavItem(
            screen = Screen.Trips,
            icon = Icons.AutoMirrored.Outlined.DirectionsWalk,
            label = "Itinerary"
        ),
        BottomNavItem(
            screen = Screen.CreatePost,
            icon = Icons.Outlined.Add,
            label = "Create"
        ),
        BottomNavItem(
            screen = Screen.Profile,
            icon = Icons.Outlined.AccountCircle,
            label = "Profile"
        )
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
