package com.mobile.travelhub.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import android.content.Context
import android.net.Uri
import com.mobile.travelhub.data.api.TravelHubApiService
import com.mobile.travelhub.data.model.FeedPostResponse
import com.mobile.travelhub.data.model.PostCreateRequest
import com.mobile.travelhub.data.model.UploadRequest
import dagger.hilt.android.qualifiers.ApplicationContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton
import retrofit2.HttpException

@Singleton
class PostRepository @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val api: TravelHubApiService
) {
    private val uploadClient = OkHttpClient()

    suspend fun createPost(description: String, imageUris: List<Uri>): Result<Unit> {
        return withContext(Dispatchers.IO) {
            runCatching {
                require(description.isNotBlank()) { "Description is required" }
                require(imageUris.isNotEmpty()) { "Please select at least one image" }

                val uploadResponse = try {
                    api.requestUploadUrls(
                        request = UploadRequest(folderName = "posts", files = imageUris.size)
                    )
                } catch (exception: HttpException) {
                    if (exception.code() == 401) {
                        throw IOException("Unauthorized at /api/upload. Access token is missing, expired, or malformed.")
                    }
                    val errorBody = exception.response()?.errorBody()?.string()
                    throw IOException("Failed to get upload URLs. Server returned ${exception.code()}: $errorBody")
                }

            if (uploadResponse.items.size < imageUris.size) {
                throw IOException("Upload URLs are missing from backend response")
            }

            val objectNames = imageUris.mapIndexed { index, uri ->
                val uploadItem = uploadResponse.items[index]

                android.util.Log.d("PostRepository", "Uploading to: ${uploadItem.url}")
                uploadToPresignedUrl(uploadItem.url, uri)
                android.util.Log.d("PostRepository", "Upload success for: ${uploadItem.objectName}")
                uploadItem.objectName
            }

                try {
                    android.util.Log.d("PostRepository", "Calling createPost with: $objectNames")
                    api.createPost(
                        request = PostCreateRequest(
                            description = description.trim(),
                            imageUrls = objectNames,
                            location = "Unknown" // Location added as default
                        )
                    )
                    android.util.Log.d("PostRepository", "createPost returned success")
                    Unit
                } catch (exception: HttpException) {
                    if (exception.code() == 401) {
                        throw IOException("Unauthorized at /api/posts. Access token is missing, expired, or malformed.")
                    }
                    val errorBody = exception.response()?.errorBody()?.string()
                    throw IOException("Failed to create post. Server returned ${exception.code()}: $errorBody")
                } catch (e: Exception) {
                    throw IOException("Failed to create post: ${e.message}", e)
                }
            }
        }
    }

    suspend fun getAllPosts(page: Int = 0, pageSize: Int = 10): Result<List<FeedPostResponse>> {
        return withContext(Dispatchers.IO) {
            runCatching {
                api.getAllPosts(
                    page = page,
                    pageSize = pageSize
                ).data
            }
        }
    }

    private fun uploadToPresignedUrl(url: String, uri: Uri) {
        val contentResolver = context.contentResolver
        val bytes = contentResolver.openInputStream(uri)?.use { it.readBytes() }
            ?: throw IOException("Unable to read selected image")
        val mimeType = contentResolver.getType(uri) ?: "image/jpeg"

        val request = Request.Builder()
            .url(url)
            .put(bytes.toRequestBody(mimeType.toMediaTypeOrNull()))
            .build()

        uploadClient.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                val errorBody = response.body?.string()
                throw IOException("Upload failed (${response.code}). details: $errorBody")
            }
        }
    }
}
