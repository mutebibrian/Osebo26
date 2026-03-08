package com.devbrian.osebo.data.remote.api


import com.google.gson.annotations.SerializedName

data class SubscriptionHistoryDto(
    @SerializedName("id")
    val id: String,

    @SerializedName("shop_id")
    val shopId: String? = null,

    @SerializedName("plan_id")
    val planId: String? = null,


    @SerializedName("max_items")
    val maxItems: Int? = null,

    @SerializedName("price")
    val price: Double? = null,

    @SerializedName("auto_renew")
    val autoRenew: Boolean? = null,

    @SerializedName("payment_method")
    val paymentMethod: String? = null,

    @SerializedName("start_date")
    val startDate: String? = null,

    @SerializedName("end_date")
    val endDate: String? = null,

    @SerializedName("days_left")
    val daysLeft: Int? = null,



    @SerializedName("plan_type")
    val planType: String? = null,

    @SerializedName("max_limit")
    val maxLimit: String? = null,


    @SerializedName("plan_name")
    val planName: String,

    @SerializedName("amount")
    val amount: Double,

    @SerializedName("currency")
    val currency: String,

    @SerializedName("status")
    val status: String,

    @SerializedName("date")
    val date: String
)

