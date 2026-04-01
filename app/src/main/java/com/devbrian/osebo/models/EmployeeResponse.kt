package com.devbrian.osebo.models

import com.devbrian.osebo.data.remote.dto.request.EmployeeData
import com.google.gson.annotations.SerializedName

// ✅ Used for GET /api/users — API returns data as a LIST
data class EmployeeResponse(
    @SerializedName("success")
    val success: Boolean,

    @SerializedName("message")
    val message: String?,

    @SerializedName("data")
    val data: List<EmployeeData>?
)

// ✅ Used for POST /api/users — API returns data as a SINGLE OBJECT
data class CreateEmployeeResponse(
    @SerializedName("success")
    val success: Boolean,

    @SerializedName("message")
    val message: String?,

    @SerializedName("data")
    val data: EmployeeData?
)