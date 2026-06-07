package com.mobile.travelhub.data

import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import org.json.JSONObject
import retrofit2.HttpException
import java.io.IOException
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

fun Throwable.httpStatusCode(): Int? = (this as? HttpException)?.code()

fun Throwable.userMessage(fallback: String = "Đã có lỗi xảy ra. Vui lòng thử lại sau."): String {
    apiErrorMessage()?.let { return it }

    firebaseAuthMessage()?.let { return it }

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

private fun Throwable.firebaseAuthMessage(): String? {
    val rawMessage = message.orEmpty()

    when (this) {
        is FirebaseAuthUserCollisionException -> {
            val email = extractEmailFromMessage(rawMessage)
            return if (email != null) {
                "Email $email đã được dùng cho một tài khoản khác."
            } else {
                "Email này đã được dùng cho một tài khoản khác."
            }
        }

        is FirebaseAuthWeakPasswordException -> {
            return "Mật khẩu quá yếu. Hãy dùng mật khẩu có ít nhất 8 ký tự, gồm chữ và số."
        }

        is FirebaseAuthInvalidUserException -> {
            return when (errorCode) {
                "ERROR_USER_NOT_FOUND" -> "Không tìm thấy tài khoản với email này."
                "ERROR_USER_DISABLED" -> "Tài khoản này đã bị vô hiệu hoá."
                else -> "Tài khoản không hợp lệ. Vui lòng kiểm tra lại."
            }
        }

        is FirebaseAuthInvalidCredentialsException -> {
            return when (errorCode) {
                "ERROR_WRONG_PASSWORD" -> "Mật khẩu không đúng."
                "ERROR_INVALID_EMAIL" -> "Email không hợp lệ."
                "ERROR_ACCOUNT_EXISTS_WITH_DIFFERENT_CREDENTIAL" ->
                    "Email này đã được dùng với cách đăng nhập khác."
                else -> "Thông tin đăng nhập không hợp lệ."
            }
        }

        is FirebaseAuthException -> {
            return when (errorCode) {
                "ERROR_EMAIL_ALREADY_IN_USE" -> "Email này đã được dùng cho một tài khoản khác."
                "ERROR_INVALID_EMAIL" -> "Email không hợp lệ."
                "ERROR_WRONG_PASSWORD" -> "Mật khẩu không đúng."
                "ERROR_USER_DISABLED" -> "Tài khoản này đã bị vô hiệu hoá."
                "ERROR_USER_NOT_FOUND" -> "Không tìm thấy tài khoản với email này."
                else -> null
            }
        }
    }

    if (
        rawMessage.contains("email address is already in use", ignoreCase = true) ||
        rawMessage.contains("already in use by another account", ignoreCase = true) ||
        rawMessage.contains("credential is already associated", ignoreCase = true)
    ) {
        return "Email này đã được dùng cho một tài khoản khác."
    }

    if (rawMessage.contains("password is invalid", ignoreCase = true)) {
        return "Mật khẩu không đúng."
    }

    if (rawMessage.contains("no user record", ignoreCase = true)) {
        return "Không tìm thấy tài khoản với email này."
    }

    return null
}

private fun extractEmailFromMessage(message: String): String? {
    val emailPattern = Regex("""[A-Z0-9._%+-]+@[A-Z0-9.-]+\.[A-Z]{2,}""", RegexOption.IGNORE_CASE)
    return emailPattern.find(message)?.value
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
