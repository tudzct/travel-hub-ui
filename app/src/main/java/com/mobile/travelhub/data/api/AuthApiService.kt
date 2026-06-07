package com.mobile.travelhub.data.api

import com.mobile.travelhub.data.model.AuthResponse
import com.mobile.travelhub.data.model.FirebaseSessionRequest
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

interface AuthApiService {
    @POST("api/auth/session")
    fun syncFirebaseSession(
        @Header("Authorization") authorization: String,
        @Body request: FirebaseSessionRequest
    ): Call<AuthResponse>
}
