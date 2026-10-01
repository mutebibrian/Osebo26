package com.devbrian.osebo.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TimeSeriesApiResponse(
    @SerialName("xAxis")
    val xAxis: List<String> = emptyList(),

    @SerialName("sales")
    val sales: List<Double> = emptyList(),

    @SerialName("expenses")
    val expenses: List<Double> = emptyList()
)
