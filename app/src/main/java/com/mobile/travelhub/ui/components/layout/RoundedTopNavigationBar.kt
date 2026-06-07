package com.mobile.travelhub.ui.components.layout

import androidx.activity.compose.LocalOnBackPressedDispatcherOwner
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.material.icons.outlined.TravelExplore
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.mobile.travelhub.R
import com.mobile.travelhub.navigation.Screen

data class BottomNavItem(
    val screen: Screen?,
    val icon: ImageVector,
    val selectedIcon: ImageVector = icon,
    val label: String,
    val contentDescription: String = label,
    val badgeCount: Int = 0
)

@Composable
fun RoundedTopNavigationBar(
    items: List<BottomNavItem>,
    navController: NavHostController,
    onHomeReselected: () -> Unit = {}
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

    fun baseRoute(route: String?): String? {
        return route
            ?.substringBefore("?")
            ?.substringBefore("/")
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = 1.dp,
                color = Color.LightGray.copy(alpha = 0.4f),
            ),
        shadowElevation = 12.dp,
        tonalElevation = 0.dp,
        color = MaterialTheme.colorScheme.surface
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .height(68.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            items.forEach { item ->
                val screen = item.screen
                val isSelected = when {
                    screen == null -> false
                    screen == Screen.Profile -> isProfileRoute(currentRoute)
                    else -> baseRoute(currentRoute) == screen.route
                }

                val iconColor by animateColorAsState(
                    targetValue = if (isSelected)
                        MaterialTheme.colorScheme.primary
                    else
                        MaterialTheme.colorScheme.onSurfaceVariant,
                    animationSpec = spring(),
                    label = "iconColor"
                )

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(50))
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) {
                            if (screen == null) return@clickable
                            val currentBaseRoute = baseRoute(currentRoute)
                            if (screen == Screen.Home) {
                                onHomeReselected()
                                if (currentBaseRoute != Screen.Home.route) {
                                    navController.navigate(screen.route) {
                                        popUpTo(navController.graph.findStartDestination().id) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            } else if (screen == Screen.Profile && isProfileRoute(currentRoute) && currentRoute != Screen.Profile.route) {
                                backPressedDispatcher?.onBackPressed()
                            } else if (currentBaseRoute != screen.route) {
                                navController.navigate(screen.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = if (isSelected) item.selectedIcon else item.icon,
                            contentDescription = item.contentDescription,
                            tint = iconColor,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = item.label,
                            color = iconColor,
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                            maxLines = 1,
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                }
            }
        }
    }
}

@Preview
@Composable
fun RoundedTopNavigationBarPreview() {
    val navController = rememberNavController()
    RoundedTopNavigationBar(
        items = listOf(
            BottomNavItem(
                Screen.Home,
                Icons.Outlined.Home,
                Icons.Filled.Home,
                stringResource(R.string.nav_home)
            ),
            BottomNavItem(
                Screen.Trips,
                Icons.Outlined.TravelExplore,
                Icons.Filled.TravelExplore,
                stringResource(R.string.nav_explore)
            ),
            BottomNavItem(
                Screen.Trips,
                Icons.AutoMirrored.Outlined.DirectionsWalk,
                Icons.AutoMirrored.Filled.DirectionsWalk,
                stringResource(R.string.nav_itinerary)
            ),
            BottomNavItem(
                Screen.CreatePost,
                Icons.Outlined.Add,
                Icons.Filled.PostAdd,
                stringResource(R.string.nav_create)
            ),
            BottomNavItem(
                Screen.Profile,
                Icons.Outlined.AccountCircle,
                Icons.Filled.AccountCircle,
                stringResource(R.string.nav_profile)
            )
        ),
        navController = navController
    )
}
