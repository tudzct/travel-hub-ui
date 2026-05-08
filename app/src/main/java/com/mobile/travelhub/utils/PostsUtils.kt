package com.mobile.travelhub.utils

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

object  PostsUtils {
    private fun parseTimestampToMillis(value: String): Long? {
        val formats = listOf(
            "yyyy-MM-dd'T'HH:mm:ss.SSSSSSX",
            "yyyy-MM-dd'T'HH:mm:ss.SSSSSS'Z'",
            "yyyy-MM-dd'T'HH:mm:ss.SSSSSS",
            "yyyy-MM-dd'T'HH:mm:ss.SSSX",
            "yyyy-MM-dd'T'HH:mm:ss.SSS",
            "yyyy-MM-dd'T'HH:mm:ssX",
            "yyyy-MM-dd'T'HH:mm:ss",
            "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
            "yyyy-MM-dd'T'HH:mm:ss'Z'",
            "yyyy-MM-dd HH:mm:ss"
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
    fun formatTimeAgo(rawTimestamp: String?): String {
        if (rawTimestamp.isNullOrBlank()) return "Just Now"

        val createdAtMillis = parseTimestampToMillis(rawTimestamp) ?: return "Just Now"
        val durationMillis = (System.currentTimeMillis() - createdAtMillis).coerceAtLeast(0L)

        val minutes = durationMillis / 60_000
        return when {
            minutes < 1 -> "Just Now"
            minutes < 60 -> "${minutes}m ago"
            minutes < 60 * 24 -> "${minutes / 60}h ago"
            else -> "${minutes / (60 * 24)}d ago"
        }
    }
}