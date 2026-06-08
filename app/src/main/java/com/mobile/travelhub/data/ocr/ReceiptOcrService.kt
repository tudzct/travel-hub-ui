package com.mobile.travelhub.data.ocr

import android.content.Context
import android.net.Uri
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import com.mobile.travelhub.data.model.ReceiptOcrResult
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.tasks.await

@Singleton
class ReceiptOcrService @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

    suspend fun scanReceipt(imageUri: Uri): ReceiptOcrResult {
        val image = InputImage.fromFilePath(context, imageUri)
        val visionText = recognizer.process(image).await()
        return ReceiptOcrParser.parse(visionText.text)
    }
}
