package com.mobile.travelhub.data

import android.content.Context
import com.mobile.travelhub.data.api.AIClient
import com.mobile.travelhub.data.model.PreferenceUpdateRequest
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONObject
import androidx.core.content.edit

@Singleton
class RecommendationRepository @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val authRepository: AuthRepository
) {
    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val api = AIClient.apiService

    private val _placeClicks = MutableStateFlow(loadMap(KEY_PLACE_CLICKS))
    private val _provinceClicks = MutableStateFlow(loadMap(KEY_PROVINCE_CLICKS))

    val placeClicks: StateFlow<Map<String, Int>> = _placeClicks.asStateFlow()
    val provinceClicks: StateFlow<Map<String, Int>> = _provinceClicks.asStateFlow()

    fun recordPlaceOpened(placeId: String, provinceName: String) {
        val placeMap = _placeClicks.value.toMutableMap()
        placeMap[placeId] = (placeMap[placeId] ?: 0) + 1

        val provinceMap = _provinceClicks.value.toMutableMap()
        val normalizedProvince = provinceName.trim().lowercase()
        if (normalizedProvince.isNotEmpty()) {
            provinceMap[normalizedProvince] = (provinceMap[normalizedProvince] ?: 0) + 1
        }

        _placeClicks.value = placeMap
        _provinceClicks.value = provinceMap

        prefs.edit {
            putString(KEY_PLACE_CLICKS, mapToJson(placeMap).toString())
                .putString(KEY_PROVINCE_CLICKS, mapToJson(provinceMap).toString())
        }
    }

    suspend fun syncPreferencesToServer(
        tripType: String?,
        interests: List<String>,
        destination: String?
    ): Result<Unit> {
        val normalizedTripType = tripType?.trim()?.takeIf { it.isNotEmpty() }
        val normalizedInterests = interests
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .distinct()
        val normalizedDestination = destination?.trim()?.takeIf { it.isNotEmpty() }

        val session = authRepository.getSavedSession()
            ?: return Result.failure(IllegalStateException("Cannot sync preferences before login"))

        return runCatching {
            api.updatePreferences(
                id = session.userId.toLong(),
                request = PreferenceUpdateRequest(
                    tripType = normalizedTripType,
                    interests = normalizedInterests,
                    destination = normalizedDestination
                )
            )
        }
    }

    companion object {
        private const val PREFS_NAME = "recommendation_store"
        private const val KEY_PLACE_CLICKS = "place_clicks"
        private const val KEY_PROVINCE_CLICKS = "province_clicks"

        private fun mapToJson(map: Map<String, Int>): JSONObject {
            return JSONObject().apply {
                map.forEach { (key, value) -> put(key, value) }
            }
        }

        private fun jsonToMap(json: String): Map<String, Int> {
            val parsed = runCatching { JSONObject(json) }.getOrNull() ?: return emptyMap()
            return buildMap {
                parsed.keys().forEach { key ->
                    put(key, parsed.optInt(key, 0))
                }
            }
        }
    }

    private fun loadMap(key: String): Map<String, Int> {
        val raw = prefs.getString(key, null) ?: return emptyMap()
        return jsonToMap(raw)
    }
}

