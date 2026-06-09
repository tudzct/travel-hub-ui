package com.mobile.travelhub.data.api

import com.mobile.travelhub.data.model.AvatarUploadResponse
import com.mobile.travelhub.data.model.UploadRequest
import com.mobile.travelhub.data.model.UploadResponse
import okhttp3.MultipartBody
import retrofit2.http.Body
import retrofit2.http.Multipart
import retrofit2.http.Part
import retrofit2.http.POST

interface UploadApiService {
    @POST("api/upload")
    suspend fun requestUploadUrls(
        @Body request: UploadRequest
    ): UploadResponse

    @Multipart
    @POST("api/upload/avatar")
    suspend fun uploadAvatar(
        @Part file: MultipartBody.Part
    ): AvatarUploadResponse
}
