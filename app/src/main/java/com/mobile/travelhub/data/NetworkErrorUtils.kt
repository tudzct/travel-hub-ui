package com.mobile.travelhub.data

import org.json.JSONObject
import retrofit2.HttpException
import java.io.IOException
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

fun Throwable.httpStatusCode(): Int? = (this as? HttpException)?.code()

fun Throwable.userMessage(fallback: String = "Đã có lỗi xảy ra. Vui lòng thử lại sau."): String {
    apiErrorMessage()?.let { return it }

    val rawMessage = message.orEmpty()
    apiErrorMessageFromText(rawMessage)?.let { return it }

    return when {
        this is UnknownHostException ||
            this is ConnectException ||
            rawMessage.contains("failed to connect", ignoreCase = true) ||
            rawMessage.contains("Unable to resolve host", ignoreCase = true) ->
            "Không thể kết nối tới máy chủ. Vui lòng kiểm tra kết nối mạng và thử lại."

        this is SocketTimeoutException ||
            rawMessage.contains("timeout", ignoreCase = true) ||
            rawMessage.contains("timed out", ignoreCase = true) ->
            "Kết nối tới máy chủ quá lâu. Vui lòng thử lại sau."

        rawMessage.contains("Canceled", ignoreCase = true) ->
            "Yêu cầu đã bị hủy."

        rawMessage.isBlank() -> fallback
        rawMessage.isVietnameseOrReadableUserMessage() -> rawMessage
        else -> fallback
    }
}

fun apiErrorMessageFromText(text: String?): String? {
    if (text.isNullOrBlank()) return null

    val jsonStart = text.indexOf('{')
    if (jsonStart < 0) return null

    return runCatching {
        extractApiErrorMessage(JSONObject(text.substring(jsonStart)))
    }.getOrNull()
}

private fun Throwable.apiErrorMessage(): String? {
    if (this is HttpException) {
        val body = response()?.errorBody()?.string()
        return apiErrorMessageFromText(body)
    }

    if (this is IOException) {
        cause?.apiErrorMessage()?.let { return it }
    }

    return null
}

private fun extractApiErrorMessage(json: JSONObject): String? {
    json.optString("message")
        .takeIf { it.isNotBlank() }
        ?.let { return it }

    val errors = json.optJSONArray("errors")
    if (errors != null && errors.length() > 0) {
        val first = errors.optJSONObject(0)
        first?.optString("message")
            ?.takeIf { it.isNotBlank() }
            ?.let { return it }
    }

    return null
}

private fun String.isVietnameseOrReadableUserMessage(): Boolean {
    val lower = lowercase()
    val technicalFragments = listOf(
        "failed to",
        "request failed",
        "server returned",
        "http ",
        "java.",
        "retrofit2",
        "okhttp",
        "socket",
        "connectexception",
        "unknownhost",
        "timeout",
        "supabase_storage_bucket"
    )

    if (technicalFragments.any { lower.contains(it) }) {
        return false
    }

    return any { it in 'À'..'ỹ' || it == 'Đ' || it == 'đ' } ||
        lower.startsWith("không ") ||
        lower.startsWith("bạn ") ||
        lower.startsWith("vui lòng ") ||
        lower.startsWith("thiếu ") ||
        lower.startsWith("ngày ") ||
        lower.startsWith("mã ")
}
