package com.devbrian.osebo.data.remote.dto.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


@Serializable
data class MonthlyRevenue(
    @SerialName("month")
    val month: String,  

    @SerialName("year")
    val year: Int,

    @SerialName("revenue")
    val revenue: Double,

    @SerialName("orders")
    val orders: Int,

    @SerialName("growth")
    val growth: Double? = null  
)

@Serializable
data class ShopStatsResponse(
    @SerialName("totalProducts")
    val totalProducts: Int,

    @SerialName("totalOrders")
    val totalOrders: Int,

    @SerialName("totalRevenue")
    val totalRevenue: Double,

    @SerialName("totalCustomers")
    val totalCustomers: Int,

    @SerialName("monthlyRevenue")
    val monthlyRevenue: List<MonthlyRevenue>
)


