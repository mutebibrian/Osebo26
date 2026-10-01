package com.devbrian.osebo.data.remote.dto.response

import com.devbrian.osebo.data.remote.dto.request.EmployeeData
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SingleEmployeeResponse(
    @SerialName("success")
    val success: Boolean,

    @SerialName("message")
    val message: String?,

    @SerialName("data")
    val data: EmployeeData?
)
