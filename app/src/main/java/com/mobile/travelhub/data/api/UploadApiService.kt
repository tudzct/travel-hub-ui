package com.mobile.travelhub.data.api

import com.mobile.travelhub.data.model.UploadRequest
import com.mobile.travelhub.data.model.UploadResponse
import retrofit2.http.Body
import retrofit2.http.POST

interface UploadApiService {
    @POST("api/upload")
    suspend fun requestUploadUrls(
        @Body request: UploadRequest
    ): UploadResponse
}
