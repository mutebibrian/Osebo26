package com.devbrian.osebo.data.remote.dto.request

import com.google.gson.annotations.SerializedName

data class CreateShopRequest(
    @SerializedName("name")
    val name: String,

    @SerializedName("address")
    val address: String,

    @SerializedName("shop_type_id")  // Changed from "business_type" to "shop_type_id"
    val shopTypeId: String,  // This should be a UUID from the shop types API

    @SerializedName("registration_number")
    val registrationNumber: String? = null,

    @SerializedName("tax_identification_number")
    val taxIdentificationNumber: String? = null,

    @SerializedName("description")
    val description: String?,

    // Removed phone and email if not needed by backend
    // @SerializedName("phone")
    // val phone: String? = null,
    //
    // @SerializedName("email")
    // val email: String? = null,
)