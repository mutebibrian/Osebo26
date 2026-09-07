package com.devbrian.osebo.data.remote.api

import com.devbrian.osebo.models.SalesDataPoint
import com.google.gson.annotations.SerializedName

data class DashboardAnalyticsDto(
    @SerializedName("sales_data")
    val salesData: List<SalesDataPoint>,

    @SerializedName("revenue_data")
    val revenueData: List<RevenueDataPoint>,

    @SerializedName("customer_growth")
    val customerGrowth: List<GrowthDataPoint>
)


data class RevenueDataPoint(
    @SerializedName("date")
    val date: String,

    @SerializedName("revenue")
    val revenue: Double
)

data class GrowthDataPoint(
    @SerializedName("date")
    val date: String,

    @SerializedName("customers")
    val customers: Int
)


