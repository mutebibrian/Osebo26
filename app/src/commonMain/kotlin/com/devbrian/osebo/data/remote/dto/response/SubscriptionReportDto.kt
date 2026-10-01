package com.devbrian.osebo.data.remote.dto.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SubscriptionReportDto(
    @SerialName("total_revenue")
    val totalRevenue: Double,

    @SerialName("active_subscriptions")
    val activeSubscriptions: Int,

    @SerialName("new_subscriptions")  
    val newSubscriptions: Int,

    @SerialName("cancelled_subscriptions")
    val cancelledSubscriptions: Int,

    @SerialName("revenue_by_plan")
    val revenueByPlan: Map<String, Double>,

    @SerialName("monthly_revenue")
    val monthlyRevenue: List<MonthlyRevenueDto>
)

@Serializable
data class MonthlyRevenueDto(
    @SerialName("month")
    val month: String,

    @SerialName("revenue")
    val revenue: Double,

    @SerialName("new_subscriptions")
    val newSubscriptions: Int
)


