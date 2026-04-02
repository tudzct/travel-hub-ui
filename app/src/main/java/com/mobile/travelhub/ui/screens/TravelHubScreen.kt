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
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.mobile.travelhub.navigation.NavGraph
import com.mobile.travelhub.navigation.Screen


@Composable
fun TravelHubScreen() {
    val navController = rememberNavController()
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

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            if (currentRoute !in onboardingRoutes) {
                RoundedTopNavigationBar(
                    items = navItems,
                    navController = navController
                )
            }
        }
    ) { innerPadding ->
        NavGraph(navController = navController, innerPadding)
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