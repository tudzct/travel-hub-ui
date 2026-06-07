package com.mobile.travelhub.usecase

import com.mobile.travelhub.data.PostRepository
import com.mobile.travelhub.data.model.GetPostsResponse
import javax.inject.Inject

class GetAllPostsUseCase @Inject constructor(
    private val postRepository: PostRepository
) {
    suspend operator fun invoke(page: Int = 0, pageSize: Int = 10): Result<GetPostsResponse> {
        return postRepository.getPostsPage(page = page, pageSize = pageSize)
    }
}
