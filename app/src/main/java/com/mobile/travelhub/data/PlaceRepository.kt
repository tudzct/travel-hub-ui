package com.mobile.travelhub.data

import android.content.Context
import com.mobile.travelhub.models.EditablePlaceDraft
import com.mobile.travelhub.models.PlaceDetail
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PlaceRepository @Inject constructor(
    @param:ApplicationContext private val context: Context
) {
    private val basePlaces: List<PlaceDetail> = loadPlacesFromAssets()
    private val overrides = linkedMapOf<String, PlaceDetail>()
    private val _places = MutableStateFlow(basePlaces)

    fun observePlaces(): StateFlow<List<PlaceDetail>> = _places.asStateFlow()

    fun getPlaces(): List<PlaceDetail> = _places.value

    fun getPlaceDetail(id: String): PlaceDetail? = _places.value.firstOrNull { it.id == id }

    fun updatePlace(id: String, draft: EditablePlaceDraft): Result<PlaceDetail> {
        val existing = getPlaceDetail(id)
            ?: return Result.failure(IllegalArgumentException("Place not found"))

        val title = draft.title.trim()
        val description = draft.description.trim()
        val provinceName = draft.provinceName.trim()
        val mainImageUrl = draft.mainImageUrl.trim()
        val bestTime = draft.bestTime.trim().ifBlank { null }
        val galleryUrls = draft.galleryUrls
            .map { it.trim() }
            .filter { it.isNotEmpty() }

        when {
            title.isEmpty() -> return Result.failure(IllegalArgumentException("Title is required"))
            description.isEmpty() -> return Result.failure(IllegalArgumentException("Description is required"))
            provinceName.isEmpty() -> return Result.failure(IllegalArgumentException("Province is required"))
            mainImageUrl.isEmpty() -> return Result.failure(IllegalArgumentException("Main image URL is required"))
        }

        val updated = existing.copy(
            title = title,
            description = description,
            provinceName = provinceName,
            bestTime = bestTime,
            mainImageUrl = mainImageUrl,
            galleryUrls = galleryUrls
        )

        overrides[id] = updated
        _places.value = basePlaces.map { place -> overrides[place.id] ?: place }
        return Result.success(updated)
    }

    private fun loadPlacesFromAssets(): List<PlaceDetail> {
        return runCatching {
            val raw = context.assets.open("places.json")
                .bufferedReader()
                .use { it.readText() }
            val array = JSONArray(raw)
            buildList {
                for (index in 0 until array.length()) {
                    val item = array.getJSONObject(index)
                    add(item.toPlaceDetail())
                }
            }
        }.getOrElse { throwable ->
            throw IOException("Failed to load local places data", throwable)
        }
    }

    private fun JSONObject.toPlaceDetail(): PlaceDetail {
        return PlaceDetail(
            id = optString("id"),
            title = optString("title"),
            description = optString("description"),
            provinceName = optString("provinceName"),
            bestTime = optString("bestTime").ifBlank { null },
            mainImageUrl = optString("mainImageUrl"),
            galleryUrls = optJsonArray("galleryUrls").toStringList(),
            viewCountLabel = optString("viewCountLabel").ifBlank { null },
            sourceUrl = optString("sourceUrl").ifBlank { null }
        )
    }

    private fun JSONObject.optJsonArray(key: String): JSONArray {
        return optJSONArray(key) ?: JSONArray()
    }

    private fun JSONArray.toStringList(): List<String> {
        return buildList {
            for (index in 0 until length()) {
                add(optString(index))
            }
        }.filter { it.isNotBlank() }
    }
}
