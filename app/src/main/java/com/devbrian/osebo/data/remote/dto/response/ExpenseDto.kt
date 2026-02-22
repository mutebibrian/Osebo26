package com.devbrian.osebo.data.remote.dto.response

import com.google.gson.annotations.SerializedName

data class ExpenseDto(
    @SerializedName("id") val id: String,
    @SerializedName("amount") val amount: Double,
    @SerializedName("description") val description: String,
    @SerializedName("category_id") val categoryId: String?,
    @SerializedName("category_name") val categoryName: String?,
    @SerializedName("shop_id") val shopId: String,
    @SerializedName("date") val date: String,
    @SerializedName("created_at") val createdAt: String,
    @SerializedName("updated_at") val updatedAt: String
)