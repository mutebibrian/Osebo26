// SubscriptionDto.kt in data/remote/dto/response
package com.devbrian.osebo.data.remote.dto.response

import com.google.gson.annotations.SerializedName

data class SubscriptionDto(
    @SerializedName("id")
    val id: String? = null,

    @SerializedName("shop_id")  // This should exist
    val shopId: String? = null,

    @SerializedName("plan_id")
    val planId: String? = null,

    @SerializedName("plan_name")
    val planName: String? = null,

    @SerializedName("plan_type")
    val planType: String? = null,

    @SerializedName("max_limit")
    val maxLimit: String? = null,

    @SerializedName("max_items")
    val maxItems: Int? = null,

    @SerializedName("price")
    val price: Double? = null,

    @SerializedName("currency")
    val currency: String? = null,

    @SerializedName("status")
    val status: String? = null,

    @SerializedName("start_date")
    val startDate: String? = null,

    @SerializedName("end_date")
    val endDate: String? = null,

    @SerializedName("days_left")
    val daysLeft: Int? = null,

    @SerializedName("auto_renew")
    val autoRenew: Boolean? = null,

    @SerializedName("payment_method")
    val paymentMethod: String? = null
)