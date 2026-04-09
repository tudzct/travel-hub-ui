package com.mobile.travelhub.ui.components.layout

import androidx.activity.compose.LocalOnBackPressedDispatcherOwner
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
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

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 12.dp)
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(30.dp),
            color = Color.White,
            shadowElevation = 10.dp
        ) {
            androidx.compose.foundation.layout.Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                items.forEach { item ->
                    val screen = item.screen
                    val isSelected = when {
                        screen == null -> false
                        screen == Screen.Profile -> isProfileRoute(currentRoute)
                        else -> currentRoute?.substringBefore("/") == screen.route
                    }

                    IconButton(
                        modifier = Modifier.weight(1f),
                        onClick = {
                            if (screen == null) return@IconButton

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
                        }
                    ) {
                        val iconTint = if (isSelected) Color(0xFFA8F65A) else Color(0xFF1A1D20)

                        Box(
                            modifier = if (isSelected) {
                                Modifier
                                    .size(36.dp)
                                    .clip(RoundedCornerShape(18.dp))
                                    .background(Color(0xFF0F1418))
                            } else {
                                Modifier.size(36.dp)
                            },
                            contentAlignment = Alignment.Center
                        ) {
                            if (item.badgeCount > 0 && !isSelected) {
                                BadgedBox(
                                    badge = {
                                        Badge(
                                            containerColor = Color(0xFFA8F65A),
                                            contentColor = Color(0xFF0F1418)
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
                                        contentDescription = item.contentDescription,
                                        tint = iconTint
                                    )
                                }
                            } else {
                                Icon(
                                    imageVector = item.icon,
                                    contentDescription = item.contentDescription,
                                    tint = iconTint
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
