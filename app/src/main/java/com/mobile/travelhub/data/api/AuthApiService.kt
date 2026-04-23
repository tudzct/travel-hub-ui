package com.mobile.travelhub.data.api

import com.mobile.travelhub.models.AuthResponse
import com.mobile.travelhub.models.LoginRequest
import com.mobile.travelhub.models.RefreshTokenRequest
import com.mobile.travelhub.models.RegisterRequest
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthApiService {
    @POST("api/auth/register")
    fun register(
        @Body request: RegisterRequest
    ): Call<AuthResponse>

    @POST("api/auth/login")
    fun login(
        @Body request: LoginRequest
    ): Call<AuthResponse>

    @POST("api/auth/refresh")
    fun refresh(
        @Body request: RefreshTokenRequest
    ): Call<AuthResponse>
}
