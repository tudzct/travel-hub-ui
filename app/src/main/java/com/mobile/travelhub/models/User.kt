package com.mobile.travelhub.models

<<<<<<< HEAD
=======
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

data class AuthSession(
    val accessToken: String,
    val refreshToken: String,
    val userId: Int
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
>>>>>>> main
