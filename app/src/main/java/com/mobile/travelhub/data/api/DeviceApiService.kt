package com.mobile.travelhub.data.api

import com.mobile.travelhub.data.model.DeviceTokenRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface DeviceApiService {
    @POST("api/devices/token")
    suspend fun registerDeviceToken(
        @Body request: DeviceTokenRequest
    ): Response<Unit>
}
