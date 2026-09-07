package com.devbrian.osebo.data.remote.api


import com.google.gson.annotations.SerializedName

data class SupplierDto(
    @SerializedName("id")
    val id: String,

    @SerializedName("shop_id")
    val shopId: String,

    @SerializedName("name")
    val name: String,

    @SerializedName("contact_person")
    val contactPerson: String?,

    @SerializedName("email")
    val email: String?,

    @SerializedName("phone")
    val phone: String
)


