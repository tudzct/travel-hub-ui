package com.mobile.travelhub.ui.screens

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.mobile.travelhub.navigation.NavGraph
import com.mobile.travelhub.navigation.Screen
import com.mobile.travelhub.viewmodels.AuthViewModel

@Composable
fun TravelHubScreen(authViewModel: AuthViewModel = hiltViewModel()) {
    val navController = rememberNavController()
    val authUiState by authViewModel.uiState.collectAsState()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val onboardingRoutes = setOf(
        Screen.OnboardingIntro.route,
        Screen.OnboardingVibe.route,
        Screen.OnboardingFinish.route
    )
    val navItems = listOf(
        Screen.Home to "Home",
        Screen.Trips to "Trips",
        Screen.Profile to "Profile",
        Screen.Chat to "Chat"
    )

    val startDestination = remember(authUiState.isAuthenticated) {
        if (authUiState.isAuthenticated) Screen.Home.route else Screen.Login.route
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            val currentScreen = Screen.fromRoute(currentRoute)
            if (currentScreen?.showBottomBar == true) {
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
            onClearAuthError = authViewModel::clearError
        )
    }
}

@Composable
private fun RoundedTopNavigationBar(
    items: List<Pair<Screen, String>>,
    navController: NavHostController
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        tonalElevation = 8.dp
    ) {
        NavigationBar {
            items.forEach { (screen, label) ->
                NavigationBarItem(
                    selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                    onClick = {
                        navController.navigate(screen.route) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    icon = { Text(text = label.first().toString()) },
                    label = { Text(text = label) }
                )
            }
        }
    }
}