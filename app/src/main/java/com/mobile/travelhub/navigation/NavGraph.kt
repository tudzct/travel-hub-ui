package com.mobile.travelhub.navigation

import android.net.Uri
import androidx.compose.animation.AnimatedContentTransitionScope.SlideDirection
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.outlined.History
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.mobile.travelhub.data.model.TravelPlaceListItemResponse
import com.mobile.travelhub.data.model.TopTravelerPeriod
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
import kotlinx.coroutines.launch

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
    data object Explore : Screen("explore", 1, true) {
        const val ACTIVATE_SEARCH_ARG = "activateSearch"
        const val ROUTE_WITH_ARGS = "explore?activateSearch={activateSearch}"

        fun createRoute(activateSearch: Boolean = false): String {
            return if (activateSearch) {
                "explore?activateSearch=true"
            } else {
                route
            }
        }
    }
    data object Search : Screen("search", 2)
    data object Trips : Screen("trips", 1, true)
    data object CreatePost : Screen("create_post", showBottomBar = true)
    data object Profile : Screen("profile", 2, true)
    data object Chat : Screen("chat", 3, true)
    data object Notifications : Screen("notifications", 4)
    data object PostDetail : Screen("post/{postId}", 6) {
        fun createRoute(postId: Long): String = "post/$postId"
    }
    data object PlaceDetail : Screen("place/{placeId}", 10) {
        fun createRoute(placeId: Long): String = "place/$placeId"
    }
    data object PlaceReviews : Screen("place/{placeId}/reviews", 11) {
        fun createRoute(placeId: Long): String = "place/$placeId/reviews"
    }
    data object ViewHistory : Screen("history/places", 14)
    data object TopTravelers : Screen("top-travelers/{period}", 12) {
        fun createRoute(period: TopTravelerPeriod): String = "top-travelers/${period.name}"
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

    data object CreateGroup : Screen("create_group", 7)
    data object GroupDetail : Screen("group_detail/{tripId}/{groupName}", 8) {
        fun createRoute(tripId: Long, groupName: String): String = "group_detail/$tripId/${Uri.encode(groupName)}"
    }
    data object Itinerary : Screen("itinerary/{tripId}/{groupName}", 10) {
        fun createRoute(tripId: Long, groupName: String) = "itinerary/$tripId/${Uri.encode(groupName)}"
    }
    data object ItineraryDayDetail : Screen("itinerary/{tripId}/{groupName}/day/{dayIndex}", 11) {
        fun createRoute(tripId: Long, groupName: String, dayIndex: Int) =
            "itinerary/$tripId/${Uri.encode(groupName)}/day/$dayIndex"
    }
    data object CostEstimate : Screen("cost_estimate/{tripId}", 12) {
        fun createRoute(tripId: Long) = "cost_estimate/$tripId"
    }
    companion object {
        fun fromRoute(route: String?): Screen? {
            return when (route?.substringBefore("?")?.substringBefore("/")) {
                OnboardingIntro.route -> OnboardingIntro
                OnboardingTripType.route -> OnboardingTripType
                OnboardingDestination.route -> OnboardingDestination
                OnboardingFinish.route -> OnboardingFinish
                Home.route -> Home
                Explore.route -> Explore
                Search.route -> Search
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
                "explore" -> Explore
                "search" -> Search
                "trips" -> Trips
                "create_post" -> CreatePost
                "profile" -> Profile
                "notifications" -> Notifications
                "post" -> PostDetail
                "place" -> PlaceDetail
                "history" -> ViewHistory
                "top-travelers" -> TopTravelers
                "profile_user" -> Profile
                "edit_profile" -> EditProfile
                "followers_following" -> FollowersFollowing
                "create_group" -> CreateGroup
                "group_detail" -> GroupDetail
                "itinerary" -> Itinerary
                "cost_estimate" -> CostEstimate
                else -> null
            }
        }
    }
}

