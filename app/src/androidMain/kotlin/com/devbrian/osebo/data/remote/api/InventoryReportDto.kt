package com.devbrian.osebo.data.remote.api


import com.google.gson.annotations.SerializedName

data class InventoryReportDto(
    @SerializedName("total_items")
    val totalItems: Int,

    @SerializedName("total_value")
    val totalValue: Double
)


