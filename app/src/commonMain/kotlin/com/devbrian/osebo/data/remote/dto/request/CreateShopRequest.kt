package com.devbrian.osebo.data.remote.dto.request

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CreateShopRequest(
    @SerialName("name")
    val name: String,

    @SerialName("address")
    val address: String,

    @SerialName("shop_type_id")
    val shopTypeId: String,

    @SerialName("registration_number")
    val registrationNumber: String? = null,

    @SerialName("tax_identification_number")
    val taxIdentificationNumber: String? = null,

    @SerialName("description")
    val description: String? = null,

    @SerialName("accountId")
    val accountId: String
)