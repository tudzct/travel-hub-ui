package com.mobile.travelhub.usecase

import com.mobile.travelhub.data.PostRepository
import com.mobile.travelhub.data.model.PostCommentResponse
import javax.inject.Inject

class AddCommentUseCase @Inject constructor(
    private val postRepository: PostRepository
) {
    suspend operator fun invoke(postId: Long, content: String): Result<PostCommentResponse> {
        return postRepository.addComment(postId = postId, content = content)
    }
}
