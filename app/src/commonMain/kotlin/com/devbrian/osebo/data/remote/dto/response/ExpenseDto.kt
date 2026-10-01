package com.devbrian.osebo.data.remote.dto.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ExpenseDto(
    @SerialName("id") val id: String,
    @SerialName("amount") val amount: Double,
    @SerialName("description") val description: String,
    @SerialName("category_id") val categoryId: String?,
    @SerialName("category_name") val categoryName: String?,
    @SerialName("shop_id") val shopId: String,
    @SerialName("date") val date: String,
    @SerialName("created_at") val createdAt: String,
    @SerialName("updated_at") val updatedAt: String
)