@Composable
private fun HomeDrawerScaffold(
    onNavigateToHistory: () -> Unit,
    onLogout: () -> Unit,
    content: @Composable (openMenu: () -> Unit) -> Unit
) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val coroutineScope = rememberCoroutineScope()
    var hideDrawerContentForNavigation by remember { mutableStateOf(false) }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            if (!hideDrawerContentForNavigation) {
                ModalDrawerSheet(
                    modifier = Modifier.width(280.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(top = 56.dp)
                    ) {
                        NavigationDrawerItem(
                            label = { androidx.compose.material3.Text("Recently viewed places") },
                            selected = false,
                            onClick = {
                                hideDrawerContentForNavigation = true
                                coroutineScope.launch {
                                    drawerState.snapTo(DrawerValue.Closed)
                                    withFrameNanos { }
                                    onNavigateToHistory()
                                }
                            },
                            icon = {
                                Icon(
                                    imageVector = Icons.Outlined.History,
                                    contentDescription = null
                                )
                            },
                            modifier = Modifier.padding(horizontal = 12.dp)
                        )
                        NavigationDrawerItem(
                            label = { androidx.compose.material3.Text("Logout") },
                            selected = false,
                            onClick = {
                                coroutineScope.launch { drawerState.close() }
                                onLogout()
                            },
                            icon = {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                                    contentDescription = null
                                )
                            },
                            modifier = Modifier.padding(horizontal = 12.dp)
                        )
                    }
                }
            }
        }
    ) {
        content {
            hideDrawerContentForNavigation = false
            coroutineScope.launch { drawerState.open() }
        }
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
    val currentRoute = navController.currentBackStackEntry?.destination?.route
        ?.substringBefore("?")
        ?.substringBefore("/")

    fun navigateToPlaceDetail(place: TravelPlaceListItemResponse) {
        navController.currentBackStackEntry?.savedStateHandle?.set(PLACE_DETAIL_PLACE_KEY, place)
        navController.navigate(Screen.PlaceDetail.createRoute(place.id))
    }

    fun navigateToUserProfile(userId: Long) {
        if (userId <= 0L) return
        val currentUserId = authUiState.session?.userId?.toLong()
        val route = if (currentUserId == userId) {
            Screen.Profile.route
        } else {
            Screen.OtherProfile.createRoute(userId)
        }
        navController.navigate(route) {
            launchSingleTop = true
        }
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
                towards = SlideDirection.Left,
                animationSpec = tween(300)
            )
        },
        exitTransition = {
            slideOutOfContainer(
                towards = SlideDirection.Left,
                animationSpec = tween(300)
            )
        },
        popEnterTransition = {
            slideIntoContainer(
                towards = SlideDirection.Right,
                animationSpec = tween(300)
            )
        },
        popExitTransition = {
            slideOutOfContainer(
                towards = SlideDirection.Right,
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
            HomeDrawerScaffold(
                onNavigateToHistory = { navController.navigate(Screen.ViewHistory.route) { launchSingleTop = true } },
                onLogout = {
                    onLogout()
                    navController.navigate(Screen.Login.route) {
                        popUpTo(navController.graph.findStartDestination().id) { inclusive = false }
                        launchSingleTop = true
                    }
                }
            ) { openMenu ->
                PlaceListScreen(
                    onPlaceClick = ::navigateToPlaceDetail,
                    onSearchClick = {
                        navController.navigate(Screen.Search.route) {
                            launchSingleTop = true
                        }
                    },
                    onAuthorClick = ::navigateToUserProfile,
                    onMenuClick = openMenu
                )
            }
        }
        composable(
            route = Screen.Explore.ROUTE_WITH_ARGS,
            arguments = listOf(
                navArgument(Screen.Explore.ACTIVATE_SEARCH_ARG) {
                    type = NavType.BoolType
                    defaultValue = false
                }
            )
        ) { backStackEntry ->
            val topTravelersRefreshKey by backStackEntry.savedStateHandle
                .getStateFlow("top_travelers_refresh", 0)
                .collectAsState()
            ExploreScreen(
                activateSearch = backStackEntry.arguments
                    ?.getBoolean(Screen.Explore.ACTIVATE_SEARCH_ARG)
                    ?: false,
                refreshTopTravelersKey = topTravelersRefreshKey,
                onSearchClick = {
                    navController.navigate(Screen.Search.route) {
                        launchSingleTop = true
                    }
                },
                onTravelerClick = { userId, currentUser ->
                    val route = if (currentUser) {
                        Screen.Profile.route
                    } else {
                        Screen.OtherProfile.createRoute(userId)
                    }
                    navController.navigate(route) { launchSingleTop = true }
                },
                onSeeAllTopTravelers = { period ->
                    navController.navigate(Screen.TopTravelers.createRoute(period)) {
                        launchSingleTop = true
                    }
                }
            )
        }
        composable(
            route = Screen.TopTravelers.route,
            arguments = listOf(navArgument("period") { type = NavType.StringType })
        ) { backStackEntry ->
            val period = runCatching {
                TopTravelerPeriod.valueOf(backStackEntry.arguments?.getString("period") ?: "WEEK")
            }.getOrDefault(TopTravelerPeriod.WEEK)
            TopTravelersScreen(
                initialPeriod = period,
                onBack = {
                    val handle = navController.previousBackStackEntry?.savedStateHandle
                    val nextRefreshKey = (handle?.get<Int>("top_travelers_refresh") ?: 0) + 1
                    handle?.set("top_travelers_refresh", nextRefreshKey)
                    navController.popBackStack()
                },
                onTravelerClick = { userId, currentUser ->
                    val route = if (currentUser) {
                        Screen.Profile.route
                    } else {
                        Screen.OtherProfile.createRoute(userId)
                    }
                    navController.navigate(route) { launchSingleTop = true }
                }
            )
        }
        composable(Screen.Search.route) {
            SearchPage(
                onBack = { navController.navigateUp() },
                onUserClick = ::navigateToUserProfile
            )
        }
        composable(Screen.Trips.route) { backStackEntry ->
            val createdTripId = backStackEntry.savedStateHandle.get<Long>("created_trip_id")
            val createdGroupName = backStackEntry.savedStateHandle.get<String>("created_group_name")
            backStackEntry.savedStateHandle.remove<Long>("created_trip_id")
            backStackEntry.savedStateHandle.remove<String>("created_group_name")

            TripsScreen(
                createdTripId = createdTripId,
                createdGroupName = createdGroupName,
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
        composable(
            route = Screen.PostDetail.route,
            arguments = listOf(navArgument("postId") { type = NavType.LongType })
        ) {
            PostDetailScreen(
                onBack = { navController.popBackStack() },
                onAuthorClick = ::navigateToUserProfile
            )
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
                onPostNotificationClick = { postId ->
                    navController.navigate(Screen.PostDetail.createRoute(postId)) {
                        launchSingleTop = true
                    }
                },
                onFollowNotificationClick = { userId ->
                    navigateToUserProfile(userId)
                },
                onNavigateToUserProfile = ::navigateToUserProfile,
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
//        composable(Screen.Notifications.route) {
//            NotificationsScreen(
//                onBack = { navController.popBackStack() }
//            )
//        }
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
                onNavigateToUserProfile = ::navigateToUserProfile,
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
                    val poppedToTrips = navController.popBackStack(Screen.Trips.route, false)
                    if (!poppedToTrips) {
                        navController.navigate(Screen.Trips.route) { launchSingleTop = true }
                    }
                    // set created trip info so TripsScreen can highlight/scroll to it
                    try {
                        val tripsEntry = navController.getBackStackEntry(Screen.Trips.route)
                        tripsEntry.savedStateHandle.set("created_trip_id", tripId)
                        tripsEntry.savedStateHandle.set("created_group_name", groupName)
                    } catch (e: Exception) {
                        // ignore if entry not available
                    }
                    navController.navigate(Screen.GroupDetail.createRoute(tripId, groupName)) {
                        launchSingleTop = true
                    }
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
                onNavigateToCost = { costTripId -> navController.navigate(Screen.CostEstimate.createRoute(costTripId)) { launchSingleTop = true } },
                onNavigateToProfile = { userId -> navController.navigate(Screen.OtherProfile.createRoute(userId)) { launchSingleTop = true } }
            )
        }

        composable(
            route = Screen.Itinerary.route,
            arguments = listOf(
                navArgument("tripId") { type = NavType.LongType },
                navArgument("groupName") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val tripId = backStackEntry.arguments?.getLong("tripId") ?: -1L
            val groupName = backStackEntry.arguments?.getString("groupName") ?: "Itinerary"
            ItineraryScreen(
                tripId = tripId,
                groupName = groupName,
                onBack = { navController.popBackStack() },
                onOpenDayDetail = { dayIndex ->
                    navController.navigate(Screen.ItineraryDayDetail.createRoute(tripId, groupName, dayIndex)) {
                        launchSingleTop = true
                    }
                }
            )
        }

        composable(
            route = Screen.ItineraryDayDetail.route,
            arguments = listOf(
                navArgument("tripId") { type = NavType.LongType },
                navArgument("groupName") { type = NavType.StringType },
                navArgument("dayIndex") { type = NavType.IntType }
            )
        ) { backStackEntry ->
            val tripId = backStackEntry.arguments?.getLong("tripId") ?: -1L
            val groupName = backStackEntry.arguments?.getString("groupName") ?: "Itinerary"
            val dayIndex = backStackEntry.arguments?.getInt("dayIndex") ?: 1
            ItineraryDayDetailScreen(
                tripId = tripId,
                groupName = groupName,
                dayIndex = dayIndex,
                onBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.CostEstimate.route,
            arguments = listOf(navArgument("tripId") { type = NavType.LongType })
        ) { backStackEntry ->
            val tripId = backStackEntry.arguments?.getLong("tripId") ?: -1L
            CostEstimateScreen(
                tripId = tripId,
                onBack = {
                    val popped = navController.popBackStack()
                    if (!popped) {
                        navController.navigate(Screen.Trips.route) {
                            popUpTo(navController.graph.findStartDestination().id) { inclusive = false }
                            launchSingleTop = true
                        }
                    }
                }
            )
        }

        composable(
            route = Screen.PlaceDetail.route,
            arguments = listOf(navArgument("placeId") { type = NavType.LongType })
        ) { backStackEntry ->
            val placeId = backStackEntry.arguments?.getLong("placeId") ?: return@composable
            val place = (backStackEntry.savedStateHandle.get<TravelPlaceListItemResponse>(PLACE_DETAIL_PLACE_KEY)
                ?: navController.previousBackStackEntry?.savedStateHandle?.get<TravelPlaceListItemResponse>(PLACE_DETAIL_PLACE_KEY)
                    ?.also { backStackEntry.savedStateHandle[PLACE_DETAIL_PLACE_KEY] = it })
                ?.takeIf { it.id == placeId }
            PlaceDetailScreen(
                placeId = placeId,
                initialPlace = place,
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
                onPlaceClick = { placeId ->
                    navController.navigate(Screen.PlaceDetail.createRoute(placeId)) {
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
    }
}
