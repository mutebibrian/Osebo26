package com.devbrian.osebo.data.remote.dto.response

import com.google.gson.annotations.SerializedName

data class EmployeesSummaryDto(
    @SerializedName("total") val total: Int,
    @SerializedName("active") val active: Int,
    @SerializedName("managers") val managers: Int,
    @SerializedName("staff") val staff: Int
)