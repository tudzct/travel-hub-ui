package com.mobile.travelhub.data.api

import okhttp3.Interceptor
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object BusinessClient {
    fun create(
        accessTokenProvider: () -> String?
    ): TravelHubApiService {
        val authInterceptor = Interceptor { chain ->
            val token = accessTokenProvider()
            val request = chain.request().newBuilder().apply {
                if (!token.isNullOrBlank()) {
                    addHeader("Authorization", "Bearer $token")
                }
            }.build()
            chain.proceed(request)
        }

        val client = OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .build()

        return Retrofit.Builder()
            .baseUrl(ApiConfig.BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(TravelHubApiService::class.java)
    }
    private const val BASE_URL = ApiConfig.BASE_URL

    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val apiService: TravelHubApiService by lazy {
        retrofit.create(TravelHubApiService::class.java)
    }
}
