package com.devbrian.osebo.data.remote.dto.request

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AddCustomerRequest(
    @SerialName("shop_id")
    val shopId: String,

    @SerialName("first_name")
    val firstName: String,

    @SerialName("last_name")
    val lastName: String,

    @SerialName("email")
    val email: String? = null,

    @SerialName("phone")
    val phone: String,

    @SerialName("address")
    val address: String? = null
)


