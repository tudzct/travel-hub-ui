package com.mobile.travelhub.data.ocr

import com.mobile.travelhub.data.model.ReceiptItem
import com.mobile.travelhub.data.model.ReceiptOcrResult
import java.text.Normalizer
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

object ReceiptOcrParser {
    private val dateDmyRegex = Regex("""\b(\d{1,2})[\/\-.](\d{1,2})[\/\-.](\d{2,4})\b""")
    private val dateYmdRegex = Regex("""\b(\d{4})[\/\-.](\d{1,2})[\/\-.](\d{1,2})\b""")
    private val totalKeywords = listOf(
        "khach phai tra",
        "can thanh toan",
        "tong thanh toan",
        "tong cong",
        "thanh toan",
        "grand total",
        "amount due",
        "phai tra",
        "total"
    )
    private val merchantSkipWords = listOf(
        "hoa don",
        "invoice",
        "receipt",
        "tax",
        "mst",
        "vat"
    )

    fun parse(rawText: String): ReceiptOcrResult {
        val lines = rawText
            .lineSequence()
            .map { it.trim() }
            .filter { it.isNotBlank() }
            .toList()

        return ReceiptOcrResult(
            merchantName = parseMerchantName(lines),
            expenseDate = parseDate(lines),
            totalAmount = parseTotalAmount(lines),
            rawText = rawText,
            items = parseItems(lines)
        )
    }

    private fun parseMerchantName(lines: List<String>): String? {
        return lines.firstOrNull { line ->
            val normalized = normalize(line)
            merchantSkipWords.none { normalized.contains(it) }
        } ?: lines.firstOrNull()
    }

    private fun parseDate(lines: List<String>): String {
        lines.forEach { line ->
            dateYmdRegex.find(line)?.let { match ->
                return normalizeDate(
                    year = match.groupValues[1].toIntOrNull(),
                    month = match.groupValues[2].toIntOrNull(),
                    day = match.groupValues[3].toIntOrNull()
                )
            }
            dateDmyRegex.find(line)?.let { match ->
                val rawYear = match.groupValues[3]
                val year = rawYear.toIntOrNull()?.let { if (rawYear.length == 2) 2000 + it else it }
                return normalizeDate(
                    year = year,
                    month = match.groupValues[2].toIntOrNull(),
                    day = match.groupValues[1].toIntOrNull()
                )
            }
        }
        return LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)
    }

    private fun parseTotalAmount(lines: List<String>): Long? {
        val normalizedLines = lines.map { normalize(it) }

        for (index in lines.indices.reversed()) {
            if (totalKeywords.any { normalizedLines[index].contains(it) }) {
                val sameLine = extractMoneyCandidates(lines[index])
                if (sameLine.isNotEmpty()) {
                    return sameLine.last()
                }

                val nearbyLines = lines
                    .drop(index + 1)
                    .take(3)
                val nearbyCandidates = nearbyLines.flatMap { extractMoneyCandidates(it) }
                if (nearbyCandidates.isNotEmpty()) {
                    return nearbyCandidates.last()
                }
            }
        }

        return lines
            .takeLast(maxOf(4, lines.size / 3))
            .flatMap { extractMoneyCandidates(it) }
            .lastOrNull()
            ?: extractMoneyCandidates(lines.joinToString(separator = "\n")).maxOrNull()
    }

    private fun parseItems(lines: List<String>): List<ReceiptItem> {
        return lines.mapNotNull { line ->
            val amounts = extractMoneyCandidates(line)
            val amount = amounts.maxOrNull() ?: return@mapNotNull null
            val name = line
                .replace(Regex("""[\d\s.,]+(?:đ|vnd|VND)?$"""), "")
                .trim(' ', '-', ':')
            if (name.length < 3 || totalKeywords.any { normalize(name).contains(it) }) {
                null
            } else {
                ReceiptItem(
                    name = name,
                    quantity = null,
                    unitPrice = null,
                    totalPrice = amount
                )
            }
        }.take(12)
    }

    private fun extractMoneyCandidates(text: String): List<Long> {
        val moneyRegex = Regex("""(?<!\d)(\d{1,3}(?:[.,\s]\d{3})+|\d{5,})(?:\s?(?:đ|d|vnd|VND))?(?!\d)""")
        return moneyRegex.findAll(text)
            .mapNotNull { match ->
                match.value
                    .filter { it.isDigit() }
                    .toLongOrNull()
            }
            .filter { it > 0L }
            .toList()
    }

    private fun normalizeDate(year: Int?, month: Int?, day: Int?): String {
        return runCatching {
            LocalDate.of(year ?: error("missing year"), month ?: error("missing month"), day ?: error("missing day"))
                .format(DateTimeFormatter.ISO_LOCAL_DATE)
        }.getOrElse {
            LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)
        }
    }

    private fun normalize(value: String): String {
        val noAccent = Normalizer.normalize(value.lowercase(Locale.ROOT), Normalizer.Form.NFD)
            .replace(Regex("\\p{Mn}+"), "")
        return noAccent.replace('đ', 'd')
    }
}
