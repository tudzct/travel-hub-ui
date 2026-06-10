package com.mobile.travelhub.ui.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable

// Brand / Common colors
val Purple80 = Color(0xFFD0BCFF)
val PurpleGrey80 = Color(0xFFCCC2DC)
val Pink80 = Color(0xFFEFB8C8)

val Purple40 = Color(0xFF6650a4)
val PurpleGrey40 = Color(0xFF625b71)
val Pink40 = Color(0xFF7D5260)
val PrimaryBlue = Color(0xFF1677F2)
val PrimaryContainer = Color(0xFF8AC8EE)
val OnPrimary = Color(0xFFFFFFFF)
val Secondary = PrimaryBlue
val SecondaryContainer = PrimaryContainer
val OnSecondary = OnPrimary

val SunsetOrange = Color(0xFF9B4500)
val Tertiary = PrimaryBlue
val TertiaryContainer = PrimaryContainer

// --- Light Theme Colors ---
val LightSurfaceBg = Color(0xFFF8F9FA)
val LightSurface = Color(0xFFFFFFFF)
val LightSurfaceContainer = Color(0xFFEDEEEF)
val LightSurfaceContainerLow = Color(0xFFF3F4F5)
val LightSurfaceContainerLowest = Color(0xFFFFFFFF)
val LightOnSurface = Color(0xFF191C1D)
val LightOnSurfaceVariant = Color(0xFF414755)
val LightOutlineVariant = Color(0xFFC1C6D7)

// --- Dark Theme Colors ---
val DarkSurfaceBg = Color(0xFF121212)
val DarkSurface = Color(0xFF1E1E1E)
val DarkSurfaceContainer = Color(0xFF2D2D2D)
val DarkSurfaceContainerLow = Color(0xFF252525)
val DarkSurfaceContainerLowest = Color(0xFF1E1E1E)
val DarkOnSurface = Color(0xFFEEEEEE)
val DarkOnSurfaceVariant = Color(0xFFCCCCCC)
val DarkOutlineVariant = Color(0xFF444444)

// Helper to check dark theme
val isDarkTheme: Boolean
    @Composable
    @ReadOnlyComposable
    get() = MaterialTheme.colorScheme.background.red < 0.5f

// Dynamic properties
val SurfaceBg: Color
    @Composable
    @ReadOnlyComposable
    get() = if (isDarkTheme) DarkSurfaceBg else LightSurfaceBg

val Surface: Color
    @Composable
    @ReadOnlyComposable
    get() = if (isDarkTheme) DarkSurface else LightSurface

val SurfaceContainer: Color
    @Composable
    @ReadOnlyComposable
    get() = if (isDarkTheme) DarkSurfaceContainer else LightSurfaceContainer

val SurfaceContainerLow: Color
    @Composable
    @ReadOnlyComposable
    get() = if (isDarkTheme) DarkSurfaceContainerLow else LightSurfaceContainerLow

val SurfaceContainerLowest: Color
    @Composable
    @ReadOnlyComposable
    get() = if (isDarkTheme) DarkSurfaceContainerLowest else LightSurfaceContainerLowest

val OnSurface: Color
    @Composable
    @ReadOnlyComposable
    get() = if (isDarkTheme) DarkOnSurface else LightOnSurface

val OnSurfaceVariant: Color
    @Composable
    @ReadOnlyComposable
    get() = if (isDarkTheme) DarkOnSurfaceVariant else LightOnSurfaceVariant

val OutlineVariant: Color
    @Composable
    @ReadOnlyComposable
    get() = if (isDarkTheme) DarkOutlineVariant else LightOutlineVariant

val Primary = PrimaryBlue
