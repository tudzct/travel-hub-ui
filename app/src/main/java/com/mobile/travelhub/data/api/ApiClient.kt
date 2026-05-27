package com.mobile.travelhub.data.api

import android.content.Context
import okhttp3.OkHttpClient
import org.json.JSONObject
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object ApiClient {
    /**
     * DÀNH CHO ANDROID EMULATOR:
     * Sử dụng IP 10.0.2.2 để kết nối đến localhost của máy tính.
     * Nếu dùng máy thật, hãy đổi thành IP mạng LAN (VD: 192.168.x.x)
     */
    private const val BASE_URL = "http://10.0.2.2:8080/"
    private const val PREFS_NAME = "travel_hub_auth"
    private const val KEY_SESSION = "auth_session"

    private var appContext: Context? = null

    fun initialize(context: Context) {
        appContext = context.applicationContext
    }

    private val httpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .addInterceptor { chain ->
                val token = readAccessToken()
                val request = if (token.isNullOrBlank()) {
                    chain.request()
                } else {
                    chain.request()
                        .newBuilder()
                        .header("Authorization", "Bearer $token")
                        .build()
                }
                chain.proceed(request)
            }
            .build()
    }

    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(httpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val apiService: TravelHubApiService by lazy {
        retrofit.create(TravelHubApiService::class.java)
    }

    private fun readAccessToken(): String? {
        val context = appContext ?: return null
        val rawSession = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getString(KEY_SESSION, null)
            ?: return null
        return runCatching { JSONObject(rawSession).optString("accessToken").takeIf { it.isNotBlank() } }
            .getOrNull()
    }
}
