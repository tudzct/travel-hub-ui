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
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.mobile.travelhub.ui.screens.EditPlaceScreen
import com.mobile.travelhub.ui.screens.ItineraryBotScreen
import com.mobile.travelhub.ui.screens.OnboardingFinishScreen
import com.mobile.travelhub.ui.screens.OnboardingIntroScreen
import com.mobile.travelhub.ui.screens.OnboardingVibeScreen
import com.mobile.travelhub.ui.screens.LoginScreen
import com.mobile.travelhub.ui.screens.PlaceDetailScreen
import com.mobile.travelhub.ui.screens.PlaceListScreen

import com.mobile.travelhub.ui.screens.ProfileScreen
import com.mobile.travelhub.ui.screens.RegisterScreen
import com.mobile.travelhub.ui.screens.TripsScreen
import com.mobile.travelhub.viewmodels.AuthUiState

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

    companion object {
        fun fromRoute(route: String?): Screen? {
            return when (route) {
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
            TripsScreen()
        }
        composable(Screen.Profile.route) {
            ProfileScreen()
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
