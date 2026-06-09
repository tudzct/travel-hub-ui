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
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.hilt.navigation.compose.hiltViewModel
import com.mobile.travelhub.data.model.TravelPlaceListItemResponse
import com.mobile.travelhub.data.model.TopTravelerPeriod
import com.mobile.travelhub.data.model.UserProfileResponse
import com.mobile.travelhub.R
import com.mobile.travelhub.ui.components.TravelHubDrawerContent
import com.mobile.travelhub.ui.screens.ProfileScreen
import com.mobile.travelhub.ui.screens.LoginScreen
import com.mobile.travelhub.ui.screens.PlaceDetailScreen
import com.mobile.travelhub.ui.screens.PlaceListScreen
import com.mobile.travelhub.ui.screens.ReviewListScreen

import com.mobile.travelhub.ui.screens.RegisterScreen
import com.mobile.travelhub.ui.screens.ViewHistoryScreen
import com.mobile.travelhub.viewmodels.AuthUiState
import androidx.navigation.navArgument
import com.mobile.travelhub.ui.screens.*
import com.mobile.travelhub.viewmodels.ProfileViewModel
import com.mobile.travelhub.viewmodels.UiState
import kotlinx.coroutines.launch

sealed class Screen(
    val route: String,
    val index: Int = -1,
    val showBottomBar: Boolean = false
) {
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
    data object Trips : Screen("trips", 3, true)
    data object UpcomingTrips : Screen("trips_upcoming", 15, true)
    data object CreatePost : Screen("create_post", 2, true)
    data object Profile : Screen("profile", 4, true)
    data object Chat : Screen("chat", 3) {
        const val TRIP_ID_ARG = "tripId"
        const val GROUP_NAME_ARG = "groupName"
        const val ROUTE_WITH_ARGS = "chat?tripId={tripId}&groupName={groupName}"

        fun createRoute(tripId: Long? = null, groupName: String = ""): String {
            val normalizedTripId = tripId?.takeIf { it > 0L } ?: -1L
            return "chat?tripId=$normalizedTripId&groupName=${Uri.encode(groupName)}"
        }
    }
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
                Home.route -> Home
                Explore.route -> Explore
                Search.route -> Search
                Trips.route -> Trips
                UpcomingTrips.route -> UpcomingTrips
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
                "profile_user" -> OtherProfile
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
    profile: UserProfileResponse?,
    onNavigateToProfile: () -> Unit,
    onNavigateToEditProfile: () -> Unit,
    onNavigateToHistory: () -> Unit,
    onLogout: () -> Unit,
    isDarkThemeEnabled: Boolean,
    onDarkThemeChange: (Boolean) -> Unit,
    changePasswordState: UiState<Boolean>,
    onChangePassword: (String, String, String) -> Unit,
    onClearChangePasswordState: () -> Unit,
    content: @Composable (openMenu: () -> Unit) -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val drawerState = androidx.compose.material3.rememberDrawerState(initialValue = DrawerValue.Closed)
    var hideDrawerContentForNavigation by remember { mutableStateOf(false) }
    var showChangePasswordDialog by remember { mutableStateOf(false) }

    LaunchedEffect(changePasswordState) {
        if (changePasswordState is UiState.Success) {
            showChangePasswordDialog = false
        }
    }

    val openDrawer = {
        coroutineScope.launch { drawerState.open() }
    }
    val drawerVisible =
        drawerState.currentValue != DrawerValue.Closed || drawerState.targetValue != DrawerValue.Closed

    @Composable
    fun HomeShellBody() {
        content {
            hideDrawerContentForNavigation = false
            openDrawer()
        }
        if (showChangePasswordDialog) {
            ChangePasswordDialog(
                state = changePasswordState,
                onDismiss = {
                    showChangePasswordDialog = false
                    onClearChangePasswordState()
                },
                onSubmit = onChangePassword
            )
        }
    }

    if (drawerVisible) {
        ModalNavigationDrawer(
            drawerState = drawerState,
            scrimColor = Color.Black.copy(alpha = 0.38f),
            drawerContent = {
                if (!hideDrawerContentForNavigation) {
                    TravelHubDrawerContent(
                        profile = profile,
                        onProfileClick = {
                            coroutineScope.launch {
                                drawerState.close()
                                onNavigateToProfile()
                            }
                        },
                        onEditProfileClick = {
                            coroutineScope.launch {
                                drawerState.close()
                                onNavigateToEditProfile()
                            }
                        },
                        onChangePasswordClick = {
                            coroutineScope.launch {
                                drawerState.close()
                                onClearChangePasswordState()
                                showChangePasswordDialog = true
                            }
                        },
                        onLogoutClick = {
                            coroutineScope.launch {
                                drawerState.close()
                                onLogout()
                            }
                        },
                        isDarkThemeEnabled = isDarkThemeEnabled,
                        onDarkThemeChange = onDarkThemeChange
                    )
                }
            }
        ) {
            HomeShellBody()
        }
    } else {
        HomeShellBody()
    }
}

