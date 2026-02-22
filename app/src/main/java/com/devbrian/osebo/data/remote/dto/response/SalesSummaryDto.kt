package com.devbrian.osebo.data.remote.dto.response

data class SalesSummaryDto(
    val today: Double,
    val this_month: Double,
    val last_month: Double,
    val percentage_change: Double,
    val transaction_count: Int
)