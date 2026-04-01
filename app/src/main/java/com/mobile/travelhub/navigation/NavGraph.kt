package com.mobile.travelhub.navigation

import androidx.compose.animation.AnimatedContentTransitionScope.SlideDirection
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.mobile.travelhub.ui.screens.CostEstimateScreen
import com.mobile.travelhub.ui.screens.CreateGroupScreen
import com.mobile.travelhub.ui.screens.EditProfileScreen
import com.mobile.travelhub.ui.screens.FollowersFollowingScreen
import com.mobile.travelhub.ui.screens.GroupChatScreen
import com.mobile.travelhub.ui.screens.GroupDetailScreen
import com.mobile.travelhub.ui.screens.GroupDiscoveryScreen
import com.mobile.travelhub.ui.screens.HomeScreen
import com.mobile.travelhub.ui.screens.ItineraryBotScreen
import com.mobile.travelhub.ui.screens.TripsScreen
import com.mobile.travelhub.ui.screens.ItineraryScreen
import com.mobile.travelhub.ui.screens.PostDetailScreen
import com.mobile.travelhub.ui.screens.ProfileScreen

sealed class Screen(val route: String, val index: Int) {
    data object Home : Screen("home", 0)
    data object Trips : Screen("trips", 1)
    data object Profile : Screen("profile", 2)
    data object EditProfile : Screen("edit_profile", 3)
    data object FollowersFollowing : Screen("followers_following/{tabIndex}", 5) {
        fun createRoute(tabIndex: Int) = "followers_following/$tabIndex"
    }
    data object PostDetail : Screen("post_detail", 6)
    
    data object CreateGroup : Screen("create_group", 7)
    data object GroupDetail : Screen("group_detail/{groupName}", 8) {
        fun createRoute(groupName: String) = "group_detail/$groupName"
    }
    data object GroupChat : Screen("group_chat/{groupName}", 9) {
        fun createRoute(groupName: String) = "group_chat/$groupName"
    }
    data object Itinerary : Screen("itinerary/{groupName}", 10) {
        fun createRoute(groupName: String) = "itinerary/$groupName"
    }
    data object CostEstimate : Screen("cost_estimate/{groupName}", 11) {
        fun createRoute(groupName: String) = "cost_estimate/$groupName"
    }

    data object Chat : Screen("chat", 3)

    companion object {
        fun fromRoute(route: String?): Screen? {
            return when (route?.substringBefore("/")) {
                "home" -> Home
                "trips" -> Trips
                "profile" -> Profile
                "edit_profile" -> EditProfile
                "followers_following" -> FollowersFollowing
                "post_detail" -> PostDetail
                "create_group" -> CreateGroup
                "group_detail" -> GroupDetail
                "group_chat" -> GroupChat
                "itinerary" -> Itinerary
                "cost_estimate" -> CostEstimate
                else -> null
            }
        }
    }
}
fun getDirection(
    initialState: NavBackStackEntry,
    targetState: NavBackStackEntry
): SlideDirection {
    val fromIndex = Screen.fromRoute(initialState.destination.route)?.index ?: 0
    val toIndex = Screen.fromRoute(targetState.destination.route)?.index ?: 0
    return if (toIndex > fromIndex) {
        SlideDirection.Left
    } else {
        SlideDirection.Right
    }
}
@Composable
fun NavGraph(navController: NavHostController, innerPadding: PaddingValues) {
    NavHost(
        navController = navController,
        startDestination = Screen.Home.route,
        enterTransition = {
            slideIntoContainer(
                towards = getDirection(initialState, targetState),
                animationSpec = tween(300)
            )
        },
        exitTransition = {
            slideOutOfContainer(
                towards = getDirection(initialState, targetState),
                animationSpec = tween(300)
            )
        },
        modifier = Modifier.padding(innerPadding)
    ) {
        composable(Screen.Home.route) {
            HomeScreen()
        }
        composable(Screen.Trips.route) {
            GroupDiscoveryScreen(
                onNavigateToCreateGroup = { navController.navigate(Screen.CreateGroup.route) { launchSingleTop = true } },
                onNavigateToGroupDetail = { groupName -> 
                    navController.navigate(Screen.GroupDetail.createRoute(groupName)) { launchSingleTop = true } 
                }
            )
        }
        composable(Screen.Profile.route) {
            ProfileScreen(
                onNavigateToEditProfile = { navController.navigate(Screen.EditProfile.route) { launchSingleTop = true } },
                onNavigateToFollowers = { navController.navigate(Screen.FollowersFollowing.createRoute(0)) { launchSingleTop = true } },
                onNavigateToFollowing = { navController.navigate(Screen.FollowersFollowing.createRoute(1)) { launchSingleTop = true } },
                onNavigateToPostDetail = { navController.navigate(Screen.PostDetail.route) { launchSingleTop = true } }
            )
        }
        composable(Screen.EditProfile.route) {
            EditProfileScreen(
                onBack = { navController.popBackStack() },
                onSaveSuccess = { navController.popBackStack() }
            )
        }
        composable(
            route = Screen.FollowersFollowing.route,
            arguments = listOf(navArgument("tabIndex") { type = NavType.IntType })
        ) { backStackEntry ->
            val tabIndex = backStackEntry.arguments?.getInt("tabIndex") ?: 0
            FollowersFollowingScreen(
                initialTabIndex = tabIndex,
                onBack = { navController.popBackStack() }
            )
        }
        composable(Screen.PostDetail.route) {
            PostDetailScreen(
                onBack = { navController.popBackStack() }
            )
        }
        
        composable(Screen.CreateGroup.route) {
            CreateGroupScreen(
                onBack = { navController.popBackStack() },
                onCreate = { navController.popBackStack() }
            )
        }
        
        composable(
            route = Screen.GroupDetail.route,
            arguments = listOf(navArgument("groupName") { type = NavType.StringType })
        ) { backStackEntry ->
            val groupName = backStackEntry.arguments?.getString("groupName") ?: "Group"
            GroupDetailScreen(
                groupName = groupName,
                onBack = { navController.popBackStack() },
                onNavigateToChat = { navController.navigate(Screen.GroupChat.createRoute(groupName)) { launchSingleTop = true } },
                onNavigateToItinerary = { navController.navigate(Screen.Itinerary.createRoute(groupName)) { launchSingleTop = true } },
                onNavigateToCost = { navController.navigate(Screen.CostEstimate.createRoute(groupName)) { launchSingleTop = true } }
            )
        }
        
        composable(
            route = Screen.GroupChat.route,
            arguments = listOf(navArgument("groupName") { type = NavType.StringType })
        ) { backStackEntry ->
            val groupName = backStackEntry.arguments?.getString("groupName") ?: "Chat"
            GroupChatScreen(
                groupName = groupName,
                onBack = { navController.popBackStack() }
            )
        }
        
        composable(
            route = Screen.Itinerary.route,
            arguments = listOf(navArgument("groupName") { type = NavType.StringType })
        ) { backStackEntry ->
            val groupName = backStackEntry.arguments?.getString("groupName") ?: "Itinerary"
            ItineraryScreen(
                groupName = groupName,
                onBack = { navController.popBackStack() }
            )
        }
        
        composable(
            route = Screen.CostEstimate.route,
            arguments = listOf(navArgument("groupName") { type = NavType.StringType })
        ) { backStackEntry ->
            val groupName = backStackEntry.arguments?.getString("groupName") ?: "Cost Estimate"
            CostEstimateScreen(
                groupName = groupName,
                onBack = { navController.popBackStack() }
            )
        }
        composable(Screen.Chat.route) {
            ItineraryBotScreen()
        }
    }
}
