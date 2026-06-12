package com.mobile.travelhub.ui.components

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.mobile.travelhub.R
import com.mobile.travelhub.data.userMessage
import com.mobile.travelhub.ui.theme.PrimaryBlue
import java.io.ByteArrayOutputStream
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

data class CroppedAvatar(
    val bytes: ByteArray,
    val mimeType: String,
    val fileName: String,
    val previewBitmap: Bitmap
)

@Composable
fun AvatarCropperScreen(
    imageUri: Uri,
    onCancel: () -> Unit,
    onCropDone: (CroppedAvatar) -> Unit
) {
    val context = LocalContext.current
    val density = LocalDensity.current
    val selectedImageReadFailedMessage = stringResource(R.string.selected_image_read_failed)
    val bitmapResult = remember(imageUri) {
        runCatching { loadEditableBitmapFromUri(context, imageUri) }
    }
    val bitmap = bitmapResult.getOrNull()

    BackHandler(onBack = onCancel)

    if (bitmap == null) {
        LaunchedEffect(imageUri) {
            Toast.makeText(
                context,
                bitmapResult.exceptionOrNull()
                    ?.userMessage(selectedImageReadFailedMessage)
                    ?: selectedImageReadFailedMessage,
                Toast.LENGTH_LONG
            ).show()
            onCancel()
        }
        return
    }

    var containerSize by remember { mutableStateOf(IntSize.Zero) }
    var cropSizePx by remember { mutableStateOf(0f) }
    var scale by remember { mutableStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }
    var isCropping by remember { mutableStateOf(false) }

    val minScale = remember(containerSize, cropSizePx, bitmap) {
        minimumCoverScale(bitmap, containerSize, cropSizePx)
    }
    val displayScale = max(scale, minScale)

    LaunchedEffect(minScale) {
        if (scale < minScale) {
            scale = minScale
            offset = clampCropOffset(bitmap, containerSize, cropSizePx, minScale, offset)
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color.Black
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp)
                    .padding(horizontal = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                TextButton(
                    onClick = onCancel,
                    modifier = Modifier.align(Alignment.CenterStart)
                ) {
                    Text("Hủy", color = Color.White, fontWeight = FontWeight.Bold)
                }
                Text(
                    text = "Cắt ảnh",
                    color = Color.White,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .onSizeChanged { size ->
                        containerSize = size
                        cropSizePx = min(size.width, size.height) * 0.78f
                        offset = clampCropOffset(bitmap, size, cropSizePx, displayScale, offset)
                    }
                    .pointerInput(bitmap, containerSize, cropSizePx, displayScale) {
                        detectTransformGestures { _, pan, zoom, _ ->
                            val nextScale = (displayScale * zoom).coerceIn(minScale, MAX_CROP_SCALE)
                            val nextOffset = clampCropOffset(
                                bitmap = bitmap,
                                containerSize = containerSize,
                                cropSizePx = cropSizePx,
                                scale = nextScale,
                                offset = offset + pan
                            )
                            scale = nextScale
                            offset = nextOffset
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                Image(
                    bitmap = bitmap.asImageBitmap(),
                    contentDescription = "Ảnh cần cắt",
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer {
                            scaleX = displayScale
                            scaleY = displayScale
                            translationX = offset.x
                            translationY = offset.y
                        },
                    contentScale = ContentScale.Fit
                )

                Canvas(
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer {
                            compositingStrategy = CompositingStrategy.Offscreen
                        }
                ) {
                    drawRect(Color.Black.copy(alpha = 0.58f))
                    drawCircle(
                        color = Color.Transparent,
                        radius = cropSizePx / 2f,
                        center = Offset(size.width / 2f, size.height / 2f),
                        blendMode = BlendMode.Clear
                    )
                }

                Box(
                    modifier = Modifier
                        .size(with(density) { cropSizePx.toDp() })
                        .clip(CircleShape)
                        .border(2.dp, Color.White, CircleShape)
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = onCancel,
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White.copy(alpha = 0.14f),
                        contentColor = Color.White
                    )
                ) {
                    Text("Hủy", fontWeight = FontWeight.Bold)
                }
                Button(
                    onClick = {
                        if (isCropping || containerSize == IntSize.Zero || cropSizePx <= 0f) {
                            return@Button
                        }
                        isCropping = true
                        try {
                            onCropDone(
                                cropAvatarBitmap(
                                    source = bitmap,
                                    sourceUri = imageUri,
                                    containerSize = containerSize,
                                    cropSizePx = cropSizePx,
                                    scale = displayScale,
                                    offset = offset
                                )
                            )
                        } catch (e: Exception) {
                            Toast.makeText(
                                context,
                                e.userMessage(selectedImageReadFailedMessage),
                                Toast.LENGTH_LONG
                            ).show()
                            isCropping = false
                        }
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp),
                    shape = RoundedCornerShape(16.dp),
                    enabled = !isCropping,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PrimaryBlue,
                        contentColor = Color.White
                    )
                ) {
                    if (isCropping) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            strokeWidth = 2.dp,
                            color = Color.White
                        )
                    } else {
                        Text("Done", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

private const val AVATAR_OUTPUT_SIZE = 500
private const val AVATAR_JPEG_QUALITY = 85
private const val MAX_CROP_SCALE = 5f

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

private fun cropAvatarBitmap(
    source: Bitmap,
    sourceUri: Uri,
    containerSize: IntSize,
    cropSizePx: Float,
    scale: Float,
    offset: Offset
): CroppedAvatar {
    val baseScale = baseFitScale(source, containerSize)
    val imageScale = baseScale * scale
    val displayedWidth = source.width * imageScale
    val displayedHeight = source.height * imageScale
    val imageLeft = (containerSize.width - displayedWidth) / 2f + offset.x
    val imageTop = (containerSize.height - displayedHeight) / 2f + offset.y
    val cropLeftOnScreen = (containerSize.width - cropSizePx) / 2f
    val cropTopOnScreen = (containerSize.height - cropSizePx) / 2f

    val cropLeft = ((cropLeftOnScreen - imageLeft) / imageScale).roundToInt()
    val cropTop = ((cropTopOnScreen - imageTop) / imageScale).roundToInt()
    val cropSize = (cropSizePx / imageScale).roundToInt().coerceAtLeast(1)
    val safeLeft = cropLeft.coerceIn(0, source.width - 1)
    val safeTop = cropTop.coerceIn(0, source.height - 1)
    val safeSize = min(
        cropSize,
        min(source.width - safeLeft, source.height - safeTop)
    ).coerceAtLeast(1)

    val cropped = Bitmap.createBitmap(source, safeLeft, safeTop, safeSize, safeSize)
    val outputBitmap = if (safeSize != AVATAR_OUTPUT_SIZE) {
        Bitmap.createScaledBitmap(cropped, AVATAR_OUTPUT_SIZE, AVATAR_OUTPUT_SIZE, true)
    } else {
        cropped
    }

    val outputStream = ByteArrayOutputStream()
    outputBitmap.compress(Bitmap.CompressFormat.JPEG, AVATAR_JPEG_QUALITY, outputStream)
    val bytes = outputStream.toByteArray()
    val previewBitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
        ?: throw IllegalStateException("Không thể tạo preview ảnh đã chọn")

    if (outputBitmap != cropped) {
        outputBitmap.recycle()
    }
    cropped.recycle()

    val fileName = (sourceUri.lastPathSegment
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

private fun minimumCoverScale(
    bitmap: Bitmap,
    containerSize: IntSize,
    cropSizePx: Float
): Float {
    if (containerSize.width <= 0 || containerSize.height <= 0 || cropSizePx <= 0f) {
        return 1f
    }
    val baseScale = baseFitScale(bitmap, containerSize)
    val minWidthScale = cropSizePx / (bitmap.width * baseScale)
    val minHeightScale = cropSizePx / (bitmap.height * baseScale)
    return max(1f, max(minWidthScale, minHeightScale))
}

private fun clampCropOffset(
    bitmap: Bitmap,
    containerSize: IntSize,
    cropSizePx: Float,
    scale: Float,
    offset: Offset
): Offset {
    if (containerSize.width <= 0 || containerSize.height <= 0 || cropSizePx <= 0f) {
        return Offset.Zero
    }
    val baseScale = baseFitScale(bitmap, containerSize)
    val displayedWidth = bitmap.width * baseScale * scale
    val displayedHeight = bitmap.height * baseScale * scale
    val maxX = max(0f, (displayedWidth - cropSizePx) / 2f)
    val maxY = max(0f, (displayedHeight - cropSizePx) / 2f)
    return Offset(
        x = offset.x.coerceIn(-maxX, maxX),
        y = offset.y.coerceIn(-maxY, maxY)
    )
}

private fun baseFitScale(bitmap: Bitmap, containerSize: IntSize): Float {
    if (containerSize.width <= 0 || containerSize.height <= 0) {
        return 1f
    }
    return min(
        containerSize.width.toFloat() / bitmap.width.toFloat(),
        containerSize.height.toFloat() / bitmap.height.toFloat()
    )
}
