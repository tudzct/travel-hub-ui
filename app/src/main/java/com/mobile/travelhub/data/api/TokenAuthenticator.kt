package com.mobile.travelhub.data.api

import com.mobile.travelhub.data.AuthRepository
import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TokenAuthenticator @Inject constructor(
    private val authRepository: AuthRepository
) : Authenticator {

    private val refreshLock = Any()

    override fun authenticate(route: Route?, response: Response): Request? {
        if (isAuthEndpoint(response.request.url.encodedPath)) {
            return null
        }

        if (responseCount(response) >= MAX_AUTH_RETRIES) {
            return null
        }

        synchronized(refreshLock) {
            val latestAccessToken = authRepository.getAccessToken()?.trim().orEmpty()
            val latestAuthorization = latestAccessToken
                .takeIf { it.isNotEmpty() }
                ?.let { token ->
                    if (token.startsWith(BEARER_PREFIX, ignoreCase = true)) token else "$BEARER_PREFIX$token"
                }

            val failedAuthorization = response.request.header(HEADER_AUTHORIZATION)
            if (!latestAuthorization.isNullOrEmpty() && failedAuthorization != latestAuthorization) {
                return response.request.newBuilder()
                    .header(HEADER_AUTHORIZATION, latestAuthorization)
                    .build()
            }

            val refreshedSession = authRepository.refreshSession().getOrNull() ?: return null
            val refreshedAuthorization = refreshedSession.accessToken.trim()
                .takeIf { it.isNotEmpty() }
                ?.let { token ->
                    if (token.startsWith(BEARER_PREFIX, ignoreCase = true)) token else "$BEARER_PREFIX$token"
                }
                ?: return null

            return response.request.newBuilder()
                .header(HEADER_AUTHORIZATION, refreshedAuthorization)
                .build()
        }
    }

    private fun responseCount(response: Response): Int {
        var result = 1
        var priorResponse = response.priorResponse
        while (priorResponse != null) {
            result++
            priorResponse = priorResponse.priorResponse
        }
        return result
    }

    private fun isAuthEndpoint(path: String): Boolean = path in AUTH_PATHS

    private companion object {
        private const val HEADER_AUTHORIZATION = "Authorization"
        private const val BEARER_PREFIX = "Bearer "
        private const val MAX_AUTH_RETRIES = 2

        private val AUTH_PATHS = setOf(
            "/api/auth/session",
            "/api/auth/login",
            "/api/auth/register",
            "/api/auth/refresh"
        )
    }
}
