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
import com.mobile.travelhub.data.model.GetPostsResponse
import com.mobile.travelhub.data.model.LikePostResponse
import com.mobile.travelhub.data.model.PostCommentResponse
import com.mobile.travelhub.data.model.PostCommentsPageResponse
import com.mobile.travelhub.data.model.PostCreateRequest
import com.mobile.travelhub.data.model.SavePostResponse
import dagger.hilt.android.qualifiers.ApplicationContext
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.storage.storage
import io.ktor.http.ContentType
import java.io.IOException
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

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
                require(description.isNotBlank()) { "Vui lòng nhập mô tả bài viết" }
                require(imageUris.isNotEmpty()) { "Vui lòng chọn ít nhất một ảnh" }
                require(travelPlaceId > 0) { "Vui lòng chọn địa điểm" }

                val objectNames = imageUris.map { uri ->
                    val objectName = uploadToSupabase(uri)
                    android.util.Log.d("PostRepository", "Upload success for: $objectName")
                    objectName
                }

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
            }
        }
    }

    suspend fun getAllPosts(page: Int = 0, pageSize: Int = 10): Result<List<FeedPostResponse>> {
        return getPostsPage(page = page, pageSize = pageSize).map { it.data }
    }

    suspend fun getPostsPage(page: Int = 0, pageSize: Int = 10): Result<GetPostsResponse> {
        return withContext(Dispatchers.IO) {
            runCatching {
                postApiService.getAllPosts(
                    page = page,
                    pageSize = pageSize
                ).let { response ->
                    response.copy(data = response.data.map(::mergeLocalPostState))
                }
            }
        }
    }

    suspend fun searchPosts(description: String, page: Int = 0, pageSize: Int = 10): Result<List<FeedPostResponse>> {
        return withContext(Dispatchers.IO) {
            runCatching {
                postApiService.searchPosts(
                    description = description,
                    page = page,
                    pageSize = pageSize
                ).data.map(::mergeLocalPostState)
            }
        }
    }

    suspend fun getPost(postId: Long): Result<FeedPostResponse> {
        return withContext(Dispatchers.IO) {
            runCatching {
                require(postId > 0) { "Invalid post id" }
                mergeLocalPostState(postApiService.getPost(postId = postId))
            }.recoverCatching {
                getAllPosts(page = 0, pageSize = 100)
                    .getOrThrow()
                    .firstOrNull { post -> post.id == postId }
                    ?: throw IOException("Không tìm thấy bài viết")
            }
        }
    }

    suspend fun getPostsByUser(userId: Long, page: Int = 0, pageSize: Int = 20): Result<List<FeedPostResponse>> {
        return withContext(Dispatchers.IO) {
            runCatching {
                userApiService.getUserPosts(
                    id = userId,
                    page = page,
                    pageSize = pageSize
                ).data.map(::mergeLocalPostState)
            }
        }
    }

    suspend fun getLikedPostsByUser(userId: Long, page: Int = 0, pageSize: Int = 20): Result<List<FeedPostResponse>> {
        return withContext(Dispatchers.IO) {
            runCatching {
                userApiService.getUserLikedPosts(
                    id = userId,
                    page = page,
                    pageSize = pageSize
                ).data.map { post ->
                    mergeLocalPostState(post).copy(likedByCurrentUser = true)
                }
            }
        }
    }

    suspend fun getSavedPostsByUser(userId: Long, page: Int = 0, pageSize: Int = 20): Result<List<FeedPostResponse>> {
        return withContext(Dispatchers.IO) {
            runCatching {
                userApiService.getUserSavedPosts(
                    id = userId,
                    page = page,
                    pageSize = pageSize
                ).data.map { post ->
                    mergeLocalPostState(post).copy(savedByCurrentUser = true)
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
            }
        }
    }

    suspend fun unlikePost(postId: Long): Result<LikePostResponse> {
        return withContext(Dispatchers.IO) {
            runCatching {
                val response = postApiService.unlikePost(postId = postId)
                updateLikedPost(postId = postId, liked = false)
                response
            }
        }
    }

    suspend fun savePost(postId: Long): Result<SavePostResponse> {
        return withContext(Dispatchers.IO) {
            runCatching {
                val response = postApiService.savePost(postId = postId)
                updateSavedPost(postId = postId, saved = response.saved)
                response
            }
        }
    }

    suspend fun unsavePost(postId: Long): Result<SavePostResponse> {
        return withContext(Dispatchers.IO) {
            runCatching {
                val response = postApiService.unsavePost(postId = postId)
                updateSavedPost(postId = postId, saved = response.saved)
                response
            }
        }
    }

    suspend fun toggleSavedPost(postId: Long, currentlySaved: Boolean): Result<SavePostResponse> {
        return if (currentlySaved) {
            unsavePost(postId)
        } else {
            savePost(postId)
        }
    }

    suspend fun addComment(postId: Long, content: String): Result<PostCommentResponse> {
        return withContext(Dispatchers.IO) {
            runCatching {
                require(content.isNotBlank()) { "Vui lòng nhập bình luận" }
                postApiService.addComment(
                    postId = postId,
                    request = CreateCommentRequest(content = content.trim())
                )
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
            }
        }
    }

    private suspend fun uploadToSupabase(uri: Uri): String {
        val contentResolver = context.contentResolver
        val bytes = contentResolver.openInputStream(uri)?.use { it.readBytes() }
            ?: throw IOException("Không thể đọc ảnh đã chọn")
        val mimeType = contentResolver.getType(uri) ?: "image/jpeg"
        val bucketName = BuildConfig.SUPABASE_STORAGE_BUCKET.takeIf { it.isNotBlank() }
            ?: throw IOException("Chưa cấu hình kho lưu trữ ảnh")
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
            throw IOException("Không thể tải ảnh lên. Vui lòng thử lại sau.", throwable)
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

    private fun getSavedPostIds(): Set<String> {
        return prefs.getStringSet(KEY_SAVED_POST_IDS, emptySet()) ?: emptySet()
    }

    private fun mergeLocalPostState(post: FeedPostResponse): FeedPostResponse {
        val localLiked = getLikedPostIds().contains(post.id.toString())
        val localSaved = getSavedPostIds().contains(post.id.toString())
        val mergedLiked = post.likedByCurrentUser ?: localLiked
        val mergedSaved = post.savedByCurrentUser ?: localSaved
        return post.copy(
            likedByCurrentUser = mergedLiked,
            savedByCurrentUser = mergedSaved
        )
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

    private fun updateSavedPost(postId: Long, saved: Boolean) {
        val savedPosts = getSavedPostIds().toMutableSet()
        val key = postId.toString()

        if (saved) {
            savedPosts.add(key)
        } else {
            savedPosts.remove(key)
        }

        prefs.edit().putStringSet(KEY_SAVED_POST_IDS, savedPosts).apply()
    }

    companion object {
        private const val PREFS_NAME = "travel_hub_post"
        private const val KEY_LIKED_POST_IDS = "liked_post_ids"
        private const val KEY_SAVED_POST_IDS = "saved_post_ids"
    }
}
