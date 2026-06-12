package com.mobile.travelhub.viewmodels

import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

sealed interface PostInteractionEvent {
    data class LikeChanged(
        val postId: Long,
        val liked: Boolean,
        val likeCount: Int
    ) : PostInteractionEvent

    data class SaveChanged(
        val postId: Long,
        val saved: Boolean,
        val saveCount: Int
    ) : PostInteractionEvent

    data class CommentCountChanged(
        val postId: Long,
        val commentCount: Int
    ) : PostInteractionEvent

    data class UserProfileChanged(
        val userId: Long,
        val username: String,
        val name: String,
        val avatarUrl: String?
    ) : PostInteractionEvent
}

@Singleton
class PostInteractionEventBus @Inject constructor() {
    private val _events = MutableSharedFlow<PostInteractionEvent>(extraBufferCapacity = 64)
    val events: SharedFlow<PostInteractionEvent> = _events.asSharedFlow()

    fun publish(event: PostInteractionEvent) {
        _events.tryEmit(event)
    }
}
