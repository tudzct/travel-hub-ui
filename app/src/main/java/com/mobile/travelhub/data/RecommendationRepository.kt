package com.mobile.travelhub.data

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONObject

@Singleton
class RecommendationRepository @Inject constructor(
    @param:ApplicationContext private val context: Context
) {
    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

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

        prefs.edit()
            .putString(KEY_PLACE_CLICKS, mapToJson(placeMap).toString())
            .putString(KEY_PROVINCE_CLICKS, mapToJson(provinceMap).toString())
            .apply()
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

