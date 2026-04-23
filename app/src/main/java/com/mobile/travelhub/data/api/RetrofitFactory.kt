package com.mobile.travelhub.data.api

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitFactory {
    fun create(baseUrl: String, client: OkHttpClient? = null): Retrofit {
        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .apply {
                if (client != null) {
                    client(client)
                }
            }
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
}
