package com.devbrian.osebo.data.remote.dto.response

data class InvoiceDto(
    val id: String,
    val subscriptionId: String,
    val amount: Double,
    val currency: String,
    val status: String, 
    val invoiceDate: String,
    val dueDate: String,
    val paidDate: String?,
    val downloadUrl: String?,
    val items: List<InvoiceItemDto>
)


