package com.devbrian.osebo.data.remote.dto.response


import com.devbrian.osebo.models.Subscription
import com.google.gson.annotations.SerializedName

data class ShopSubscriptionStatusResponse(
    @SerializedName("success")
    val success: Boolean,

    @SerializedName("message")
    val message: String?,

    @SerializedName("status")
    val status: String,

    @SerializedName("type")
    val type: String?, 

    @SerializedName("expiry_date")
    val expiryDate: String?,

    @SerializedName("is_active")
    val isActive: Boolean,

    @SerializedName("days_remaining")
    val daysRemaining: Int?,

    @SerializedName("can_activate")
    val canActivate: Boolean,

    @SerializedName("shop_id")
    val shopId: String?,

    @SerializedName("shop_name")
    val shopName: String?,

    @SerializedName("subscription_id")
    val subscriptionId: String?,

    @SerializedName("subscription")
    val subscription: Subscription?
)

