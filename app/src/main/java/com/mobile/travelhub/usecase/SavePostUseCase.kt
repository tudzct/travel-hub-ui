package com.mobile.travelhub.usecase

import com.mobile.travelhub.data.PostRepository
import com.mobile.travelhub.data.model.SavePostResponse
import javax.inject.Inject

class SavePostUseCase @Inject constructor(
    private val postRepository: PostRepository
) {
    suspend operator fun invoke(postId: Long, currentlySaved: Boolean): Result<SavePostResponse> {
        return postRepository.toggleSavedPost(postId = postId, currentlySaved = currentlySaved)
    }
}
