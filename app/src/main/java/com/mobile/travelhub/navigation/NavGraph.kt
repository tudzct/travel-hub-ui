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
import com.mobile.travelhub.ui.screens.ProfileScreen
import com.mobile.travelhub.ui.screens.TripsScreen

sealed class Screen(val route: String, val index: Int) {
    data object Home : Screen("home", 0)
    data object Trips : Screen("trips", 1)
    data object Profile : Screen("profile", 2)

    companion object {
        fun fromRoute(route: String?): Screen? {
            return when (route) {
                Home.route -> Home
                Trips.route -> Trips
                Profile.route -> Profile
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
            TripsScreen()
        }
        composable(Screen.Profile.route) {
            ProfileScreen()
        }
    }
}
