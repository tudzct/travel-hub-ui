package com.mobile.travelhub.ui.screens

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.composables.icons.lucide.CalendarDays
import com.composables.icons.lucide.Compass
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.MessageCircle
import com.composables.icons.lucide.User
import com.mobile.travelhub.navigation.NavGraph
import com.mobile.travelhub.navigation.Screen
import com.mobile.travelhub.ui.theme.TravelHubTheme
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

    RoundedTopNavigationBarContent(
        items = items,
        currentRoute = currentDestination?.route,
        onItemClick = { screen ->
            navController.navigate(screen.route) {
                popUpTo(navController.graph.findStartDestination().id) {
                    saveState = true
                }
                launchSingleTop = true
                restoreState = true
            }
        }
    )
}

@Composable
private fun RoundedTopNavigationBarContent(
    items: List<Pair<Screen, String>>,
    currentRoute: String?,
    onItemClick: (Screen) -> Unit
) {
    val iconByScreen = mapOf(
        Screen.Home to Lucide.Compass,
        Screen.Trips to Lucide.CalendarDays,
        Screen.Chat to Lucide.MessageCircle,
        Screen.Profile to Lucide.User
    )

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 10.dp),
        shape = RoundedCornerShape(28.dp),
        color = MaterialTheme.colorScheme.surfaceVariant
    ) {
        NavigationBar(
            modifier = Modifier.height(84.dp),
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            tonalElevation = 0.dp,
            windowInsets = WindowInsets(0, 0, 0, 0)
        ) {
            items.forEach { (screen, label) ->
                val isSelected = currentRoute == screen.route

                NavigationBarItem(
                    selected = isSelected,
                    onClick = { onItemClick(screen) },
                    icon = {
                        Icon(
                            imageVector = iconByScreen[screen] ?: Lucide.Compass,
                            contentDescription = label
                        )
                    },
                    label = {
                        Text(
                            text = label.uppercase(),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        indicatorColor = MaterialTheme.colorScheme.secondaryContainer,
                        selectedIconColor = MaterialTheme.colorScheme.onSecondaryContainer,
                        selectedTextColor = MaterialTheme.colorScheme.onSecondaryContainer,
                        unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun RoundedTopNavigationBarPreview() {
    TravelHubTheme {
        RoundedTopNavigationBarContent(
            items = listOf(
                Screen.Home to "Home",
                Screen.Trips to "Trips",
                Screen.Profile to "Profile",
                Screen.Chat to "Chat"
            ),
            currentRoute = Screen.Home.route,
            onItemClick = {}
        )
    }
}