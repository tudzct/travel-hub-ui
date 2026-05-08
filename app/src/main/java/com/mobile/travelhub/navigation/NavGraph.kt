package com.mobile.travelhub.navigation

import android.net.Uri
import androidx.compose.animation.AnimatedContentTransitionScope.SlideDirection
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.mobile.travelhub.data.model.TravelPlaceListItemResponse
import com.mobile.travelhub.ui.screens.OnboardingInterestsScreen
import com.mobile.travelhub.ui.screens.OnboardingIntroScreen
import com.mobile.travelhub.ui.screens.ProfileScreen
import com.mobile.travelhub.ui.screens.OnboardingFinishScreen
import com.mobile.travelhub.ui.screens.OnboardingTripTypeScreen
import com.mobile.travelhub.ui.screens.LoginScreen
import com.mobile.travelhub.ui.screens.PlaceDetailScreen
import com.mobile.travelhub.ui.screens.PlaceListScreen
import com.mobile.travelhub.ui.screens.ReviewListScreen

import com.mobile.travelhub.ui.screens.RegisterScreen
import com.mobile.travelhub.ui.screens.ViewHistoryScreen
import com.mobile.travelhub.viewmodels.AuthUiState
import androidx.navigation.navArgument
import com.mobile.travelhub.ui.screens.*
import com.mobile.travelhub.viewmodels.OnboardingViewModel

private const val PLACE_DETAIL_PLACE_KEY = "place_detail_place"

