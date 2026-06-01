package com.mobile.travelhub.data

import android.content.Context
import com.mobile.travelhub.data.api.AuthApiService
import com.mobile.travelhub.data.model.AuthResponse
import com.mobile.travelhub.data.model.AuthSession
import com.mobile.travelhub.data.model.LoginRequest
import com.mobile.travelhub.data.model.RefreshTokenRequest
import com.mobile.travelhub.data.model.RegisterRequest
import com.mobile.travelhub.data.model.authResponseFromJson
import com.mobile.travelhub.data.model.toJson
import com.mobile.travelhub.data.model.toSession
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepository @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val authApiService: AuthApiService
) {
    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun register(request: RegisterRequest): Result<AuthResponse> {
        return executeAuthCall { authApiService.register(request) }
    }

    fun login(request: LoginRequest): Result<AuthResponse> {
        return executeAuthCall { authApiService.login(request) }
    }

    fun refreshSession(): Result<AuthSession> {
        val currentSession = getSavedSession()
            ?: return Result.failure(IllegalStateException("No active session. Please login again."))

        val refreshToken = currentSession.refreshToken.trim()
        if (refreshToken.isEmpty()) {
            return Result.failure(IllegalStateException("Refresh token is missing. Please login again."))
        }

        return executeAuthCall {
            authApiService.refresh(RefreshTokenRequest(refreshToken = refreshToken))
        }
            .mapCatching { response ->
                val normalizedResponse = response.copy(
                    refreshToken = response.refreshToken.takeIf { it.isNotBlank() } ?: currentSession.refreshToken,
                    userId = if (response.userId > 0) response.userId else currentSession.userId,
                    isOnboarded = response.isOnboarded || currentSession.isOnboarded
                )
                saveSession(normalizedResponse)
                normalizedResponse.toSession()
            }
    }

    fun saveSession(response: AuthResponse) {
        prefs.edit().putString(KEY_SESSION, response.toJson()).apply()
    }

    fun getSavedSession(): AuthSession? {
        val raw = prefs.getString(KEY_SESSION, null) ?: return null
        val session = runCatching { authResponseFromJson(raw).toSession() }.getOrNull()
            ?: run {
                clearSession()
                return null
            }

        return session
    }

    fun clearSession() {
        prefs.edit().remove(KEY_SESSION).apply()
    }

    fun getAccessToken(): String? = getSavedSession()?.accessToken?.takeIf { it.isNotBlank() }

    fun updateOnboardingStatus(isOnboarded: Boolean) {
        val currentSession = getSavedSession() ?: return
        saveSession(
            AuthResponse(
                accessToken = currentSession.accessToken,
                refreshToken = currentSession.refreshToken,
                userId = currentSession.userId,
                isOnboarded = isOnboarded
            )
        )
    }

    private fun executeAuthCall(callFactory: () -> retrofit2.Call<AuthResponse>): Result<AuthResponse> {
        return runCatching {
            val response = callFactory().execute()
            if (!response.isSuccessful) {
                val errorBody = response.errorBody()?.string()
                throw IOException("Request failed (${response.code()}): $errorBody")
            }

            response.body()
                ?: throw IOException("Empty response body")
        }
    }

    companion object {
        private const val PREFS_NAME = "travel_hub_auth"
        private const val KEY_SESSION = "auth_session"
    }
}
