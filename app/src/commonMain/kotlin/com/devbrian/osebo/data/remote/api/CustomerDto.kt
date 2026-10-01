package com.devbrian.osebo.data.remote.api


import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CustomerDto(
    @SerialName("id")
    val id: String,

    @SerialName("shop_id")
    val shopId: String,

    @SerialName("first_name")
    val firstName: String,

    @SerialName("last_name")
    val lastName: String,

    @SerialName("email")
    val email: String?,

    @SerialName("phone")
    val phone: String,

    @SerialName("total_purchases")
    val totalPurchases: Double
)


