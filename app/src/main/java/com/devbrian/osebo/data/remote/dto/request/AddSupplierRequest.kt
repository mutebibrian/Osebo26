package com.devbrian.osebo.data.remote.dto.request

import com.google.gson.annotations.SerializedName

data class AddSupplierRequest(
    @SerializedName("shop_id")
    val shopId: String,

    @SerializedName("name")
    val name: String,

    @SerializedName("contact_person")
    val contactPerson: String? = null,

    @SerializedName("email")
    val email: String? = null,

    @SerializedName("phone")
    val phone: String,

    @SerializedName("address")
    val address: String? = null
)