package com.devbrian.osebo.data.remote.api


import com.google.gson.annotations.SerializedName

data class TransactionDto(
    @SerializedName("id")
    val id: String,

    @SerializedName("type")
    val type: String, // "income" or "expense"

    @SerializedName("amount")
    val amount: Double,

    @SerializedName("description")
    val description: String,

    @SerializedName("category")
    val category: String,

    @SerializedName("date")
    val date: String,

    @SerializedName("reference")
    val reference: String?
)