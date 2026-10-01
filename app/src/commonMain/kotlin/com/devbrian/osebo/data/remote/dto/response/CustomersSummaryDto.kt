package com.devbrian.osebo.data.remote.dto.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CustomersSummaryDto(
    @SerialName("total") val total: Int,
    @SerialName("new_today") val newToday: Int,
    @SerialName("new_this_month") val newThisMonth: Int,
    @SerialName("top_customers") val topCustomers: List<CustomerSummaryItem>
)

@Serializable
data class CustomerSummaryItem(
    @SerialName("id") val id: String,
    @SerialName("name") val name: String,
    @SerialName("total_purchases") val totalPurchases: Double,
    @SerialName("last_purchase") val lastPurchase: String
)


