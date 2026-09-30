package com.devbrian.osebo.data.remote.api


import com.google.gson.annotations.SerializedName

data class SupplierDto(
    @SerializedName("id")
    val id: String,

    @SerializedName(value = "shopId", alternate = ["shop_id"])
    val shopId: String? = null,

    @SerializedName("name")
    val name: String,

    @SerializedName(value = "contactPerson", alternate = ["contact_person"])
    val contactPerson: String? = null,

    @SerializedName("email")
    val email: String? = null,

    @SerializedName("phone")
    val phone: String? = null,

    @SerializedName(value = "address", alternate = ["location"])
    val address: String? = null,

    @SerializedName(value = "numberOfProducts", alternate = ["products", "productCount"])
    val numberOfProducts: Int? = null,

    @SerializedName(value = "totalPurchases", alternate = ["total_purchases"])
    val totalPurchases: Double? = null,
)

