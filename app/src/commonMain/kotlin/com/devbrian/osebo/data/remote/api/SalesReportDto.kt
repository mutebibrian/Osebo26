package com.devbrian.osebo.data.remote.api


import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SalesReportDto(
    @SerialName("period")
    val period: String,

    @SerialName("total_sales")
    val totalSales: Double,

    @SerialName("total_orders")
    val totalOrders: Int
)


