package com.mobile.travelhub.data

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONArray

@Singleton
class SearchHistoryRepository @Inject constructor(
    @param:ApplicationContext context: Context,
    private val authRepository: AuthRepository
) {
    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val _recentSearches = MutableStateFlow(loadRecentSearches())
    val recentSearches: StateFlow<List<String>> = _recentSearches.asStateFlow()

    fun refresh() {
        _recentSearches.value = loadRecentSearches()
    }

    fun addRecentSearch(query: String) {
        val trimmedQuery = query.trim()
        if (trimmedQuery.isBlank()) return

        val updatedSearches = (
            listOf(trimmedQuery) + _recentSearches.value.filterNot {
                it.equals(trimmedQuery, ignoreCase = true)
            }
        ).take(MAX_RECENT_SEARCHES)

        saveRecentSearches(updatedSearches)
        _recentSearches.value = updatedSearches
    }

    fun removeRecentSearch(query: String) {
        val trimmedQuery = query.trim()
        if (trimmedQuery.isBlank()) return

        val updatedSearches = _recentSearches.value.filterNot {
            it.equals(trimmedQuery, ignoreCase = true)
        }
        saveRecentSearches(updatedSearches)
        _recentSearches.value = updatedSearches
    }

    fun clearRecentSearches() {
        saveRecentSearches(emptyList())
        _recentSearches.value = emptyList()
    }

    private fun loadRecentSearches(): List<String> {
        val rawSearches = prefs.getString(recentSearchesKey, null) ?: return emptyList()
        return runCatching {
            val jsonArray = JSONArray(rawSearches)
            List(jsonArray.length()) { index -> jsonArray.optString(index) }
                .map { it.trim() }
                .filter { it.isNotBlank() }
                .distinctBy { it.lowercase() }
                .take(MAX_RECENT_SEARCHES)
        }.getOrElse {
            emptyList()
        }
    }

    private fun saveRecentSearches(searches: List<String>) {
        if (searches.isEmpty()) {
            prefs.edit().remove(recentSearchesKey).apply()
            return
        }

        val jsonArray = JSONArray()
        searches.forEach { jsonArray.put(it) }
        prefs.edit()
            .putString(recentSearchesKey, jsonArray.toString())
            .apply()
    }

    private val recentSearchesKey: String
        get() = "$KEY_RECENT_SEARCHES_PREFIX${sessionUserId}"

    private val sessionUserId: Long
        get() = authRepository.getSavedSession()?.userId?.toLong() ?: -1L

    companion object {
        private const val PREFS_NAME = "travel_hub_search"
        private const val KEY_RECENT_SEARCHES_PREFIX = "recent_searches_user_"
        private const val MAX_RECENT_SEARCHES = 10
    }
}
