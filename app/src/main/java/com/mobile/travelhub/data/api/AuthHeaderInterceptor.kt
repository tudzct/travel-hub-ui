package com.mobile.travelhub.data.api

import com.mobile.travelhub.data.AuthRepository
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthHeaderInterceptor @Inject constructor(
    private val authRepository: AuthRepository
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()

        if (originalRequest.header(HEADER_AUTHORIZATION) != null) {
            return chain.proceed(originalRequest)
        }

        val accessToken = authRepository.getSavedSession()
            ?.accessToken
            ?.trim()
            .orEmpty()

        if (accessToken.isEmpty()) {
            return chain.proceed(originalRequest)
        }

        val authorization = if (accessToken.startsWith(BEARER_PREFIX, ignoreCase = true)) {
            accessToken
        } else {
            "$BEARER_PREFIX$accessToken"
        }

        val authenticatedRequest = originalRequest.newBuilder()
            .header(HEADER_AUTHORIZATION, authorization)
            .build()

        return chain.proceed(authenticatedRequest)
    }

    private companion object {
        private const val HEADER_AUTHORIZATION = "Authorization"
        private const val BEARER_PREFIX = "Bearer "
    }
}