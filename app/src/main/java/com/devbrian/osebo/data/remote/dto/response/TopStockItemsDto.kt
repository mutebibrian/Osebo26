package com.devbrian.osebo.data.remote.dto.response

import com.google.gson.annotations.SerializedName

data class TopStockItemsDto(
    @SerializedName("items") val items: List<TopStockItemDto>
)

data class TopStockItemDto(
    @SerializedName("id") val id: String,
    @SerializedName("name") val name: String,
    @SerializedName("totalQuantitySold") val totalQuantitySold: Int,
    @SerializedName("totalSalesAmount") val totalSalesAmount: Double
)
