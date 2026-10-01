package com.devbrian.osebo.data.remote.dto.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ExpensesSummaryDto(
    @SerialName("today") val today: Double,
    @SerialName("this_month") val thisMonth: Double,
    @SerialName("last_month") val lastMonth: Double,
    @SerialName("percentage_change") val percentageChange: Double
)


