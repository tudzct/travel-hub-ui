package com.mobile.travelhub.data.model

data class ReceiptOcrResult(
    val merchantName: String?,
    val expenseDate: String?,
    val totalAmount: Long?,
    val rawText: String,
    val items: List<ReceiptItem> = emptyList()
)

data class ReceiptItem(
    val name: String,
    val quantity: Int?,
    val unitPrice: Long?,
    val totalPrice: Long?
)
