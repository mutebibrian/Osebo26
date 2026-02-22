// SubscriptionDto.kt in data/remote/dto/response
package com.devbrian.osebo.data.remote.dto.response

import com.google.gson.annotations.SerializedName

data class SubscriptionDto(
    @SerializedName("id") val id: String? = null,
    @SerializedName("shop_id") val shop_id: String? = null,
    @SerializedName("package_type") val package_type: String? = null,
    @SerializedName("package_name") val package_name: String? = null,
    @SerializedName("amount") val amount: Double? = null,
    @SerializedName("currency") val currency: String? = null,
    @SerializedName("phone_number") val phone_number: String? = null,
    @SerializedName("status") val status: String? = null,
    @SerializedName("start_date") val start_date: String? = null,
    @SerializedName("end_date") val end_date: String? = null,
    @SerializedName("transaction_id") val transaction_id: String? = null,
    @SerializedName("payment_method") val payment_method: String? = null,
    @SerializedName("is_trial") val is_trial: Boolean? = null,
    @SerializedName("trial_ends_at") val trial_ends_at: String? = null,
    @SerializedName("auto_renew") val auto_renew: Boolean? = null,
    @SerializedName("created_at") val created_at: String? = null,
    @SerializedName("updated_at") val updated_at: String? = null,

    @SerializedName("plan_id") val plan_id: String? = null,
    @SerializedName("price") val price: Double? = null
)