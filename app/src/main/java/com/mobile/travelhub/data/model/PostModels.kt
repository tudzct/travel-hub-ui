package com.mobile.travelhub.data.model

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
    val owner: PostOwner
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
    val createdAt: String? = null,
    val updatedAt: String? = null,
    val location: String? = null
)

data class PostOwner(
    val id: Long,
    val username: String
)