@Composable
fun NavGraph(
    navController: NavHostController,
    innerPadding: PaddingValues,
    startDestination: String,
    authUiState: AuthUiState,
    homeReloadSignal: Int = 0,
    onExploreSearchActiveChange: (Boolean) -> Unit = {},
    onLogin: (String, String) -> Unit,
    onRegister: (String, String, String, String) -> Unit,
    onClearAuthError: () -> Unit,
    onLogout: () -> Unit,
    isDarkThemeEnabled: Boolean,
    onDarkThemeChange: (Boolean) -> Unit
) {
    val currentRoute = navController.currentBackStackEntry?.destination?.route
        ?.substringBefore("?")
        ?.substringBefore("/")

    fun navigateToPlaceDetail(place: TravelPlaceListItemResponse) {
        navController.navigate(Screen.PlaceDetail.createRoute(place.id))
    }

    fun navigateToUserProfile(userId: Long) {
        if (userId <= 0L) return
        val currentUserId = authUiState.session?.userId?.toLong()
        if (currentUserId == userId) {
            navController.navigate(Screen.Profile.route) {
                popUpTo(navController.graph.findStartDestination().id) {
                    saveState = true
                }
                launchSingleTop = true
                restoreState = true
            }
        } else {
            navController.navigate(Screen.OtherProfile.createRoute(userId)) {
                launchSingleTop = true
            }
        }
    }

    LaunchedEffect(authUiState.isAuthenticated, currentRoute) {
        val isAuthRoute = currentRoute == Screen.Login.route || currentRoute == Screen.Register.route
        if (authUiState.isAuthenticated && isAuthRoute) {
            navController.navigate(Screen.Home.route) {
                popUpTo(Screen.Login.route) { inclusive = true }
                launchSingleTop = true
            }
        }
    }

    fun bottomNavIndex(route: String?): Int? {
        val baseRoute = route
            ?.substringBefore("?")
            ?.substringBefore("/")

        return when (baseRoute) {
            Screen.Home.route -> Screen.Home.index
            Screen.Explore.route -> Screen.Explore.index
            Screen.CreatePost.route -> Screen.CreatePost.index
            Screen.Trips.route -> Screen.Trips.index
            Screen.Profile.route -> Screen.Profile.index
            else -> null
        }
    }

    NavHost(
        navController = navController,
        startDestination = startDestination,
        enterTransition = {
            val initialIndex = bottomNavIndex(initialState.destination.route)
            val targetIndex = bottomNavIndex(targetState.destination.route)
            val direction = if (
                initialIndex != null &&
                targetIndex != null &&
                targetIndex < initialIndex
            ) {
                SlideDirection.Right
            } else {
                SlideDirection.Left
            }
            slideIntoContainer(
                towards = direction,
                animationSpec = tween(300)
            )
        },
        exitTransition = {
            val initialIndex = bottomNavIndex(initialState.destination.route)
            val targetIndex = bottomNavIndex(targetState.destination.route)
            val direction = if (
                initialIndex != null &&
                targetIndex != null &&
                targetIndex < initialIndex
            ) {
                SlideDirection.Right
            } else {
                SlideDirection.Left
            }
            slideOutOfContainer(
                towards = direction,
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
                onNavigateToRegister = {
                    onClearAuthError()
                    navController.navigate(Screen.Register.route)
                },
                onDismissError = onClearAuthError
            )
        }
        composable(Screen.Register.route) {
            RegisterScreen(
                uiState = authUiState,
                onRegister = onRegister,
                onNavigateToLogin = {
                    onClearAuthError()
                    navController.popBackStack()
                },
                onDismissError = onClearAuthError
            )
        }
        composable(Screen.Home.route) {
            val profileViewModel: ProfileViewModel = hiltViewModel()
            val changePasswordState by profileViewModel.changePasswordState.collectAsState()
            val drawerProfileState by profileViewModel.profileState.collectAsState()
            val drawerProfile = (drawerProfileState as? UiState.Success)?.data

            LaunchedEffect(Unit) {
                profileViewModel.loadUserProfile()
            }

            HomeDrawerScaffold(
                profile = drawerProfile,
                onNavigateToProfile = {
                    navController.navigate(Screen.Profile.route) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                onNavigateToHistory = { navController.navigate(Screen.ViewHistory.route) { launchSingleTop = true } },
                onNavigateToEditProfile = {
                    navController.navigate(Screen.EditProfile.route) {
                        launchSingleTop = true
                    }
                },
                onLogout = {
                    onLogout()
                    navController.navigate(Screen.Login.route) {
                        popUpTo(navController.graph.findStartDestination().id) { inclusive = false }
                        launchSingleTop = true
                    }
                },
                changePasswordState = changePasswordState,
                onChangePassword = profileViewModel::changePassword,
                onClearChangePasswordState = profileViewModel::clearChangePasswordState,
                isDarkThemeEnabled = isDarkThemeEnabled,
                onDarkThemeChange = onDarkThemeChange
            ) { openMenu ->
                PlaceListScreen(
                    reloadSignal = homeReloadSignal,
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
                onSearchActiveChange = onExploreSearchActiveChange,
                onSearchUserClick = ::navigateToUserProfile,
                onPlaceClick = ::navigateToPlaceDetail,
                onAssistantClick = {
                    navController.navigate(Screen.Chat.createRoute()) {
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
                onUserClick = ::navigateToUserProfile,
                onPlaceClick = ::navigateToPlaceDetail
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
                onNavigateToUpcomingTrips = {
                    navController.navigate(Screen.UpcomingTrips.route) { launchSingleTop = true }
                },
                onNavigateToCreateGroup = {
                    navController.navigate(Screen.CreateGroup.route) { launchSingleTop = true }
                },
                onNavigateToEditProfile = {
                    navController.navigate(Screen.EditProfile.route) { launchSingleTop = true }
                }
            )
        }

        composable(Screen.UpcomingTrips.route) {
            UpcomingTripsScreen(
                onBack = { navController.popBackStack() },
                onNavigateToGroupDetail = { tripId, groupName ->
                    navController.navigate(Screen.GroupDetail.createRoute(tripId, groupName)) { launchSingleTop = true }
                }
            )
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
            val profileViewModel: ProfileViewModel = hiltViewModel()
            val previousRoute = navController.previousBackStackEntry?.destination?.route
            val isFromDeepScreen = previousRoute != null && 
                previousRoute != Screen.Home.route &&
                previousRoute != Screen.Explore.route &&
                previousRoute != Screen.Explore.ROUTE_WITH_ARGS &&
                previousRoute != Screen.Trips.route &&
                previousRoute != Screen.CreatePost.route &&
                previousRoute != Screen.Chat.route &&
                previousRoute != Screen.Profile.route

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
                onNavigateToCreatePost = {
                    navController.navigate(Screen.CreatePost.route) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
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
                },
                onBack = if (isFromDeepScreen) {
                    { navController.popBackStack() }
                } else {
                    null
                },
                viewModel = profileViewModel,
                isDarkThemeEnabled = isDarkThemeEnabled,
                onDarkThemeChange = onDarkThemeChange
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
                    val navigatedBack = navController.navigateUp()
                    if (!navigatedBack) {
                        navController.navigate(Screen.Explore.route) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                        }
                    }
                },
                isDarkThemeEnabled = isDarkThemeEnabled,
                onDarkThemeChange = onDarkThemeChange
            )
        }
        composable(Screen.EditProfile.route) {
            val profileViewModel: ProfileViewModel = hiltViewModel()
            EditProfileScreen(
                onBack = { navController.popBackStack() },
                onSaveSuccess = { navController.popBackStack() },
                viewModel = profileViewModel
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
                onNavigateToAssistant = { assistantTripId, assistantGroupName ->
                    navController.navigate(Screen.Chat.createRoute(assistantTripId, assistantGroupName)) {
                        launchSingleTop = true
                    }
                },
                onNavigateToProfile = ::navigateToUserProfile
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
            route = Screen.Chat.ROUTE_WITH_ARGS,
            arguments = listOf(
                navArgument(Screen.Chat.TRIP_ID_ARG) {
                    type = NavType.LongType
                    defaultValue = -1L
                },
                navArgument(Screen.Chat.GROUP_NAME_ARG) {
                    type = NavType.StringType
                    defaultValue = ""
                }
            )
        ) { backStackEntry ->
            val tripId = backStackEntry.arguments
                ?.getLong(Screen.Chat.TRIP_ID_ARG)
                ?.takeIf { it > 0L }
            val groupName = backStackEntry.arguments
                ?.getString(Screen.Chat.GROUP_NAME_ARG)
                .orEmpty()
            TravelAssistantScreen(
                tripId = tripId,
                groupName = groupName,
                onBack = { navController.navigateUp() },
                onPlaceClick = { placeId ->
                    navController.navigate(Screen.PlaceDetail.createRoute(placeId)) {
                        launchSingleTop = true
                    }
                }
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
                },
                onNavigateToProfile = ::navigateToUserProfile
            )
        }

        composable(
            route = Screen.PlaceDetail.route,
            arguments = listOf(navArgument("placeId") { type = NavType.LongType })
        ) { backStackEntry ->
            val placeId = backStackEntry.arguments?.getLong("placeId") ?: return@composable
            PlaceDetailScreen(
                placeId = placeId,
                initialPlace = null,
                onBack = { navController.navigateUp() },
                onPlaceClick = ::navigateToPlaceDetail,
                onUserClick = ::navigateToUserProfile,
                onShowAllReviews = { id -> navController.navigate(Screen.PlaceReviews.createRoute(id)) },
                onReviewAuthorClick = ::navigateToUserProfile,
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
                onBack = { navController.navigateUp() },
                onAuthorClick = ::navigateToUserProfile,
                onRequireLogin = {
                    onLogout()
                    navController.navigate(Screen.Login.route) {
                        popUpTo(navController.graph.findStartDestination().id) { inclusive = false }
                        launchSingleTop = true
                    }
                }
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
