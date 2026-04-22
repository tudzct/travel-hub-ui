package com.mobile.travelhub.data.api

import android.util.Log
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Request
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object PlaceApiFactory {
    private const val TAG = "PLACE_API"

    fun create(
        accessTokenProvider: () -> String?
    ): PlaceApiService {
        val authInterceptor = Interceptor { chain ->
            val token = accessTokenProvider()
            val originalRequest = chain.request()
            Log.d(TAG, "request ${originalRequest.method} ${originalRequest.url}")
            if (token.isNullOrBlank() || shouldSkipAuthHeader(originalRequest)) {
                return@Interceptor proceedWithLogging(chain, originalRequest, "auth=skipped")
            }

            val authenticatedRequest = originalRequest.newBuilder()
                .addHeader("Authorization", "Bearer $token")
                .build()

            val response = proceedWithLogging(chain, authenticatedRequest, "auth=true")
            if (response.code != 401 || !isPublicPlaceRequest(originalRequest)) {
                return@Interceptor response
            }

            response.close()
            Log.d(TAG, "retry without auth ${originalRequest.method} ${originalRequest.url}")
            proceedWithLogging(chain, originalRequest, "auth=false")
        }

        val client = OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .build()

        return Retrofit.Builder()
            .baseUrl(ApiConfig.BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(PlaceApiService::class.java)
    }

    private fun isPublicPlaceRequest(request: Request): Boolean {
        if (request.method != "GET") {
            return false
        }

        val path = request.url.encodedPath
        return path == "/api/places" ||
            path.matches(Regex("^/api/places/\\d+$")) ||
            path.matches(Regex("^/api/places/\\d+/reviews$"))
    }

    private fun shouldSkipAuthHeader(request: Request): Boolean {
        if (request.method != "GET") {
            return false
        }

        val path = request.url.encodedPath
        return path == "/api/places" || path.matches(Regex("^/api/places/\\d+/reviews$"))
    }

    private fun proceedWithLogging(
        chain: Interceptor.Chain,
        request: Request,
        context: String
    ) = try {
        chain.proceed(request).also { response ->
            Log.d(TAG, "response ${response.code} ${request.method} ${request.url} $context")
        }
    } catch (throwable: Throwable) {
        Log.e(TAG, "failure ${request.method} ${request.url} $context: ${throwable.javaClass.simpleName}: ${throwable.message}", throwable)
        throw throwable
    }
}
