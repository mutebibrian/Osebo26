package com.devbrian.osebo.models

import com.google.gson.annotations.SerializedName

data class TimeSeriesApiResponse(
    @SerializedName("xAxis")
    val xAxis: List<String> = emptyList(),

    @SerializedName("sales")
    val sales: List<Double> = emptyList(),

    @SerializedName("expenses")
    val expenses: List<Double> = emptyList()
)