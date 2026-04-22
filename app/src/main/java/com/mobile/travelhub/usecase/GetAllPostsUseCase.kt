package com.mobile.travelhub.usecase

import com.mobile.travelhub.data.PostRepository
import com.mobile.travelhub.data.model.FeedPostResponse
import javax.inject.Inject

class GetAllPostsUseCase @Inject constructor(
    private val postRepository: PostRepository
) {
    suspend operator fun invoke(page: Int = 0, pageSize: Int = 10): Result<List<FeedPostResponse>> {
        return postRepository.getAllPosts(page = page, pageSize = pageSize)
    }
}
