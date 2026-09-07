package com.devbrian.osebo.data.remote.api


import com.google.gson.annotations.SerializedName

data class SalesReportDto(
    @SerializedName("period")
    val period: String,

    @SerializedName("total_sales")
    val totalSales: Double,

    @SerializedName("total_orders")
    val totalOrders: Int
)


