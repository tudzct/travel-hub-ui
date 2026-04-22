package com.mobile.travelhub.usecase

import com.mobile.travelhub.data.PostRepository
import com.mobile.travelhub.data.model.LikePostResponse
import javax.inject.Inject

class LikePostUseCase @Inject constructor(
    private val postRepository: PostRepository
) {
    suspend operator fun invoke(postId: Long): Result<LikePostResponse> {
        return postRepository.likePost(postId = postId)
    }
}
