package com.mobile.travelhub.data

import android.content.Context
import android.net.Uri
import com.mobile.travelhub.data.api.FileUploadApiService
import com.mobile.travelhub.data.api.UploadApiService
import com.mobile.travelhub.data.model.UploadRequest
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody

@Singleton
class ExpenseProofRepository @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val uploadApiService: UploadApiService,
    private val fileUploadApiService: FileUploadApiService
) {
    suspend fun uploadProof(imageUri: Uri): Result<String> {
        return withContext(Dispatchers.IO) {
            runCatching {
                val resolver = context.contentResolver
                val bytes = resolver.openInputStream(imageUri)?.use { it.readBytes() }
                    ?: throw IOException("Không thể đọc ảnh minh chứng")
                val mimeType = resolver.getType(imageUri) ?: "image/jpeg"
                val uploadItem = uploadApiService
                    .requestUploadUrls(UploadRequest(folderName = "trip-expense-proofs", files = 1))
                    .items
                    .firstOrNull()
                    ?: throw IOException("Không lấy được đường dẫn tải ảnh")

                val response = fileUploadApiService.uploadFile(
                    url = uploadItem.url,
                    body = bytes.toRequestBody(mimeType.toMediaType())
                )
                if (!response.isSuccessful) {
                    throw IOException("Không thể tải ảnh minh chứng lên storage")
                }

                uploadItem.objectName
            }
        }
    }
}
