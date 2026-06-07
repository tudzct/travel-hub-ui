package com.mobile.travelhub.data.model

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

data class FirebaseSessionRequest(
    val username: String? = null,
    val name: String? = null
)

data class AuthResponse(
    val accessToken: String,
    val refreshToken: String = "",
    val userId: Int,
    val isOnboarded: Boolean = false
)

data class AuthSession(
    val accessToken: String,
    val userId: Int,
    val isOnboarded: Boolean = false
)

fun AuthResponse.toSession(): AuthSession = AuthSession(
    accessToken = accessToken,
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
