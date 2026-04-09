package com.mobile.travelhub.navigation

import androidx.compose.animation.AnimatedContentTransitionScope.SlideDirection
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.mobile.travelhub.ui.screens.ItineraryBotScreen
import com.mobile.travelhub.ui.screens.ProfileScreen
import com.mobile.travelhub.ui.screens.EditPlaceScreen
import com.mobile.travelhub.ui.screens.OnboardingFinishScreen
import com.mobile.travelhub.ui.screens.OnboardingIntroScreen
import com.mobile.travelhub.ui.screens.OnboardingVibeScreen
import com.mobile.travelhub.ui.screens.LoginScreen
import com.mobile.travelhub.ui.screens.PlaceDetailScreen
import com.mobile.travelhub.ui.screens.PlaceListScreen

import com.mobile.travelhub.ui.screens.RegisterScreen
import com.mobile.travelhub.viewmodels.AuthUiState
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
//import com.mobile.travelhub.ui.screens.PostDetailScreen
import com.mobile.travelhub.ui.screens.ProfileScreen

sealed class Screen(
    val route: String,
    val index: Int = -1,
    val showBottomBar: Boolean = false
) {
    data object OnboardingIntro : Screen("onboarding-intro", -3)
    data object OnboardingVibe : Screen("onboarding-vibe", -2)
    data object OnboardingFinish : Screen("onboarding-finish", -1)
    data object Home : Screen("home", 0, true)
    data object Trips : Screen("trips", 1, true)
    data object Profile : Screen("profile", 2, true)
    data object Chat : Screen("chat", 3, true)
    data object PlaceDetail : Screen("place/{placeId}", 10) {
        fun createRoute(placeId: String): String = "place/$placeId"
    }
    data object EditPlace : Screen("place/{placeId}/edit", 11) {
        fun createRoute(placeId: String): String = "place/$placeId/edit"
    }

    data object Login : Screen("login")
    data object Register : Screen("register")
    //merge from truong
    data object OtherProfile : Screen("profile_user/{userId}", 2) {
        fun createRoute(userId: Long) = "profile_user/$userId"
    }
    data object EditProfile : Screen("edit_profile", 3)
    data object FollowersFollowing : Screen("followers_following/{tabIndex}/{userId}", 5) {
        fun createRoute(tabIndex: Int, userId: Long? = null): String {
            val normalizedUserId = userId ?: -1L
            return "followers_following/$tabIndex/$normalizedUserId"
        }
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

    companion object {
        fun fromRoute(route: String?): Screen? {
            return when (route?.substringBefore("/")) {
                OnboardingIntro.route -> OnboardingIntro
                OnboardingVibe.route -> OnboardingVibe
                OnboardingFinish.route -> OnboardingFinish
                Home.route -> Home
                Trips.route -> Trips
                Profile.route -> Profile
                Chat.route -> Chat
                PlaceDetail.route -> PlaceDetail
                EditPlace.route -> EditPlace
                Login.route -> Login
                Register.route -> Register
                //mẻge from trường
                "home" -> Home
                "trips" -> Trips
                "profile" -> Profile
                "profile_user" -> Profile
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
fun NavGraph(
    navController: NavHostController,
    innerPadding: PaddingValues,
    startDestination: String,
    authUiState: AuthUiState,
    onLogin: (String, String) -> Unit,
    onRegister: (String, String, String) -> Unit,
    onClearAuthError: () -> Unit
) {
    LaunchedEffect(authUiState.isAuthenticated) {
        if (authUiState.isAuthenticated) {
            navController.navigate(Screen.Home.route) {
                popUpTo(Screen.Login.route) { inclusive = true }
                launchSingleTop = true
            }
        }
    }

    NavHost(
        navController = navController,
//        startDestination = Screen.OnboardingIntro.route,
        startDestination = startDestination,
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
        composable(Screen.Login.route) {
            LoginScreen(
                uiState = authUiState,
                onLogin = onLogin,
                onNavigateToRegister = { navController.navigate(Screen.Register.route) },
                onDismissError = onClearAuthError
            )
        }
        composable(Screen.Register.route) {
            RegisterScreen(
                uiState = authUiState,
                onRegister = onRegister,
                onNavigateToLogin = { navController.popBackStack() },
                onDismissError = onClearAuthError
            )
        }
        composable(Screen.OnboardingIntro.route) {
            OnboardingIntroScreen(
                onSkip = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.OnboardingIntro.route) { inclusive = true }
                    }
                },
                onContinue = { navController.navigate(Screen.OnboardingVibe.route) },
                onPrevious = { navController.popBackStack() },
                onBack = { navController.popBackStack() }
            )
        }
        composable(Screen.OnboardingVibe.route) {
            OnboardingVibeScreen(
                onSkip = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.OnboardingIntro.route) { inclusive = true }
                    }
                },
                onContinue = { selectedVibes ->
                    navController.currentBackStackEntry
                        ?.savedStateHandle
                        ?.set("selected_vibes", ArrayList(selectedVibes))
                    navController.navigate(Screen.OnboardingFinish.route)
                },
                onPrevious = { navController.navigateUp() },
                onBack = { navController.popBackStack() }
            )
        }
        composable(Screen.OnboardingFinish.route) {
            val selectedVibes = navController.previousBackStackEntry
                ?.savedStateHandle
                ?.get<ArrayList<String>>("selected_vibes")
                ?.toList()
                .orEmpty()

            OnboardingFinishScreen(
                selectedVibes = selectedVibes,
                onSkip = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.OnboardingIntro.route) { inclusive = true }
                    }
                },
                onContinue = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.OnboardingIntro.route) { inclusive = true }
                    }
                },
                onPrevious = { navController.navigateUp() },
                onBack = { navController.navigateUp() }
            )
        }
        composable(Screen.Home.route) {
            PlaceListScreen(
                onPlaceClick = { placeId ->
                    navController.navigate(Screen.PlaceDetail.createRoute(placeId))
                }
            )
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
                onNavigateToFollowers = { navController.navigate(Screen.FollowersFollowing.createRoute(0, null)) { launchSingleTop = true } },
                onNavigateToFollowing = { navController.navigate(Screen.FollowersFollowing.createRoute(1, null)) { launchSingleTop = true } },
                onNavigateToChat = { navController.navigate(Screen.Chat.route) { launchSingleTop = true } }
            )
        }
        composable(
            route = Screen.OtherProfile.route,
            arguments = listOf(navArgument("userId") { type = NavType.LongType })
        ) { backStackEntry ->
            val userId = backStackEntry.arguments?.getLong("userId") ?: return@composable
            ProfileScreen(
                onNavigateToEditProfile = {},
                onNavigateToFollowers = { navController.navigate(Screen.FollowersFollowing.createRoute(0, userId)) { launchSingleTop = true } },
                onNavigateToFollowing = { navController.navigate(Screen.FollowersFollowing.createRoute(1, userId)) { launchSingleTop = true } },
                viewingUserId = userId,
                onNavigateToChat = {
                    navController.navigate(Screen.Chat.route) {
                        launchSingleTop = true
                    }
                },
                onBack = {
                    val poppedToOwnProfile = navController.popBackStack(Screen.Profile.route, false)
                    if (!poppedToOwnProfile) {
                        navController.popBackStack()
                    }
                }
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
            arguments = listOf(
                navArgument("tabIndex") { type = NavType.IntType },
                navArgument("userId") { type = NavType.LongType }
            )
        ) { backStackEntry ->
            val tabIndex = backStackEntry.arguments?.getInt("tabIndex") ?: 0
            val targetUserId = backStackEntry.arguments?.getLong("userId")?.takeIf { it > 0L }
            FollowersFollowingScreen(
                initialTabIndex = tabIndex,
                viewingUserId = targetUserId,
                onBack = { navController.popBackStack() },
                onNavigateToUserProfile = { userId ->
                    if (userId == null) {
                        val poppedToOwnProfile = navController.popBackStack(Screen.Profile.route, false)
                        if (!poppedToOwnProfile) {
                            navController.navigate(Screen.Profile.route) { launchSingleTop = true }
                        }
                    } else {
                        navController.navigate(Screen.OtherProfile.createRoute(userId)) { launchSingleTop = true }
                    }
                }
            )
        }
        composable(Screen.PostDetail.route) {
//            PostDetailScreen(
//                onBack = { navController.popBackStack() }
//            )
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
        composable(Screen.PlaceDetail.route) { backStackEntry ->
            val placeId = backStackEntry.arguments?.getString("placeId").orEmpty()
            PlaceDetailScreen(
                placeId = placeId,
                onBack = { navController.navigateUp() },
                onEdit = { id ->
                    navController.navigate(Screen.EditPlace.createRoute(id))
                }
            )
        }
        composable(Screen.EditPlace.route) { backStackEntry ->
            val placeId = backStackEntry.arguments?.getString("placeId").orEmpty()
            EditPlaceScreen(
                placeId = placeId,
                onBack = { navController.navigateUp() },
                onSaved = { navController.navigateUp() }
            )
        }
    }
}
