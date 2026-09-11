package com.devbrian.osebo.data.remote.dto.response

import com.google.gson.annotations.SerializedName

data class SubscriptionReportDto(
    @SerializedName("total_revenue")
    val totalRevenue: Double,

    @SerializedName("active_subscriptions")
    val activeSubscriptions: Int,

    @SerializedName("new_subscriptions")  
    val newSubscriptions: Int,

    @SerializedName("cancelled_subscriptions")
    val cancelledSubscriptions: Int,

    @SerializedName("revenue_by_plan")
    val revenueByPlan: Map<String, Double>,

    @SerializedName("monthly_revenue")
    val monthlyRevenue: List<MonthlyRevenueDto>
)

data class MonthlyRevenueDto(
    @SerializedName("month")
    val month: String,

    @SerializedName("revenue")
    val revenue: Double,

    @SerializedName("new_subscriptions")
    val newSubscriptions: Int
)


