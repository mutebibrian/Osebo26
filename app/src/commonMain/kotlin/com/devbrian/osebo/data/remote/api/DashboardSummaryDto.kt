package com.devbrian.osebo.data.remote.api

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class DashboardSummaryDto(
    @SerialName("total_sales")
    val totalSales: Double,

    @SerialName("total_orders")
    val totalOrders: Int,

    @SerialName("total_customers")
    val totalCustomers: Int,

    @SerialName("total_products")
    val totalProducts: Int
)


