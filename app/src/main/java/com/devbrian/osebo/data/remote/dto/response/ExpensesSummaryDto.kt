package com.devbrian.osebo.data.remote.dto.response

import com.google.gson.annotations.SerializedName

data class ExpensesSummaryDto(
    @SerializedName("today") val today: Double,
    @SerializedName("this_month") val thisMonth: Double,
    @SerializedName("last_month") val lastMonth: Double,
    @SerializedName("percentage_change") val percentageChange: Double
)