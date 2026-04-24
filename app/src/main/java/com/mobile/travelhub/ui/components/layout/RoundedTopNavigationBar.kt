package com.mobile.travelhub.ui.components.layout

import androidx.activity.compose.LocalOnBackPressedDispatcherOwner
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.sp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.mobile.travelhub.navigation.Screen


data class BottomNavItem(
    val screen: Screen?,
    val icon: ImageVector,
    val contentDescription: String,
    val badgeCount: Int = 0
)

@Composable
fun RoundedTopNavigationBar(
    items: List<BottomNavItem>,
    navController: NavHostController
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val backPressedDispatcher = LocalOnBackPressedDispatcherOwner.current?.onBackPressedDispatcher

    fun isProfileRoute(route: String?): Boolean {
        return route?.startsWith(Screen.Profile.route) == true ||
                route?.startsWith("profile_user") == true ||
                route?.startsWith("edit_profile") == true ||
                route?.startsWith("followers_following") == true
    }

    NavigationBar(
        modifier = Modifier,
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface
    ) {
        items.forEach { item ->
            val screen = item.screen
            val isSelected = when {
                screen == null -> false
                screen == Screen.Profile -> isProfileRoute(currentRoute)
                else -> currentRoute?.substringBefore("/") == screen.route
            }

            NavigationBarItem(
                selected = isSelected,
                onClick = {
                    if (screen == null) return@NavigationBarItem

                    if (screen == Screen.Profile && isProfileRoute(currentRoute) && currentRoute != Screen.Profile.route) {
                        backPressedDispatcher?.onBackPressed()
                    } else if (currentRoute?.substringBefore("/") != screen.route) {
                        navController.navigate(screen.route) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                },
                icon = {
                    if (item.badgeCount > 0 && !isSelected) {
                        BadgedBox(
                            badge = {
                                Badge(
                                    containerColor = MaterialTheme.colorScheme.primary,
                                    contentColor = MaterialTheme.colorScheme.onPrimary
                                ) {
                                    androidx.compose.material3.Text(
                                        text = item.badgeCount.toString(),
                                        fontSize = 9.sp
                                    )
                                }
                            }
                        ) {
                            Icon(
                                imageVector = item.icon,
                                contentDescription = item.contentDescription
                            )
                        }
                    } else {
                        Icon(
                            imageVector = item.icon,
                            contentDescription = item.contentDescription
                        )
                    }
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.onPrimary,
                    selectedTextColor = MaterialTheme.colorScheme.primary,
                    indicatorColor = MaterialTheme.colorScheme.primary,
                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
        }
    }
}
