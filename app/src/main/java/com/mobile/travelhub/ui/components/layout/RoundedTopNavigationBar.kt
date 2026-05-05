package com.mobile.travelhub.ui.components.layout

import androidx.activity.compose.LocalOnBackPressedDispatcherOwner
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.DirectionsWalk
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.mobile.travelhub.navigation.Screen

data class BottomNavItem(
    val screen: Screen?,
    val icon: ImageVector,
    val label: String,
    val contentDescription: String = label,
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

    Surface(
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 12.dp,
        tonalElevation = 0.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .height(68.dp)
                .padding(horizontal = 12.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            items.forEach { item ->
                val screen = item.screen
                val isSelected = when {
                    screen == null -> false
                    screen == Screen.Profile -> isProfileRoute(currentRoute)
                    else -> currentRoute?.substringBefore("/") == screen.route
                }

                val selectionProgress by animateFloatAsState(
                    targetValue = if (isSelected) 1f else 0f,
                    animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
                    label = "selection_${item.label}"
                )

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
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        modifier = Modifier
                            .graphicsLayer {
                                scaleX = 1f + 0.05f * selectionProgress
                                scaleY = 1f + 0.05f * selectionProgress
                            }
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f * selectionProgress))
                            .padding(
                                horizontal = (12 * selectionProgress).dp,
                                vertical = (8 * selectionProgress).dp
                            ),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = item.icon,
                            contentDescription = item.contentDescription,
                            tint = iconColor,
                            modifier = Modifier.size(22.dp)
                        )
                        if (selectionProgress < 0.5f) {
                            Spacer(modifier = Modifier.height(2.dp))
                        }
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
            BottomNavItem(Screen.Home, Icons.Outlined.Home, "Home"),
            BottomNavItem(Screen.Trips, Icons.Outlined.TravelExplore, "Explore"),
            BottomNavItem(Screen.Trips, Icons.AutoMirrored.Outlined.DirectionsWalk, "Itinerary"),
            BottomNavItem(Screen.CreatePost, Icons.Outlined.Add, "Create"),
            BottomNavItem(Screen.Profile, Icons.Outlined.AccountCircle, "Profile")
        ),
        navController = navController
    )
}