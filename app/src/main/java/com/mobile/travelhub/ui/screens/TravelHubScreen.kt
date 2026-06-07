package com.mobile.travelhub.ui.screens

import androidx.compose.ui.res.stringResource
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsWalk
import androidx.compose.material.icons.automirrored.outlined.DirectionsWalk
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.PostAdd
import androidx.compose.material.icons.filled.TravelExplore
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.PostAdd
import androidx.compose.material.icons.outlined.TravelExplore
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.mobile.travelhub.navigation.NavGraph
import com.mobile.travelhub.navigation.Screen
import com.mobile.travelhub.ui.components.layout.BottomNavItem
import com.mobile.travelhub.ui.components.layout.RoundedTopNavigationBar
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.mobile.travelhub.viewmodels.AuthViewModel
import kotlinx.coroutines.delay
import com.mobile.travelhub.R

@Composable
fun TravelHubScreen(
    authViewModel: AuthViewModel = hiltViewModel()
) {
    val navController = rememberNavController()
    val authUiState by authViewModel.uiState.collectAsState()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    var showSplash by remember { mutableStateOf(true) }
    var homeReloadSignal by remember { mutableStateOf(0) }
    var isExploreSearchExpanded by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(1200)
        showSplash = false
    }

    if (showSplash) {
        SplashScreen()
        return
    }

    val navItems = listOf(
        BottomNavItem(
            screen = Screen.Home,
            icon = Icons.Outlined.Home,
            selectedIcon = Icons.Filled.Home,
            label = stringResource(R.string.ui_70f8bb9a8a)
        ),
        BottomNavItem(
            screen = Screen.Explore,
            icon = Icons.Outlined.TravelExplore,
            selectedIcon = Icons.Filled.TravelExplore,
            label = stringResource(R.string.ui_b965ae66fc)
        ),
        BottomNavItem(
            screen = Screen.CreatePost,
            icon = Icons.Outlined.PostAdd,
            selectedIcon = Icons.Filled.PostAdd,
            label = stringResource(R.string.nav_create)
        ),
        BottomNavItem(
            screen = Screen.Trips,
            icon = Icons.AutoMirrored.Outlined.DirectionsWalk,
            selectedIcon = Icons.AutoMirrored.Filled.DirectionsWalk,
            label = stringResource(R.string.ui_b1bed287e1)
        ),
        BottomNavItem(
            screen = Screen.Profile,
            icon = Icons.Outlined.AccountCircle,
            selectedIcon = Icons.Filled.AccountCircle,
            label = stringResource(R.string.ui_ff4fc0276e)
        )
    )

    val startDestination = when {
        !authUiState.isAuthenticated -> Screen.Login.route
        else -> Screen.Home.route
    }
    val isExploreRoute = currentRoute
        ?.substringBefore("?")
        ?.substringBefore("/") == Screen.Explore.route
    val showBottomBar = Screen.fromRoute(currentRoute)?.showBottomBar == true &&
        !(isExploreRoute && isExploreSearchExpanded)

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            if (showBottomBar) {
                RoundedTopNavigationBar(
                    items = navItems,
                    navController = navController,
                    onHomeReselected = { homeReloadSignal++ }
                )
            }
        }
    ) { innerPadding ->
        NavGraph(
            navController = navController,
            innerPadding = innerPadding,
            startDestination = startDestination,
            authUiState = authUiState,
            homeReloadSignal = homeReloadSignal,
            onExploreSearchActiveChange = { isExploreSearchExpanded = it },
            onLogin = authViewModel::login,
            onRegister = authViewModel::register,
            onClearAuthError = authViewModel::clearError,
            onLogout = authViewModel::logout
        )
    }
}
