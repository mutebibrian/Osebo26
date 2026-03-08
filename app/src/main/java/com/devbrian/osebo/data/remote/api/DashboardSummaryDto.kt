package com.devbrian.osebo.data.remote.api

import com.google.gson.annotations.SerializedName

data class DashboardSummaryDto(
    @SerializedName("total_sales")
    val totalSales: Double,

    @SerializedName("total_orders")
    val totalOrders: Int,

    @SerializedName("total_customers")
    val totalCustomers: Int,

    @SerializedName("total_products")
    val totalProducts: Int
)

