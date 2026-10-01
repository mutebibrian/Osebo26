package com.devbrian.osebo.data.remote.api


import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TransactionDto(
    @SerialName("id")
    val id: String,

    @SerialName("type")
    val type: String, 

    @SerialName("amount")
    val amount: Double,

    @SerialName("description")
    val description: String,

    @SerialName("category")
    val category: String,

    @SerialName("date")
    val date: String,

    @SerialName("reference")
    val reference: String?
)


