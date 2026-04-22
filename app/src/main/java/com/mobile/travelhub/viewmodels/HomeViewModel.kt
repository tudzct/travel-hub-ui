package com.mobile.travelhub.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mobile.travelhub.data.model.FeedPostResponse
import com.mobile.travelhub.usecase.GetAllPostsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class HomePostUiModel(
    val id: Long,
    val username: String,
    val subtitle: String,
    val description: String,
    val imageUrls: List<String>,
    val likeCountLabel: String,
    val commentCountLabel: String,
    val timeAgoLabel: String
)

data class HomeUiState(
    val isLoading: Boolean = false,
    val posts: List<HomePostUiModel> = emptyList(),
    val errorMessage: String? = null
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getAllPostsUseCase: GetAllPostsUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState(isLoading = true))
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        refreshPosts()
    }

    fun refreshPosts() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            getAllPostsUseCase(page = 0, pageSize = 20)
                .onSuccess { posts ->
                    val safePosts = posts.mapNotNull { post ->
                        runCatching { toUiModel(post) }.getOrNull()
                    }

                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            posts = safePosts,
                            errorMessage = null
                        )
                    }
                }
                .onFailure { throwable ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            posts = emptyList(),
                            errorMessage = throwable.message ?: "Failed to load posts"
                        )
                    }
                }
        }
    }

    private fun toUiModel(post: FeedPostResponse): HomePostUiModel {
        val safeId = runCatching { post.id }.getOrDefault(0L)
        val safeUsername = runCatching { post.owner.username }
            .getOrNull()
            ?.takeIf { it.isNotBlank() }
            ?: "unknown"
        val safeDescription = runCatching { post.description }
            .getOrNull()
            ?.takeIf { it.isNotBlank() }
            ?: ""
        val safeLocation = runCatching { post.location }
            .getOrNull()
            ?.takeIf { it.isNotBlank() }
            ?: "STUDIO NULL"
        val safeImageUrls = runCatching { post.imageUrls }
            .getOrNull()
            ?.filter { it.isNotBlank() }
            ?: emptyList()
        val safeCreatedAt = runCatching { post.createdAt }.getOrNull()

        return HomePostUiModel(
            id = safeId,
            username = safeUsername,
            subtitle = safeLocation,
            description = safeDescription,
            imageUrls = safeImageUrls,
            likeCountLabel = formatLikesFromId(safeId),
            commentCountLabel = "View all comments",
            timeAgoLabel = formatTimeAgo(safeCreatedAt)
        )
    }

    private fun formatLikesFromId(id: Long): String {
        val likes = ((id % 900) + 100).toInt()
        return "$likes likes"
    }

    private fun formatTimeAgo(rawTimestamp: String?): String {
        if (rawTimestamp.isNullOrBlank()) return "JUST NOW"

        val createdAtMillis = parseTimestampToMillis(rawTimestamp) ?: return "JUST NOW"
        val durationMillis = (System.currentTimeMillis() - createdAtMillis).coerceAtLeast(0L)

        val minutes = durationMillis / 60_000
        return when {
            minutes < 1 -> "JUST NOW"
            minutes < 60 -> "$minutes MINUTES AGO"
            minutes < 60 * 24 -> "${minutes / 60} HOURS AGO"
            else -> "${minutes / (60 * 24)} DAYS AGO"
        }
    }

    private fun parseTimestampToMillis(value: String): Long? {
        val formats = listOf(
            "yyyy-MM-dd'T'HH:mm:ss.SSSX",
            "yyyy-MM-dd'T'HH:mm:ssX",
            "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
            "yyyy-MM-dd'T'HH:mm:ss'Z'"
        )

        for (pattern in formats) {
            val parser = SimpleDateFormat(pattern, Locale.US).apply {
                timeZone = TimeZone.getTimeZone("UTC")
                isLenient = false
            }
            val parsed = runCatching { parser.parse(value) as Date? }.getOrNull()
            if (parsed != null) {
                return parsed.time
            }
        }

        return null
    }
}
