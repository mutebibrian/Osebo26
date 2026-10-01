package com.devbrian.osebo.models

import com.devbrian.osebo.data.remote.dto.request.EmployeeData
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class EmployeeResponse(
    @SerialName("success")
    val success: Boolean,

    @SerialName("message")
    val message: String?,

    @SerialName("data")
    val data: List<EmployeeData>?
)

@Serializable
data class CreateEmployeeResponse(
    @SerialName("success")
    val success: Boolean,

    @SerialName("message")
    val message: String?,

    @SerialName("data")
    val data: EmployeeData?
)
