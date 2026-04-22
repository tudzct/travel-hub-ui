package com.mobile.travelhub.usecase

import com.mobile.travelhub.data.PostRepository
import com.mobile.travelhub.data.model.PostCommentsPageResponse
import javax.inject.Inject

class GetPostCommentsUseCase @Inject constructor(
    private val postRepository: PostRepository
) {
    suspend operator fun invoke(postId: Long, page: Int = 0, pageSize: Int = 20): Result<PostCommentsPageResponse> {
        return postRepository.getPostComments(postId = postId, page = page, pageSize = pageSize)
    }
}
