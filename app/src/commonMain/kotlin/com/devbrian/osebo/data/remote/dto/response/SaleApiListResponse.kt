package com.devbrian.osebo.data.remote.dto.response


import com.devbrian.osebo.models.SaleData
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SaleApiListResponse(
    @SerialName("success")
    val success: Boolean,

    @SerialName("message")
    val message: String? = null,

    @SerialName("data")
    val data: List<SaleApiData>? = null
)





