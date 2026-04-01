package com.devbrian.osebo.data.remote.dto.response

import com.devbrian.osebo.data.remote.dto.request.EmployeeData
import com.google.gson.annotations.SerializedName

data class SingleEmployeeResponse(
    @SerializedName("success")
    val success: Boolean,

    @SerializedName("message")
    val message: String?,

    @SerializedName("data")
    val data: EmployeeData?
)