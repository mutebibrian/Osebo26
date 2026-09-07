package com.devbrian.osebo.data.remote.dto.response

import com.google.gson.annotations.SerializedName

data class CustomersSummaryDto(
    @SerializedName("total") val total: Int,
    @SerializedName("new_today") val newToday: Int,
    @SerializedName("new_this_month") val newThisMonth: Int,
    @SerializedName("top_customers") val topCustomers: List<CustomerSummaryItem>
)

data class CustomerSummaryItem(
    @SerializedName("id") val id: String,
    @SerializedName("name") val name: String,
    @SerializedName("total_purchases") val totalPurchases: Double,
    @SerializedName("last_purchase") val lastPurchase: String
)


