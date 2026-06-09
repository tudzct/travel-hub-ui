package com.mobile.travelhub.data

import android.util.Log
import com.mobile.travelhub.data.api.UploadApiService
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody

@Singleton
class AvatarRepository @Inject constructor(
    private val uploadApiService: UploadApiService
) {
    suspend fun uploadAvatar(
        userId: Long,
        imageBytes: ByteArray,
        mimeType: String,
        fileName: String
    ): String = withContext(Dispatchers.IO) {
        require(userId > 0L) { "Bạn cần đăng nhập để cập nhật ảnh đại diện" }
        require(imageBytes.isNotEmpty()) { "Ảnh đại diện không hợp lệ" }

        val safeMimeType = mimeType
            .trim()
            .takeIf { it.startsWith("image/") }
            ?: "image/jpeg"
        val requestBody = imageBytes.toRequestBody(safeMimeType.toMediaType())
        val filePart = MultipartBody.Part.createFormData(
            name = "file",
            filename = fileName.ifBlank { DEFAULT_FILE_NAME },
            body = requestBody
        )

        runCatching {
            uploadApiService.uploadAvatar(filePart).avatarUrl
        }.getOrElse { throwable ->
            Log.e(TAG, "Unable to upload avatar file", throwable)
            throw IOException("Không thể tải ảnh đại diện lên. Vui lòng thử lại sau.", throwable)
        }
    }

    private companion object {
        private const val TAG = "AvatarRepository"
        private const val DEFAULT_FILE_NAME = "avatar.jpg"
    }
}
