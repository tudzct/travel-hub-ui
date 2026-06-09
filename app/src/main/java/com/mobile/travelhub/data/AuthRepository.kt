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

    suspend fun register(request: RegisterRequest): Result<AuthResponse> {
        return runCatching {
            executeAuthCall {
                authApiService.register(request)
            }.getOrThrow()
        }
    }

    suspend fun login(request: LoginRequest): Result<AuthResponse> {
        return runCatching {
            executeAuthCall {
                authApiService.login(request)
            }.getOrThrow()
        }
    }

    fun refreshSession(): Result<AuthSession> {
        return runCatching {
            val currentSession = getSavedSession()
                ?: throw IllegalStateException("Phiên đăng nhập đã hết hạn. Vui lòng đăng nhập lại.")
            val refreshToken = currentSession.refreshToken.takeIf { it.isNotBlank() }
                ?: throw IllegalStateException("Phiên đăng nhập đã hết hạn. Vui lòng đăng nhập lại.")
            val response = executeAuthCall {
                authApiService.refresh(RefreshTokenRequest(refreshToken))
            }.getOrThrow()
            saveSession(response)
            response.toSession()
        }
    }

    fun saveSession(response: AuthResponse) {
        prefs.edit().putString(KEY_SESSION, response.toJson()).apply()
    }

    fun saveSession(session: AuthSession) {
        saveSession(
            AuthResponse(
                accessToken = session.accessToken,
                refreshToken = session.refreshToken,
                userId = session.userId,
                isOnboarded = session.isOnboarded
            )
        )
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

    fun getAccessToken(): String? = getSavedSession()?.accessToken

    private fun executeAuthCall(callFactory: () -> retrofit2.Call<AuthResponse>): Result<AuthResponse> {
        return runCatching {
            val response = callFactory().execute()
            if (!response.isSuccessful) {
                val errorBody = response.errorBody()?.string()
                throw IOException(apiErrorMessageFromText(errorBody) ?: "Yêu cầu không thành công (${response.code()}).")
            }

            response.body()
                ?: throw IOException("Máy chủ không trả về dữ liệu.")
        }
    }

    companion object {
        private const val PREFS_NAME = "travel_hub_auth"
        private const val KEY_SESSION = "auth_session"
    }
}
