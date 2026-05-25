package com.devbrian.osebo.data.remote.dto.response


import com.devbrian.osebo.models.SaleData
import com.google.gson.annotations.SerializedName

data class SaleApiListResponse(
    @SerializedName("success")
    val success: Boolean,

    @SerializedName("message")
    val message: String? = null,

    @SerializedName("data")
    val data: List<SaleApiData>? = null
)





