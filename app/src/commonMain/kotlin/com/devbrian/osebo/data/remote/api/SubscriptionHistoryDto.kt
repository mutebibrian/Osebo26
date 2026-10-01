package com.devbrian.osebo.data.remote.api


import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SubscriptionHistoryDto(
    @SerialName("id")
    val id: String,

    @SerialName("shop_id")
    val shopId: String? = null,

    @SerialName("plan_id")
    val planId: String? = null,


    @SerialName("max_items")
    val maxItems: Int? = null,

    @SerialName("price")
    val price: Double? = null,

    @SerialName("auto_renew")
    val autoRenew: Boolean? = null,

    @SerialName("payment_method")
    val paymentMethod: String? = null,

    @SerialName("start_date")
    val startDate: String? = null,

    @SerialName("end_date")
    val endDate: String? = null,

    @SerialName("days_left")
    val daysLeft: Int? = null,



    @SerialName("plan_type")
    val planType: String? = null,

    @SerialName("max_limit")
    val maxLimit: String? = null,


    @SerialName("plan_name")
    val planName: String,

    @SerialName("amount")
    val amount: Double,

    @SerialName("currency")
    val currency: String,

    @SerialName("status")
    val status: String,

    @SerialName("date")
    val date: String
)


