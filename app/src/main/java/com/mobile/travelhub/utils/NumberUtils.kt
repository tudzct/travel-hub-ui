package com.mobile.travelhub.utils

import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import java.text.NumberFormat
import java.util.Locale

object NumberUtils {
    private val viLocale = Locale.forLanguageTag("vi-VN")
    private val formatter = NumberFormat.getNumberInstance(viLocale)

    fun formatVnd(amount: Double): String {
        return "${formatter.format(amount)} đ"
    }

    fun formatVndNoSymbol(amount: Double): String {
        return formatter.format(amount)
    }

    fun formatInputString(input: String): String {
        val clean = input.replace(".", "").replace("[^0-9]".toRegex(), "")
        if (clean.isEmpty()) return ""
        val parsed = clean.toLongOrNull() ?: return ""
        return formatter.format(parsed)
    }

    fun cleanInputToDouble(input: String): Double {
        val clean = input.replace(".", "").trim()
        return clean.toDoubleOrNull() ?: 0.0
    }

    fun formatTextFieldValue(newValue: TextFieldValue): TextFieldValue {
        val originalText = newValue.text
        val originalSelection = newValue.selection
        
        // 1. Calculate how many digits are before the cursor in the typed text
        val cursorPosition = originalSelection.start
        val textBeforeCursor = originalText.take(cursorPosition)
        val digitsBeforeCursor = textBeforeCursor.count { it.isDigit() }
        
        // 2. Clean the entire text (remove all non-digits)
        val cleanText = originalText.replace(".", "").filter { it.isDigit() }
        if (cleanText.isEmpty()) {
            return TextFieldValue("", selection = TextRange(0))
        }
        
        // 3. Format the clean digits
        val longVal = cleanText.toLongOrNull() ?: return TextFieldValue("", selection = TextRange(0))
        val formattedText = formatter.format(longVal)
        
        // 4. Find the cursor position in the formatted text
        var newCursorPos = 0
        var digitCount = 0
        for (i in 0 until formattedText.length) {
            if (digitCount == digitsBeforeCursor) {
                break
            }
            if (formattedText[i].isDigit()) {
                digitCount++
            }
            newCursorPos++
        }
        
        if (digitCount < digitsBeforeCursor) {
            newCursorPos = formattedText.length
        }
        
        return TextFieldValue(
            text = formattedText,
            selection = TextRange(newCursorPos)
        )
    }
}
