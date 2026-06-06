package com.mobile.travelhub.data

import android.webkit.MimeTypeMap
import com.mobile.travelhub.BuildConfig
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.storage.storage
import io.ktor.http.ContentType
import java.io.IOException
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Singleton
class AvatarRepository @Inject constructor(
    private val supabaseClient: SupabaseClient
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
        val bucketName = BuildConfig.SUPABASE_STORAGE_BUCKET.takeIf { it.isNotBlank() }
            ?: throw IOException("Chưa cấu hình kho lưu trữ ảnh")
        val objectPath = buildObjectPath(
            userId = userId,
            mimeType = safeMimeType,
            fileName = fileName
        )
        val bucket = supabaseClient.storage.from(bucketName)

        runCatching {
            bucket.upload(
                path = objectPath,
                data = imageBytes
            ) {
                upsert = false
                contentType = ContentType.parse(safeMimeType)
            }
        }.getOrElse { throwable ->
            throw IOException("Không thể tải ảnh đại diện lên. Vui lòng thử lại sau.", throwable)
        }

        bucket.publicUrl(objectPath)
    }

    private fun buildObjectPath(
        userId: Long,
        mimeType: String,
        fileName: String
    ): String {
        val extensionFromMime = MimeTypeMap.getSingleton().getExtensionFromMimeType(mimeType)
        val extensionFromName = fileName
            .substringAfterLast('.', "")
            .takeIf { it.isNotBlank() }
        val extension = (extensionFromMime ?: extensionFromName ?: "jpg")
            .lowercase()
            .filter(Char::isLetterOrDigit)
            .ifBlank { "jpg" }

        return "public/avatars/$userId/${System.currentTimeMillis()}-${UUID.randomUUID()}.$extension"
    }
}
