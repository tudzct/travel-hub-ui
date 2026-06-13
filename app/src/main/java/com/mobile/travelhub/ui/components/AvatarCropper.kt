package com.mobile.travelhub.ui.components

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import java.io.ByteArrayOutputStream
import kotlin.math.min

data class CroppedAvatar(
    val bytes: ByteArray,
    val mimeType: String,
    val fileName: String,
    val previewBitmap: Bitmap
)

private const val AVATAR_OUTPUT_SIZE = 500
private const val AVATAR_JPEG_QUALITY = 85

/**
 * Loads the picked image and prepares it for upload without a manual crop step:
 * the image is center-cropped to a square (to match the circular avatar frame) and
 * downscaled to [AVATAR_OUTPUT_SIZE].
 */
fun buildCroppedAvatar(context: Context, uri: Uri): CroppedAvatar {
    val source = loadEditableBitmapFromUri(context, uri)

    val squareSize = min(source.width, source.height).coerceAtLeast(1)
    val left = (source.width - squareSize) / 2
    val top = (source.height - squareSize) / 2
    val squared = Bitmap.createBitmap(source, left, top, squareSize, squareSize)
    val outputBitmap = if (squareSize != AVATAR_OUTPUT_SIZE) {
        Bitmap.createScaledBitmap(squared, AVATAR_OUTPUT_SIZE, AVATAR_OUTPUT_SIZE, true)
    } else {
        squared
    }

    val outputStream = ByteArrayOutputStream()
    outputBitmap.compress(Bitmap.CompressFormat.JPEG, AVATAR_JPEG_QUALITY, outputStream)
    val bytes = outputStream.toByteArray()
    val previewBitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
        ?: throw IllegalStateException("Không thể tạo preview ảnh đã chọn")

    if (outputBitmap != squared) {
        outputBitmap.recycle()
    }
    if (squared != source) {
        squared.recycle()
    }
    source.recycle()

    val fileName = (uri.lastPathSegment
        ?.substringAfterLast('/')
        ?.takeIf { it.isNotBlank() }
        ?: "avatar")
        .substringBeforeLast('.') + ".jpg"

    return CroppedAvatar(
        bytes = bytes,
        mimeType = "image/jpeg",
        fileName = fileName,
        previewBitmap = previewBitmap
    )
}

private fun loadEditableBitmapFromUri(context: Context, uri: Uri): Bitmap {
    val bitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
        val source = ImageDecoder.createSource(context.contentResolver, uri)
        ImageDecoder.decodeBitmap(source) { decoder, _, _ ->
            decoder.allocator = ImageDecoder.ALLOCATOR_SOFTWARE
            decoder.isMutableRequired = false
        }
    } else {
        context.contentResolver.openInputStream(uri).use { input ->
            BitmapFactory.decodeStream(input)
                ?: throw IllegalStateException("Không thể giải mã ảnh đã chọn")
        }
    }
    return if (bitmap.config == Bitmap.Config.ARGB_8888 && !bitmap.isRecycled) {
        bitmap
    } else {
        bitmap.copy(Bitmap.Config.ARGB_8888, false)
    }
}
