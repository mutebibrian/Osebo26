package com.devbrian.osebo.data.remote.dto.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AccountDto(
    @SerialName("id")
    val id: String,

    @SerialName("business_name")
    val businessName: String,

    @SerialName("business_type")
    val businessType: String,

    @SerialName("registration_number")
    val registrationNumber: String,

    @SerialName("tax_id")
    val taxId: String,

    @SerialName("address")
    val address: String,

    @SerialName("status")
    val status: String,

    @SerialName("payment_method")
    val paymentMethod: String,

    @SerialName("billing_cycle")
    val billingCycle: String,

    @SerialName("next_billing_date")
    val nextBillingDate: String,

    @SerialName("two_factor_enabled")
    val twoFactorEnabled: Boolean,

    @SerialName("login_notifications_enabled")
    val loginNotificationsEnabled: Boolean,

    @SerialName("created_at")
    val createdAt: String,

    @SerialName("updated_at")
    val updatedAt: String
)