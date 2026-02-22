package com.devbrian.osebo.models


import com.google.gson.annotations.SerializedName

// Request to create a subscription
data class CreateSubscriptionRequest(
    @SerializedName("shop_id")
    val shopId: String,

    @SerializedName("package_type")
    val packageType: String, // BASIC, PRO, POPULAR

    @SerializedName("phone_number")
    val phoneNumber: String,

    @SerializedName("months")
    val months: Int = 1,

    @SerializedName("amount")
    val amount: Double? = null,

    @SerializedName("currency")
    val currency: String = "UGX"
)

// Request to authorize payment
data class AuthorizePaymentRequest(
    @SerializedName("transaction_id")
    val transactionId: String,

    @SerializedName("phone_number")
    val phoneNumber: String,

    @SerializedName("amount")
    val amount: Double,

    @SerializedName("currency")
    val currency: String = "UGX"
)

// Request to poll payment status
data class PollPaymentStatusRequest(
    @SerializedName("transaction_id")
    val transactionId: String
)


// Request to renew subscription
data class RenewSubscriptionRequest(
    @SerializedName("phone_number")
    val phoneNumber: String,

    @SerializedName("months")
    val months: Int = 1,

    @SerializedName("auto_renew")
    val autoRenew: Boolean = false
)

// Request to cancel subscription
data class CancelSubscriptionRequest(
    @SerializedName("reason")
    val reason: String? = null
)