package com.devbrian.osebo.data.remote.dto.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class EmployeesSummaryDto(
    @SerialName("total") val total: Int,
    @SerialName("active") val active: Int,
    @SerialName("managers") val managers: Int,
    @SerialName("staff") val staff: Int
)


