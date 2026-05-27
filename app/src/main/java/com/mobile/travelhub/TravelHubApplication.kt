package com.mobile.travelhub

import android.app.Application
import com.mobile.travelhub.data.api.ApiClient
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class TravelHubApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        ApiClient.initialize(this)
    }
}
