package com.devbrian.osebo.data.remote.dto.request


import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UpdateShopRequest(
    @SerialName("name")
    val name: String? = null,

    @SerialName("address")
    val address: String? = null,

    @SerialName("phone")
    val phone: String? = null,

    @SerialName("email")
    val email: String? = null,

    @SerialName("status")
    val status: String? = null
)


