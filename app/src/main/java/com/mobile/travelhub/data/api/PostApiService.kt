package com.mobile.travelhub.data.api

import com.mobile.travelhub.data.model.CreateCommentRequest
import com.mobile.travelhub.data.model.FeedPostResponse
import com.mobile.travelhub.data.model.GetPostsResponse
import com.mobile.travelhub.data.model.LikePostResponse
import com.mobile.travelhub.data.model.PostCommentResponse
import com.mobile.travelhub.data.model.PostCommentsPageResponse
import com.mobile.travelhub.data.model.PostCreateRequest
import com.mobile.travelhub.data.model.PostResponse
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface PostApiService {
    @GET("api/posts")
    suspend fun getAllPosts(
        @Query("page") page: Int = 0,
        @Query("pageSize") pageSize: Int = 10
    ): GetPostsResponse

    @GET("api/posts/search")
    suspend fun searchPosts(
        @Query("description") description: String,
        @Query("page") page: Int = 0,
        @Query("pageSize") pageSize: Int = 10
    ): GetPostsResponse

    @GET("api/posts/{postId}")
    suspend fun getPost(
        @Path("postId") postId: Long
    ): FeedPostResponse

    @POST("api/posts")
    suspend fun createPost(
        @Body request: PostCreateRequest
    ): PostResponse

    @POST("api/posts/{postId}/like")
    suspend fun likePost(
        @Path("postId") postId: Long
    ): LikePostResponse

    @DELETE("api/posts/{postId}/unlike")
    suspend fun unlikePost(
        @Path("postId") postId: Long
    ): LikePostResponse

    @POST("api/posts/{postId}/comments")
    suspend fun addComment(
        @Path("postId") postId: Long,
        @Body request: CreateCommentRequest
    ): PostCommentResponse

    @GET("api/posts/{postId}/comments")
    suspend fun getPostComments(
        @Path("postId") postId: Long,
        @Query("page") page: Int = 0,
        @Query("pageSize") pageSize: Int = 20
    ): PostCommentsPageResponse
}
