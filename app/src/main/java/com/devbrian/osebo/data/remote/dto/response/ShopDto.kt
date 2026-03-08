package com.devbrian.osebo.data.remote.dto.response

import com.google.gson.annotations.SerializedName

data class ShopDto(
    @SerializedName("id")
    val id: String,

    @SerializedName("name")
    val name: String,

    @SerializedName("address")
    val address: String?,

    @SerializedName("phone")
    val phone: String?,

    @SerializedName("email")
    val email: String?,

    @SerializedName("business_type")
    val businessType: String?,

    @SerializedName("shop_type")
    val shopType: String?,

    @SerializedName("description")
    val description: String?,

    @SerializedName("registration_number")
    val registrationNumber: String?,

    @SerializedName("tax_identification_number")
    val taxIdentificationNumber: String?,

    @SerializedName("owner_id")
    val ownerId: String?,

    @SerializedName("status")
    val status: String,

    @SerializedName("is_active")
    val isActive: Boolean = true,

    @SerializedName("created_at")
    val createdAt: String,

    @SerializedName("updated_at")
    val updatedAt: String,

    @SerializedName("subscription")
    val subscription: ShopSubscriptionDto? = null, 
)




