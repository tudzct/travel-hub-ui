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
import com.mobile.travelhub.ui.screens.HomeScreen
import com.mobile.travelhub.ui.screens.ItineraryBotScreen
import com.mobile.travelhub.ui.screens.LoginScreen
import com.mobile.travelhub.ui.screens.ProfileScreen
import com.mobile.travelhub.ui.screens.RegisterScreen
import com.mobile.travelhub.ui.screens.TripsScreen
import com.mobile.travelhub.viewmodels.AuthUiState

sealed class Screen(
    val route: String,
    val index: Int = -1,
    val showBottomBar: Boolean = false
) {
    data object Home : Screen("home", 0, true)
    data object Trips : Screen("trips", 1, true)
    data object Profile : Screen("profile", 2, true)
    data object Chat : Screen("chat", 3, true)

    data object Login : Screen("login")
    data object Register : Screen("register")

    companion object {
        fun fromRoute(route: String?): Screen? {
            return when (route) {
                Home.route -> Home
                Trips.route -> Trips
                Profile.route -> Profile
                Chat.route -> Chat
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
