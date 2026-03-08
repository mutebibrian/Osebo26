package com.devbrian.osebo.data.remote.dto.response

import com.google.gson.annotations.SerializedName

data class AccountDto(
    @SerializedName("id")
    val id: String,

    @SerializedName("business_name")
    val businessName: String,

    @SerializedName("business_type")
    val businessType: String,

    @SerializedName("registration_number")
    val registrationNumber: String,

    @SerializedName("tax_id")
    val taxId: String,

    @SerializedName("address")
    val address: String,

    @SerializedName("status")
    val status: String,

    @SerializedName("payment_method")
    val paymentMethod: String,

    @SerializedName("billing_cycle")
    val billingCycle: String,

    @SerializedName("next_billing_date")
    val nextBillingDate: String,

    @SerializedName("two_factor_enabled")
    val twoFactorEnabled: Boolean,

    @SerializedName("login_notifications_enabled")
    val loginNotificationsEnabled: Boolean,

    @SerializedName("created_at")
    val createdAt: String,

    @SerializedName("updated_at")
    val updatedAt: String
)

