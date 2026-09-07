package com.devbrian.osebo.models

import com.devbrian.osebo.data.remote.dto.request.EmployeeData
import com.google.gson.annotations.SerializedName


data class EmployeeResponse(
    @SerializedName("success")
    val success: Boolean,

    @SerializedName("message")
    val message: String?,

    @SerializedName("data")
    val data: List<EmployeeData>?
)


data class CreateEmployeeResponse(
    @SerializedName("success")
    val success: Boolean,

    @SerializedName("message")
    val message: String?,

    @SerializedName("data")
    val data: EmployeeData?
)