sealed class Screen(
    val route: String,
    val index: Int = -1,
    val showBottomBar: Boolean = false
) {
    data object OnboardingTripType : Screen("onboarding-trip-type", -5)
    data object OnboardingIntro : Screen("onboarding-intro", -4)
    data object OnboardingDestination : Screen("onboarding-destination", -3)
    data object OnboardingFinish : Screen("onboarding-finish", -2)
    data object Home : Screen("home", 0, true)
    data object Trips : Screen("trips", 1, true)
    data object CreatePost : Screen("create_post", showBottomBar = true)
    data object Profile : Screen("profile", 2, true)
    data object Chat : Screen("chat", 3, true)
    data object PlaceDetail : Screen("place/{placeId}", 10) {
        fun createRoute(placeId: Long): String = "place/$placeId"
    }
    data object PlaceReviews : Screen("place/{placeId}/reviews", 11) {
        fun createRoute(placeId: Long): String = "place/$placeId/reviews"
    }
    data object ViewHistory : Screen("history/places", 14)

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

    data object CreateGroup : Screen("create_group", 7)
    data object GroupDetail : Screen("group_detail/{tripId}/{groupName}", 8) {
        fun createRoute(tripId: Long, groupName: String): String = "group_detail/$tripId/${Uri.encode(groupName)}"
    }
    data object GroupChat : Screen("group_chat/{groupName}", 9) {
        fun createRoute(groupName: String) = "group_chat/$groupName"
    }
    data object Itinerary : Screen("itinerary/{groupName}", 10) {
        fun createRoute(groupName: String) = "itinerary/$groupName"
    }
    data object ItineraryDayDetail : Screen("itinerary/{groupName}/day/{dayIndex}", 11) {
        fun createRoute(groupName: String, dayIndex: Int) = "itinerary/$groupName/day/$dayIndex"
    }
    data object CostEstimate : Screen("cost_estimate/{groupName}", 12) {
        fun createRoute(groupName: String) = "cost_estimate/$groupName"
    }
    data object GroupDiscovery : Screen("group_discovery", 13)
    data object RouteMap : Screen("route_map", 14)

    companion object {
        fun fromRoute(route: String?): Screen? {
            return when (route?.substringBefore("/")) {
                OnboardingIntro.route -> OnboardingIntro
                OnboardingTripType.route -> OnboardingTripType
                OnboardingDestination.route -> OnboardingDestination
                OnboardingFinish.route -> OnboardingFinish
                Home.route -> Home
                Trips.route -> Trips
                CreatePost.route -> CreatePost
                Profile.route -> Profile
                Chat.route -> Chat
                PlaceDetail.route -> PlaceDetail
                PlaceReviews.route -> PlaceReviews
                ViewHistory.route -> ViewHistory
                Login.route -> Login
                Register.route -> Register
                //mẻge from trường
                "home" -> Home
                "trips" -> Trips
                "create_post" -> CreatePost
                "profile" -> Profile
                "place" -> PlaceDetail
                "history" -> ViewHistory
                "profile_user" -> Profile
                "edit_profile" -> EditProfile
                "followers_following" -> FollowersFollowing
                "create_group" -> CreateGroup
                "group_detail" -> GroupDetail
                "group_chat" -> GroupChat
                "itinerary" -> Itinerary
                "cost_estimate" -> CostEstimate
                "group_discovery" -> GroupDiscovery
                "route_map" -> RouteMap
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
    onClearAuthError: () -> Unit,
    onLogout: () -> Unit,
    onCompleteOnboarding: () -> Unit,
    onboardingViewModel: OnboardingViewModel
) {
    val onboardingUiState by onboardingViewModel.uiState.collectAsState()
    val currentRoute = navController.currentBackStackEntry?.destination?.route?.substringBefore("/")

    fun navigateToPlaceDetail(place: TravelPlaceListItemResponse) {
        navController.currentBackStackEntry?.savedStateHandle?.set(PLACE_DETAIL_PLACE_KEY, place)
        navController.navigate(Screen.PlaceDetail.createRoute(place.id))
    }

    LaunchedEffect(authUiState.isAuthenticated, currentRoute) {
        val isAuthRoute = currentRoute == Screen.Login.route || currentRoute == Screen.Register.route
        if (authUiState.isAuthenticated && isAuthRoute) {
            val destination = if (!authUiState.isOnboarded) {
                Screen.OnboardingTripType.route
            } else {
                Screen.Home.route
            }
            navController.navigate(destination) {
                popUpTo(Screen.Login.route) { inclusive = true }
                launchSingleTop = true
            }
        }
    }

    NavHost(
        navController = navController,
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
        composable(Screen.OnboardingTripType.route) {
            OnboardingTripTypeScreen(
                onSkip = {
                    onCompleteOnboarding()
                    val destination = if (authUiState.isAuthenticated) Screen.Home.route else Screen.Login.route
                    navController.navigate(destination) {
                        popUpTo(Screen.OnboardingIntro.route) { inclusive = true }
                    }
                },
                onContinue = { selectedTripType ->
                    onboardingViewModel.updateTripType(selectedTripType)
                    navController.navigate(Screen.OnboardingIntro.route)
                },
                onPrevious = { navController.popBackStack() },
                onBack = { navController.popBackStack() }
            )
        }
        composable(Screen.OnboardingIntro.route) {
            OnboardingInterestsScreen(
                initialSelected = onboardingUiState.interests,
                onSkip = {
                    onCompleteOnboarding()
                    val destination = if (authUiState.isAuthenticated) Screen.Home.route else Screen.Login.route
                    navController.navigate(destination) {
                        popUpTo(Screen.OnboardingIntro.route) { inclusive = true }
                    }
                },
                onContinue = { selectedInterests ->
                    onboardingViewModel.updateInterests(selectedInterests)
                    navController.navigate(Screen.OnboardingDestination.route)
                },
                onPrevious = { navController.navigateUp() },
                onBack = { navController.popBackStack() }
            )
        }
        composable(Screen.OnboardingDestination.route) {
            OnboardingIntroScreen(
                onSkip = {
                    onCompleteOnboarding()
                    val destination = if (authUiState.isAuthenticated) Screen.Home.route else Screen.Login.route
                    navController.navigate(destination) {
                        popUpTo(Screen.OnboardingIntro.route) { inclusive = true }
                    }
                },
                onContinue = {
                    navController.navigate(Screen.OnboardingFinish.route)
                },
                onPrevious = { navController.navigateUp() },
                onBack = { navController.popBackStack() }
            )
        }
        composable(Screen.OnboardingFinish.route) {

            OnboardingFinishScreen(
                selectedInterests = onboardingUiState.interests,
                selectedTripType = onboardingUiState.tripType,
                selectedDestination = onboardingUiState.destination,
                startDate = onboardingUiState.startDate,
                endDate = onboardingUiState.endDate,
                travelers = onboardingUiState.travelers,
                budgetLevel = onboardingUiState.budgetLevel,
                isSyncingPreferences = onboardingUiState.isSyncingPreferences,
                syncErrorMessage = onboardingUiState.preferenceSyncErrorMessage,
                onSkip = {
                    onCompleteOnboarding()
                    val destination = if (authUiState.isAuthenticated) Screen.Home.route else Screen.Login.route
                    navController.navigate(destination) {
                        popUpTo(Screen.OnboardingIntro.route) { inclusive = true }
                    }
                },
                onContinue = {
                    if (!authUiState.isAuthenticated) {
                        navController.navigate(Screen.Login.route) {
                            popUpTo(Screen.OnboardingIntro.route) { inclusive = true }
                        }
                    } else {
                        onboardingViewModel.syncPreferences {
                            onCompleteOnboarding()
                            navController.navigate(Screen.Home.route) {
                                popUpTo(Screen.OnboardingIntro.route) { inclusive = true }
                            }
                        }
                    }
                },
                onPrevious = { navController.navigateUp() },
                onBack = { navController.navigateUp() }
            )
        }
        composable(Screen.Home.route) {
            PlaceListScreen(
                onPlaceClick = ::navigateToPlaceDetail
            )
        }
        composable(Screen.Trips.route) {
            TripsScreen(
                onNavigateToGroupDetail = { tripId, groupName ->
                    navController.navigate(Screen.GroupDetail.createRoute(tripId, groupName)) { launchSingleTop = true }
                },
                onNavigateToCreateGroup = {
                    navController.navigate(Screen.CreateGroup.route) { launchSingleTop = true }
                }
            )
//            GroupDiscoveryScreen(
//                onNavigateToCreateGroup = { navController.navigate(Screen.CreateGroup.route) { launchSingleTop = true } },
//                onNavigateToGroupDetail = { groupName ->
//                    navController.navigate(Screen.GroupDetail.createRoute(groupName)) { launchSingleTop = true }
//                }
//            )
        }
        composable(Screen.CreatePost.route) {
            CreatePostScreen()
        }
        composable(Screen.Profile.route) {
            if (!authUiState.isAuthenticated) {
                LaunchedEffect(Unit) {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(navController.graph.findStartDestination().id) { inclusive = false }
                        launchSingleTop = true
                    }
                }
                return@composable
            }
            //^^^^ tu^^^^
            ProfileScreen(
                onNavigateToEditProfile = { navController.navigate(Screen.EditProfile.route) { launchSingleTop = true } },
                onNavigateToFollowers = { navController.navigate(Screen.FollowersFollowing.createRoute(0, null)) { launchSingleTop = true } },
                onNavigateToFollowing = { navController.navigate(Screen.FollowersFollowing.createRoute(1, null)) { launchSingleTop = true } },
                onNavigateToHistory = { navController.navigate(Screen.ViewHistory.route) { launchSingleTop = true } },
                onNavigateToChat = { navController.navigate(Screen.Chat.route) { launchSingleTop = true } },
                onLogout = {
                    onLogout()
                    navController.navigate(Screen.Login.route) {
                        popUpTo(navController.graph.findStartDestination().id) { inclusive = false }
                        launchSingleTop = true
                    }
                },
                onRequireLogin = {
                    onLogout()
                    navController.navigate(Screen.Login.route) {
                        popUpTo(navController.graph.findStartDestination().id) { inclusive = false }
                        launchSingleTop = true
                    }
                }
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
                onNavigateToHistory = null,
                onRequireLogin = null,
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

        composable(Screen.CreateGroup.route) {
            CreateGroupScreen(
                onBack = { navController.popBackStack() },
                onCreate = { tripId, groupName ->
                    navController.navigate(Screen.GroupDetail.createRoute(tripId, groupName)) { launchSingleTop = true }
                }
            )
        }

        composable(
            route = Screen.GroupDetail.route,
            arguments = listOf(
                navArgument("tripId") { type = NavType.LongType },
                navArgument("groupName") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val tripId = backStackEntry.arguments?.getLong("tripId") ?: -1L
            val groupName = backStackEntry.arguments?.getString("groupName") ?: "Group"
            GroupDetailScreen(
                tripId = tripId,
                groupName = groupName,
                onBack = { navController.popBackStack() },
                onNavigateToChat = { navController.navigate(Screen.GroupChat.createRoute(groupName)) { launchSingleTop = true } },
                onNavigateToItinerary = { navController.navigate(Screen.Itinerary.createRoute(groupName)) { launchSingleTop = true } },
                onNavigateToDiscovery = { navController.navigate(Screen.GroupDiscovery.route) { launchSingleTop = true } },
                onNavigateToMap = { navController.navigate(Screen.RouteMap.route) { launchSingleTop = true } },
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
                onBack = { navController.popBackStack() },
                onOpenDayDetail = { dayIndex ->
                    navController.navigate(Screen.ItineraryDayDetail.createRoute(groupName, dayIndex)) {
                        launchSingleTop = true
                    }
                }
            )
        }

        composable(
            route = Screen.ItineraryDayDetail.route,
            arguments = listOf(
                navArgument("groupName") { type = NavType.StringType },
                navArgument("dayIndex") { type = NavType.IntType }
            )
        ) { backStackEntry ->
            val groupName = backStackEntry.arguments?.getString("groupName") ?: "Itinerary"
            val dayIndex = backStackEntry.arguments?.getInt("dayIndex") ?: 1
            ItineraryDayDetailScreen(
                groupName = groupName,
                dayIndex = dayIndex,
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

        composable(
            route = Screen.PlaceDetail.route,
            arguments = listOf(navArgument("placeId") { type = NavType.LongType })
        ) { backStackEntry ->
            val place = backStackEntry.savedStateHandle.get<TravelPlaceListItemResponse>(PLACE_DETAIL_PLACE_KEY)
                ?: navController.previousBackStackEntry?.savedStateHandle?.get<TravelPlaceListItemResponse>(PLACE_DETAIL_PLACE_KEY)
                    ?.also { backStackEntry.savedStateHandle[PLACE_DETAIL_PLACE_KEY] = it }
                ?: return@composable
            PlaceDetailScreen(
                place = place,
                onBack = { navController.navigateUp() },
                onPlaceClick = ::navigateToPlaceDetail,
                onShowAllReviews = { id -> navController.navigate(Screen.PlaceReviews.createRoute(id)) },
                onRequireLogin = {
                    onLogout()
                    navController.navigate(Screen.Login.route) {
                        popUpTo(navController.graph.findStartDestination().id) { inclusive = false }
                        launchSingleTop = true
                    }
                }
            )
        }

        composable(
            route = Screen.PlaceReviews.route,
            arguments = listOf(navArgument("placeId") { type = NavType.LongType })
        ) { backStackEntry ->
            val placeId = backStackEntry.arguments?.getLong("placeId") ?: return@composable
            ReviewListScreen(
                placeId = placeId,
                onBack = { navController.navigateUp() }
            )
        }

        composable(Screen.ViewHistory.route) {
            if (!authUiState.isAuthenticated) {
                LaunchedEffect(Unit) {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(navController.graph.findStartDestination().id) { inclusive = false }
                        launchSingleTop = true
                    }
                }
                return@composable
            }
            ViewHistoryScreen(
                onBack = { navController.navigateUp() },
                onRequireLogin = {
                    onLogout()
                    navController.navigate(Screen.Login.route) {
                        popUpTo(navController.graph.findStartDestination().id) { inclusive = false }
                        launchSingleTop = true
                    }
                }
            )
        }
        composable(Screen.GroupDiscovery.route) {
            GroupDiscoveryScreen(
                onBack = { navController.popBackStack() }
            )
        }

        composable(Screen.RouteMap.route) {
            RouteMapScreen(
                onBack = { navController.popBackStack() }
            )
        }
    }
}
