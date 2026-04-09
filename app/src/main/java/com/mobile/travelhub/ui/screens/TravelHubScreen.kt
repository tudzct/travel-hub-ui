package com.mobile.travelhub.ui.screens

import androidx.activity.compose.LocalOnBackPressedDispatcherOwner
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
    val navItems = listOf(
        Screen.Home to "Home",
        Screen.Trips to "Trips",
        Screen.Profile to "Profile",
        Screen.Chat to "Chat"
    )

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            RoundedTopNavigationBar(
                items = navItems,
                navController = navController
            )
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
    val backPressedDispatcher = LocalOnBackPressedDispatcherOwner.current?.onBackPressedDispatcher

    fun isProfileRoute(route: String?): Boolean {
        return route?.startsWith(Screen.Profile.route) == true ||
               route?.startsWith("profile_user") == true ||
               route?.startsWith("edit_profile") == true ||
               route?.startsWith("followers_following") == true
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        tonalElevation = 8.dp
    ) {
        NavigationBar {
            items.forEach { (screen, label) ->
                val isSelected = if (screen == Screen.Profile) {
                    isProfileRoute(currentDestination?.route)
                } else {
                    currentDestination?.hierarchy?.any { it.route == screen.route } == true
                }

                NavigationBarItem(
                    selected = isSelected,
                    onClick = {
                        if (screen == Screen.Profile && isProfileRoute(currentDestination?.route) && currentDestination?.route != Screen.Profile.route) {
                            // Khi đang ở các trang con của Profile (như edit_profile), bấm lại vào tab Profile sẽ kích hoạt phím Back
                            // Điều này cho phép BackHandler bên trong EditProfileScreen bắt sự kiện và hiển thị Dialog cảnh báo
                            backPressedDispatcher?.onBackPressed()
                        } else if (currentDestination?.route != screen.route) {
                            navController.navigate(screen.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    },
                    icon = { Text(text = label.first().toString()) },
                    label = { Text(text = label) }
                )
            }
        }
    }
}
