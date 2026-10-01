package com.devbrian.osebo.data.remote.dto.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PaymentCheckResponse(
    @SerialName("id")
    val id: String = "",

    @SerialName("is_paid")
    val isPaid: Boolean = false,

    @SerialName("shop")
    val shop: PaymentCheckShop? = null,

    @SerialName("packageSubscriptions")
    val packageSubscriptions: List<PaymentCheckPackageSubscription> = emptyList(),

    @SerialName("payment")
    val payment: PaymentCheckPayment? = null
) {

    val effectiveStatus: String
        get() = when {
            isPaid -> "active"
            payment?.status.equals("failed", ignoreCase = true) -> "failed"
            payment?.status.equals("cancelled", ignoreCase = true) -> "cancelled"
            else -> "pending"
        }

    val isActive: Boolean
        get() = isPaid

    val isFailed: Boolean
        get() = payment?.status.equals("failed", ignoreCase = true)

    val isCancelled: Boolean
        get() = payment?.status.equals("cancelled", ignoreCase = true)

    val isPending: Boolean
        get() = !isPaid && !isFailed && !isCancelled
}

@Serializable
data class PaymentCheckShop(
    @SerialName("id")
    val id: String = "",

    @SerialName("name")
    val name: String = "",

    @SerialName("address")
    val address: String? = null
)

@Serializable
data class PaymentCheckPackageSubscription(
    @SerialName("id")
    val id: String = "",

    @SerialName("unit_monthly_amount_snapshot")
    val unitMonthlyAmountSnapshot: String? = null,

    @SerialName("is_trial")
    val isTrial: Boolean = false,

    @SerialName("cancelled_at")
    val cancelledAt: String? = null,

    @SerialName("starts_at")
    val startsAt: String? = null,

    @SerialName("ends_at")
    val endsAt: String? = null,

    @SerialName("duration_days")
    val durationDays: Int = 0,

    @SerialName("package")
    val packageInfo: PackageDto? = null
)

@Serializable
data class PaymentCheckPayment(
    @SerialName("id")
    val id: String = "",

    @SerialName("method")
    val method: String? = null,

    @SerialName("reference")
    val reference: String? = null,

    @SerialName("amount")
    val amount: Double = 0.0,

    @SerialName("status")
    val status: String? = null,

    @SerialName("description")
    val description: String? = null,

    @SerialName("provider")
    val provider: String? = null,

    @SerialName("type")
    val type: String? = null
)