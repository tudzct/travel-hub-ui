package com.mobile.travelhub.navigation

import androidx.compose.animation.AnimatedContentTransitionScope.SlideDirection
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.mobile.travelhub.ui.screens.HomeScreen
import com.mobile.travelhub.ui.screens.ItineraryBotScreen
import com.mobile.travelhub.ui.screens.OnboardingFinishScreen
import com.mobile.travelhub.ui.screens.OnboardingIntroScreen
import com.mobile.travelhub.ui.screens.OnboardingVibeScreen
import com.mobile.travelhub.ui.screens.ProfileScreen
import com.mobile.travelhub.ui.screens.TripsScreen

sealed class Screen(val route: String, val index: Int) {
    data object OnboardingIntro : Screen("onboarding-intro", -3)
    data object OnboardingVibe : Screen("onboarding-vibe", -2)
    data object OnboardingFinish : Screen("onboarding-finish", -1)
    data object Home : Screen("home", 0)
    data object Trips : Screen("trips", 1)
    data object Profile : Screen("profile", 2)

    data object Chat : Screen("chat", 3)

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
        startDestination = Screen.OnboardingIntro.route,
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
            HomeScreen()
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
    }
}
