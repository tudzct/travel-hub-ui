package com.mobile.travelhub.data.model

import com.google.gson.annotations.SerializedName

data class UploadRequest(
    val folderName: String,
    val files: Int
)

data class UploadResponse(
    val items: List<UploadItem>
)

data class UploadItem(
    val objectName: String,
    val url: String
)

data class PostCreateRequest(
    val description: String,
    val imageUrls: List<String>,
    val location: String? = null
)

data class PostResponse(
    val id: Long,
    val description: String,
    val imageUrls: List<String>,
    val owner: PostOwner,
    val likeCount: Int? = null,
    val commentCount: Int? = null
)

data class GetPostsResponse(
    val pageNumber: Int,
    val pageSize: Int,
    val totalPages: Int,
    val totalElements: Long,
    val data: List<FeedPostResponse>
)

data class FeedPostResponse(
    val id: Long,
    val description: String,
    val imageUrls: List<String>,
    val owner: PostOwner,
    @SerializedName(value = "createdAt", alternate = ["created_at", "createdDate", "postedAt"]) 
    val createdAt: String? = null,
    @SerializedName(value = "updatedAt", alternate = ["updated_at", "updatedDate"]) 
    val updatedAt: String? = null,
    val location: String? = null,
    @SerializedName(value = "likedByCurrentUser", alternate = ["liked", "isLiked"]) 
    val likedByCurrentUser: Boolean? = null,
    @SerializedName(value = "likeCount", alternate = ["likesCount", "likes", "like_count"]) 
    val likeCount: Int? = null,
    @SerializedName(value = "commentCount", alternate = ["commentsCount", "comments", "comment_count"]) 
    val commentCount: Int? = null
)

data class LikePostResponse(
    val postId: Long,
    val liked: Boolean,
    val likeCount: Int
)

data class CreateCommentRequest(
    val content: String
)

data class PostCommentResponse(
    val id: Long? = null,
    val content: String,
    val owner: CommentOwnerResponse? = null,
    val createdAt: String? = null,
    val updatedAt: String? = null
)

data class PostCommentsPageResponse(
    val pageNumber: Int = 0,
    val pageSize: Int = 0,
    val totalPages: Int = 0,
    val totalElements: Long = 0,
    val data: List<PostCommentResponse> = emptyList()
)

data class CommentOwnerResponse(
    val id: Long,
    val username: String,
    val avatarUrl: String? = null
)

data class PostOwner(
    val id: Long,
    val username: String
)
