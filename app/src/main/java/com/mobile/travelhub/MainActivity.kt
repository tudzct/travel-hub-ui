package com.mobile.travelhub

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.lifecycleScope
import com.mobile.travelhub.data.DeviceTokenRepository
import com.mobile.travelhub.ui.screens.TravelHubScreen
import com.mobile.travelhub.ui.theme.TravelHubTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val themePrefs by lazy { getSharedPreferences("travel_hub_prefs", Context.MODE_PRIVATE) }

    @Inject
    lateinit var deviceTokenRepository: DeviceTokenRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        askNotificationPermission()
        registerDeviceToken()
        setContent {
            val hasDarkThemePreference = remember { themePrefs.contains("dark_theme_enabled") }
            val systemDarkTheme = isSystemInDarkTheme()
            var darkThemeEnabled by remember {
                mutableStateOf(
                    if (hasDarkThemePreference) {
                        themePrefs.getBoolean("dark_theme_enabled", false)
                    } else {
                        systemDarkTheme
                    }
                )
            }

            TravelHubTheme(darkTheme = darkThemeEnabled) {
                TravelHubScreen(
                    isDarkThemeEnabled = darkThemeEnabled,
                    onDarkThemeChange = { enabled ->
                        darkThemeEnabled = enabled
                        themePrefs.edit().putBoolean("dark_theme_enabled", enabled).apply()
                    }
                )
            }
        }
    }
    // [START ask_post_notifications]
    // Declare the launcher at the top of your Activity/Fragment:
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { isGranted: Boolean ->
        if (isGranted) {
            // FCM SDK (and your app) can post notifications.
        } else {
            // TODO: Inform user that that your app will not show notifications.
        }
    }

    private fun askNotificationPermission() {
        // This is only necessary for API level >= 33 (TIRAMISU)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) ==
                PackageManager.PERMISSION_GRANTED
            ) {
                // FCM SDK (and your app) can post notifications.
            } else if (shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS)) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            } else {
                // Directly ask for the permission
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    private fun registerDeviceToken() {
        lifecycleScope.launch(Dispatchers.IO) {
            deviceTokenRepository.registerCurrentDeviceToken()
        }
    }
}
