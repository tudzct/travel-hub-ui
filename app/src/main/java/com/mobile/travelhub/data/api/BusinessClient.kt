package com.mobile.travelhub.data.api

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object BusinessClient {
    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(ApiConfig.BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val apiService: TravelHubApiService by lazy {
        retrofit.create(TravelHubApiService::class.java)
    }
}
