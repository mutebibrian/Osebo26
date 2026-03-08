package com.devbrian.osebo.data.remote.dto.response

import com.google.gson.annotations.SerializedName


data class MonthlyRevenue(
    @SerializedName("month")
    val month: String,  

    @SerializedName("year")
    val year: Int,

    @SerializedName("revenue")
    val revenue: Double,

    @SerializedName("orders")
    val orders: Int,

    @SerializedName("growth")
    val growth: Double? = null  
)

data class ShopStatsResponse(
    @SerializedName("totalProducts")
    val totalProducts: Int,

    @SerializedName("totalOrders")
    val totalOrders: Int,

    @SerializedName("totalRevenue")
    val totalRevenue: Double,

    @SerializedName("totalCustomers")
    val totalCustomers: Int,

    @SerializedName("monthlyRevenue")
    val monthlyRevenue: List<MonthlyRevenue>
)

