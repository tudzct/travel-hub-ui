package com.mobile.travelhub.models

data class PlaceSummary(
    val id: String,
    val title: String,
    val shortDescription: String,
    val provinceName: String,
    val thumbnailUrl: String,
    val viewCountLabel: String?
)

data class PlaceDetail(
    val id: String,
    val title: String,
    val description: String,
    val provinceName: String,
    val bestTime: String?,
    val mainImageUrl: String,
    val galleryUrls: List<String>,
    val viewCountLabel: String?,
    val sourceUrl: String?
)

data class EditablePlaceDraft(
    val title: String,
    val description: String,
    val provinceName: String,
    val bestTime: String,
    val mainImageUrl: String,
    val galleryUrls: List<String>
)

fun PlaceDetail.toSummary(): PlaceSummary {
    val compactDescription = description
        .replace(Regex("\\s+"), " ")
        .trim()
        .let { text ->
            if (text.length <= 120) text else text.take(117).trimEnd() + "..."
        }

    return PlaceSummary(
        id = id,
        title = title,
        shortDescription = compactDescription,
        provinceName = provinceName,
        thumbnailUrl = mainImageUrl,
        viewCountLabel = viewCountLabel
    )
}

fun PlaceDetail.toEditableDraft(): EditablePlaceDraft = EditablePlaceDraft(
    title = title,
    description = description,
    provinceName = provinceName,
    bestTime = bestTime.orEmpty(),
    mainImageUrl = mainImageUrl,
    galleryUrls = galleryUrls
)
