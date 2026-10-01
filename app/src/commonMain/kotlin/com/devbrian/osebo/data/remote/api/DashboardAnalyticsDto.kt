package com.devbrian.osebo.data.remote.api

import com.devbrian.osebo.models.SalesDataPoint
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class DashboardAnalyticsDto(
    @SerialName("sales_data")
    val salesData: List<SalesDataPoint>,

    @SerialName("revenue_data")
    val revenueData: List<RevenueDataPoint>,

    @SerialName("customer_growth")
    val customerGrowth: List<GrowthDataPoint>
)


@Serializable
data class RevenueDataPoint(
    @SerialName("date")
    val date: String,

    @SerialName("revenue")
    val revenue: Double
)

@Serializable
data class GrowthDataPoint(
    @SerialName("date")
    val date: String,

    @SerialName("customers")
    val customers: Int
)


