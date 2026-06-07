package com.mobile.travelhub.data.model

import android.util.Base64
import org.json.JSONObject

data class RegisterRequest(
    val email: String,
    val username: String,
    val name: String,
    val password: String
)

data class LoginRequest(
    val email: String,
    val password: String
)

data class AuthResponse(
    val accessToken: String,
    val refreshToken: String,
    val userId: Int,
    val isOnboarded: Boolean = false
)

data class RefreshTokenRequest(
    val refreshToken: String
)

data class AuthSession(
    val accessToken: String,
    val refreshToken: String,
    val userId: Int,
    val isOnboarded: Boolean = false
)

fun AuthResponse.toSession(): AuthSession = AuthSession(
    accessToken = accessToken,
    refreshToken = refreshToken,
    userId = userId,
    isOnboarded = isOnboarded
)

fun AuthResponse.toJson(): String {
    return JSONObject()
        .put("accessToken", accessToken)
        .put("refreshToken", refreshToken)
        .put("userId", userId)
        .put("isOnboarded", isOnboarded)
        .toString()
}

fun authResponseFromJson(raw: String): AuthResponse {
    val json = JSONObject(raw)
    return AuthResponse(
        accessToken = json.optString("accessToken"),
        refreshToken = json.optString("refreshToken"),
        userId = json.optInt("userId", -1),
        isOnboarded = json.optBoolean("isOnboarded", false)
    )
}

private fun decodeJwtExpiration(token: String): Long? {
    return runCatching {
        val segments = token.split(".")
        if (segments.size < 2) {
            return null
        }
        val payloadSegment = segments[1]
        val padded = payloadSegment.padEnd(((payloadSegment.length + 3) / 4) * 4, '=')
        val decoded = Base64.decode(padded, Base64.URL_SAFE or Base64.NO_WRAP or Base64.NO_PADDING)
        val json = JSONObject(String(decoded, Charsets.UTF_8))
        json.optLong("exp")
            .takeIf { it > 0L }
    }.getOrNull()
}

val AuthSession.isExpired: Boolean
    get() {
        val expiresAtEpochSeconds = decodeJwtExpiration(accessToken) ?: return false
        val nowEpochSeconds = System.currentTimeMillis() / 1000
        return nowEpochSeconds >= expiresAtEpochSeconds
    }
