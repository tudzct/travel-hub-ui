package com.mobile.travelhub.usecase

import android.net.Uri
import android.util.Log
import com.mobile.travelhub.data.PostRepository
import javax.inject.Inject

class CreatePostUseCase @Inject constructor(
    private val postRepository: PostRepository
) {
    suspend operator fun invoke(description: String, imageUris: List<Uri>): Result<Unit> {
        val result = postRepository.createPost(description = description, imageUris = imageUris)
        result.onFailure {
            Log.e("CreatePostUseCase", "Post creation failed fully", it)
        }
        return result
    }
}
