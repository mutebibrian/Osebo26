package com.devbrian.osebo.data.remote.api


import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class InventoryReportDto(
    @SerialName("total_items")
    val totalItems: Int,

    @SerialName("total_value")
    val totalValue: Double
)


