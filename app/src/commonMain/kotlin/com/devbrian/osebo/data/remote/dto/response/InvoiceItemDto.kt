package com.devbrian.osebo.data.remote.dto.response


import kotlinx.serialization.Serializable
@Serializable
data class InvoiceItemDto(
    val description: String,
    val amount: Double,
    val quantity: Int
)


