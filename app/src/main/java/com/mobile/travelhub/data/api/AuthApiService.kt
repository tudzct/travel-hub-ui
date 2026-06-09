package com.mobile.travelhub.data.api

import com.mobile.travelhub.data.model.AuthResponse
import com.mobile.travelhub.data.model.LoginRequest
import com.mobile.travelhub.data.model.RefreshTokenRequest
import com.mobile.travelhub.data.model.RegisterRequest
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthApiService {
    @POST("api/auth/login")
    fun login(@Body request: LoginRequest): Call<AuthResponse>

    @POST("api/auth/register")
    fun register(@Body request: RegisterRequest): Call<AuthResponse>

    @POST("api/auth/refresh")
    fun refresh(@Body request: RefreshTokenRequest): Call<AuthResponse>
}
