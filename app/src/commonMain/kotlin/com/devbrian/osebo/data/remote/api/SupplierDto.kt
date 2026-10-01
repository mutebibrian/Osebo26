package com.devbrian.osebo.data.remote.api


import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SupplierDto(
    @SerialName("id")
    val id: String,

    @SerialName(value = "shopId", alternate = ["shop_id"])
    val shopId: String? = null,

    @SerialName("name")
    val name: String,

    @SerialName(value = "contactPerson", alternate = ["contact_person"])
    val contactPerson: String? = null,

    @SerialName("email")
    val email: String? = null,

    @SerialName("phone")
    val phone: String? = null,

    @SerialName(value = "address", alternate = ["location"])
    val address: String? = null,

    @SerialName(value = "numberOfProducts", alternate = ["products", "productCount"])
    val numberOfProducts: Int? = null,

    @SerialName(value = "totalPurchases", alternate = ["total_purchases"])
    val totalPurchases: Double? = null,
)

