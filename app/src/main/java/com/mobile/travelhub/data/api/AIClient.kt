package com.mobile.travelhub.data.api

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object AIClient {
    /**
     * DÀNH CHO ANDROID EMULATOR:
     * Sử dụng IP 10.0.2.2 để kết nối đến localhost của máy tính.
     * Nếu dùng máy thật, hãy đổi thành IP mạng LAN (VD: 192.168.x.x)
     */
    private const val BASE_URL = "http://10.0.2.2:8888/"

    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val apiService: AIApiService by lazy {
        retrofit.create(AIApiService::class.java)
    }
}
