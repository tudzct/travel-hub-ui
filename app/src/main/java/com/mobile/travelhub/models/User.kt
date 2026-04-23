package com.mobile.travelhub.models

import android.util.Base64
import org.json.JSONObject

data class RegisterRequest(
    val email: String,
    val username: String,
    val password: String
)

data class LoginRequest(
    val email: String,
    val password: String
)

data class AuthResponse(
    val accessToken: String,
    val refreshToken: String,
    val userId: Int
)

data class RefreshTokenRequest(
    val refreshToken: String
)

data class AuthSession(
    val accessToken: String,
    val refreshToken: String,
    val userId: Int
)

data class JwtClaims(
    val authorities: List<String> = emptyList(),
    val expiresAtEpochSeconds: Long? = null
)

fun AuthResponse.toSession(): AuthSession = AuthSession(
    accessToken = accessToken,
    refreshToken = refreshToken,
    userId = userId
)

fun AuthResponse.toJson(): String {
    return JSONObject()
        .put("accessToken", accessToken)
        .put("refreshToken", refreshToken)
        .put("userId", userId)
        .toString()
}

fun authResponseFromJson(raw: String): AuthResponse {
    val json = JSONObject(raw)
    return AuthResponse(
        accessToken = json.optString("accessToken"),
        refreshToken = json.optString("refreshToken"),
        userId = json.optInt("userId", -1)
    )
}

fun decodeJwtClaims(token: String): JwtClaims {
    return runCatching {
        val segments = token.split(".")
        if (segments.size < 2) {
            return JwtClaims()
        }
        val payloadSegment = segments[1]
        val padded = payloadSegment.padEnd(((payloadSegment.length + 3) / 4) * 4, '=')
        val decoded = Base64.decode(padded, Base64.URL_SAFE or Base64.NO_WRAP or Base64.NO_PADDING)
        val json = JSONObject(String(decoded, Charsets.UTF_8))
        val authoritiesJson = json.optJSONArray("authorities")
        val authorities = buildList {
            if (authoritiesJson != null) {
                for (index in 0 until authoritiesJson.length()) {
                    add(authoritiesJson.optString(index))
                }
            }
        }
        val expiresAtEpochSeconds = json.optLong("exp")
            .takeIf { it > 0L }
        JwtClaims(
            authorities = authorities.filter { it.isNotBlank() },
            expiresAtEpochSeconds = expiresAtEpochSeconds
        )
    }.getOrDefault(JwtClaims())
}

val AuthSession.jwtClaims: JwtClaims
    get() = decodeJwtClaims(accessToken)

val AuthSession.isAdmin: Boolean
    get() = jwtClaims.authorities.contains("ROLE_ADMIN")

val AuthSession.isExpired: Boolean
    get() {
        val expiresAtEpochSeconds = jwtClaims.expiresAtEpochSeconds ?: return false
        val nowEpochSeconds = System.currentTimeMillis() / 1000
        return nowEpochSeconds >= expiresAtEpochSeconds
    }
