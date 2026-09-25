package com.devbrian.osebo.data.remote.dto.request

import com.google.gson.annotations.SerializedName

data class AddSupplierRequest(
    @SerializedName("name")
    val name: String,

    @SerializedName("contactPerson")
    val contactPerson: String? = null,

    @SerializedName("email")
    val email: String? = null,

    @SerializedName("phone")
    val phone: String,

    @SerializedName("address")
    val address: String? = null
)

data class UpdateSupplierRequest(
    @SerializedName("name")
    val name: String? = null,

    @SerializedName("contactPerson")
    val contactPerson: String? = null,

    @SerializedName("email")
    val email: String? = null,

    @SerializedName("phone")
    val phone: String? = null,

    @SerializedName("address")
    val address: String? = null,
)

