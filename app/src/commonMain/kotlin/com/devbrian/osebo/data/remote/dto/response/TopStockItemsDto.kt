package com.devbrian.osebo.data.remote.dto.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TopStockItemsDto(
    @SerialName("items") val items: List<TopStockItemDto>
)

@Serializable
data class TopStockItemDto(
    @SerialName("id") val id: String,
    @SerialName("name") val name: String,
    @SerialName("totalQuantitySold") val totalQuantitySold: Int,
    @SerialName("totalSalesAmount") val totalSalesAmount: Double
)
