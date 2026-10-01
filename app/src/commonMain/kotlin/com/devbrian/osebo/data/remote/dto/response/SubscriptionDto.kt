
package com.devbrian.osebo.data.remote.dto.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SubscriptionDto(
    @SerialName("id") val id: String? = null,
    @SerialName("shop_id") val shop_id: String? = null,
    @SerialName("package_type") val package_type: String? = null,
    @SerialName("package_name") val package_name: String? = null,
    @SerialName("amount") val amount: Double? = null,
    @SerialName("currency") val currency: String? = null,
    @SerialName("phone_number") val phone_number: String? = null,
    @SerialName("status") val status: String? = null,
    @SerialName("start_date") val start_date: String? = null,
    @SerialName("end_date") val end_date: String? = null,
    @SerialName("transaction_id") val transaction_id: String? = null,
    @SerialName("payment_method") val payment_method: String? = null,
    @SerialName("is_trial") val is_trial: Boolean? = null,
    @SerialName("trial_ends_at") val trial_ends_at: String? = null,
    @SerialName("auto_renew") val auto_renew: Boolean? = null,
    @SerialName("created_at") val created_at: String? = null,
    @SerialName("updated_at") val updated_at: String? = null,

    @SerialName("plan_id") val plan_id: String? = null,
    @SerialName("price") val price: Double? = null
)


