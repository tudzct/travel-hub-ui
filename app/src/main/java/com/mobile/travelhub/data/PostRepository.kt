package com.mobile.travelhub.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import android.content.Context
import android.net.Uri
import android.webkit.MimeTypeMap
import com.mobile.travelhub.BuildConfig
import com.mobile.travelhub.data.api.PostApiService
import com.mobile.travelhub.data.api.UserApiService
import com.mobile.travelhub.data.model.CreateCommentRequest
import com.mobile.travelhub.data.model.FeedPostResponse
import com.mobile.travelhub.data.model.LikePostResponse
import com.mobile.travelhub.data.model.PostCommentResponse
import com.mobile.travelhub.data.model.PostCommentsPageResponse
import com.mobile.travelhub.data.model.PostCreateRequest
import dagger.hilt.android.qualifiers.ApplicationContext
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.storage.storage
import io.ktor.http.ContentType
import java.io.IOException
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton
import retrofit2.HttpException

@Singleton
class PostRepository @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val postApiService: PostApiService,
    private val userApiService: UserApiService,
    private val supabaseClient: SupabaseClient
) {
    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    suspend fun createPost(description: String, imageUris: List<Uri>, travelPlaceId: Long): Result<Unit> {
        return withContext(Dispatchers.IO) {
            runCatching {
                require(description.isNotBlank()) { "Description is required" }
                require(imageUris.isNotEmpty()) { "Please select at least one image" }
                require(travelPlaceId > 0) { "Please select a place" }

                val objectNames = imageUris.map { uri ->
                    val objectName = uploadToSupabase(uri)
                    android.util.Log.d("PostRepository", "Upload success for: $objectName")
                    objectName
                }

                try {
                    android.util.Log.d("PostRepository", "Calling createPost with: $objectNames")
                    postApiService.createPost(
                        request = PostCreateRequest(
                            description = description.trim(),
                            imageUrls = objectNames,
                            travelPlaceId = travelPlaceId
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
                val likedPostIds = getLikedPostIds()
                postApiService.getAllPosts(
                    page = page,
                    pageSize = pageSize
                ).data.map { post ->
                    val localLiked = likedPostIds.contains(post.id.toString())
                    val mergedLiked = (post.likedByCurrentUser == true) || localLiked
                    post.copy(likedByCurrentUser = mergedLiked)
                }
            }
        }
    }

    suspend fun getPostsByUser(userId: Long, page: Int = 0, pageSize: Int = 20): Result<List<FeedPostResponse>> {
        return withContext(Dispatchers.IO) {
            runCatching {
                val likedPostIds = getLikedPostIds()
                userApiService.getUserPosts(
                    id = userId,
                    page = page,
                    pageSize = pageSize
                ).data.map { post ->
                    val localLiked = likedPostIds.contains(post.id.toString())
                    val mergedLiked = (post.likedByCurrentUser == true) || localLiked
                    post.copy(likedByCurrentUser = mergedLiked)
                }
            }
        }
    }

    suspend fun likePost(postId: Long): Result<LikePostResponse> {
        return withContext(Dispatchers.IO) {
            runCatching {
                val response = postApiService.likePost(postId = postId)
                updateLikedPost(postId = postId, liked = true)
                response
            }.recoverCatching { throwable ->
                if (throwable is HttpException) {
                    val errorBody = throwable.response()?.errorBody()?.string()
                    throw IOException("Failed to like post. Server returned ${throwable.code()}: $errorBody", throwable)
                }
                throw throwable
            }
        }
    }

    suspend fun unlikePost(postId: Long): Result<LikePostResponse> {
        return withContext(Dispatchers.IO) {
            runCatching {
                val response = postApiService.unlikePost(postId = postId)
                updateLikedPost(postId = postId, liked = false)
                response
            }.recoverCatching { throwable ->
                if (throwable is HttpException) {
                    val errorBody = throwable.response()?.errorBody()?.string()
                    throw IOException("Failed to unlike post. Server returned ${throwable.code()}: $errorBody", throwable)
                }
                throw throwable
            }
        }
    }

    suspend fun addComment(postId: Long, content: String): Result<PostCommentResponse> {
        return withContext(Dispatchers.IO) {
            runCatching {
                require(content.isNotBlank()) { "Comment cannot be empty" }
                postApiService.addComment(
                    postId = postId,
                    request = CreateCommentRequest(content = content.trim())
                )
            }.recoverCatching { throwable ->
                if (throwable is HttpException) {
                    val errorBody = throwable.response()?.errorBody()?.string()
                    throw IOException("Failed to add comment. Server returned ${throwable.code()}: $errorBody", throwable)
                }
                throw throwable
            }
        }
    }

    suspend fun getPostComments(postId: Long, page: Int = 0, pageSize: Int = 20): Result<PostCommentsPageResponse> {
        return withContext(Dispatchers.IO) {
            runCatching {
                postApiService.getPostComments(
                    postId = postId,
                    page = page,
                    pageSize = pageSize
                )
            }.recoverCatching { throwable ->
                if (throwable is HttpException) {
                    val errorBody = throwable.response()?.errorBody()?.string()
                    throw IOException("Failed to load comments. Server returned ${throwable.code()}: $errorBody", throwable)
                }
                throw throwable
            }
        }
    }

    private suspend fun uploadToSupabase(uri: Uri): String {
        val contentResolver = context.contentResolver
        val bytes = contentResolver.openInputStream(uri)?.use { it.readBytes() }
            ?: throw IOException("Unable to read selected image")
        val mimeType = contentResolver.getType(uri) ?: "image/jpeg"
        val bucketName = BuildConfig.SUPABASE_STORAGE_BUCKET.takeIf { it.isNotBlank() }
            ?: throw IOException("SUPABASE_STORAGE_BUCKET is not configured")
        val objectPath = buildStorageObjectPath(uri = uri, mimeType = mimeType)

        runCatching {
            supabaseClient.storage.from(bucketName).upload(
                path = objectPath,
                data = bytes
            ) {
                upsert = false
                contentType = ContentType.parse(mimeType)
            }
        }.getOrElse { throwable ->
            throw IOException("Upload failed: ${throwable.message}", throwable)
        }

        return objectPath
    }

    private fun buildStorageObjectPath(uri: Uri, mimeType: String): String {
        val extensionFromMime = MimeTypeMap.getSingleton().getExtensionFromMimeType(mimeType)
        val extensionFromUri = uri.lastPathSegment
            ?.substringAfterLast('.', "")
            ?.takeIf { it.isNotBlank() }
        val extension = (extensionFromMime ?: extensionFromUri ?: "jpg").lowercase()

        return "public/${System.currentTimeMillis()}-${UUID.randomUUID()}.$extension"
    }

    private fun getLikedPostIds(): Set<String> {
        return prefs.getStringSet(KEY_LIKED_POST_IDS, emptySet()) ?: emptySet()
    }

    private fun updateLikedPost(postId: Long, liked: Boolean) {
        val likedPosts = getLikedPostIds().toMutableSet()
        val key = postId.toString()

        if (liked) {
            likedPosts.add(key)
        } else {
            likedPosts.remove(key)
        }

        prefs.edit().putStringSet(KEY_LIKED_POST_IDS, likedPosts).apply()
    }

    companion object {
        private const val PREFS_NAME = "travel_hub_post"
        private const val KEY_LIKED_POST_IDS = "liked_post_ids"
    }
}
