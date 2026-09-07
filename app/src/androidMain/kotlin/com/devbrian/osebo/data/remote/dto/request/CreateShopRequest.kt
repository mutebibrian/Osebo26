package com.devbrian.osebo.data.remote.dto.request

import com.google.gson.annotations.SerializedName

data class CreateShopRequest(
    @SerializedName("name")
    val name: String,

    @SerializedName("address")
    val address: String,

    @SerializedName("shop_type_id")
    val shopTypeId: String,

    @SerializedName("registration_number")
    val registrationNumber: String? = null,

    @SerializedName("tax_identification_number")
    val taxIdentificationNumber: String? = null,

    @SerializedName("description")
    val description: String? = null,

    @SerializedName("accountId")
    val accountId: String
)