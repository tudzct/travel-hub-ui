package com.mobile.travelhub.data

import android.content.Context
import com.mobile.travelhub.models.AuthResponse
import com.mobile.travelhub.models.AuthSession
import com.mobile.travelhub.models.LoginRequest
import com.mobile.travelhub.models.RegisterRequest
import com.mobile.travelhub.models.authResponseFromJson
import com.mobile.travelhub.models.toJson
import com.mobile.travelhub.models.toSession
import dagger.hilt.android.qualifiers.ApplicationContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepository @Inject constructor(
    @param:ApplicationContext private val context: Context
) {
    private val httpClient = OkHttpClient()

    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun register(request: RegisterRequest): Result<AuthResponse> {
        val payload = JSONObject()
            .put("email", request.email)
            .put("username", request.username)
            .put("password", request.password)
            .toString()

        return postJson(path = REGISTER_PATH, payload = payload)
    }

    fun login(request: LoginRequest): Result<AuthResponse> {
        val payload = JSONObject()
            .put("email", request.email)
            .put("password", request.password)
            .toString()

        return postJson(path = LOGIN_PATH, payload = payload)
    }

    fun saveSession(response: AuthResponse) {
        prefs.edit().putString(KEY_SESSION, response.toJson()).apply()
    }

    fun getSavedSession(): AuthSession? {
        val raw = prefs.getString(KEY_SESSION, null) ?: return null
        return runCatching { authResponseFromJson(raw).toSession() }.getOrNull()
    }

    fun clearSession() {
        prefs.edit().remove(KEY_SESSION).apply()
    }

    private fun postJson(path: String, payload: String): Result<AuthResponse> {
        return runCatching {
            val request = Request.Builder()
                .url("$BASE_URL$path")
                .post(payload.toRequestBody(JSON_MEDIA_TYPE))
                .build()

            httpClient.newCall(request).execute().use { response ->
                val rawBody = response.body.string()

                if (!response.isSuccessful) {
                    throw IOException("Request failed (${response.code}): $rawBody")
                }

                authResponseFromJson(rawBody)
            }
        }
    }

    companion object {
        private const val BASE_URL = "http://10.0.2.2:8080"
        private const val REGISTER_PATH = "/api/auth/register"
        private const val LOGIN_PATH = "/api/auth/login"

        private const val PREFS_NAME = "travel_hub_auth"
        private const val KEY_SESSION = "auth_session"

        private val JSON_MEDIA_TYPE = "application/json; charset=utf-8".toMediaType()
    }
}
