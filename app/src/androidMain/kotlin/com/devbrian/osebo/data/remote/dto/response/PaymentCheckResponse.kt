package com.devbrian.osebo.data.remote.dto.response

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize
import kotlinx.parcelize.RawValue

@Parcelize
data class PaymentCheckResponse(
    @SerializedName("id")
    val id: String = "",

    @SerializedName("is_paid")
    val isPaid: Boolean = false,

    @SerializedName("shop")
    val shop: PaymentCheckShop? = null,

    @SerializedName("packageSubscriptions")
    val packageSubscriptions: List<PaymentCheckPackageSubscription> = emptyList(),

    @SerializedName("payment")
    val payment: PaymentCheckPayment? = null
) : Parcelable {

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

@Parcelize
data class PaymentCheckShop(
    @SerializedName("id")
    val id: String = "",

    @SerializedName("name")
    val name: String = "",

    @SerializedName("address")
    val address: String? = null
) : Parcelable

@Parcelize
data class PaymentCheckPackageSubscription(
    @SerializedName("id")
    val id: String = "",

    @SerializedName("unit_monthly_amount_snapshot")
    val unitMonthlyAmountSnapshot: String? = null,

    @SerializedName("is_trial")
    val isTrial: Boolean = false,

    @SerializedName("cancelled_at")
    val cancelledAt: String? = null,

    @SerializedName("starts_at")
    val startsAt: String? = null,

    @SerializedName("ends_at")
    val endsAt: String? = null,

    @SerializedName("duration_days")
    val durationDays: Int = 0,

    @SerializedName("package")
    val packageInfo: @RawValue PackageDto? = null
) : Parcelable

@Parcelize
data class PaymentCheckPayment(
    @SerializedName("id")
    val id: String = "",

    @SerializedName("method")
    val method: String? = null,

    @SerializedName("reference")
    val reference: String? = null,

    @SerializedName("amount")
    val amount: Double = 0.0,

    @SerializedName("status")
    val status: String? = null,

    @SerializedName("description")
    val description: String? = null,

    @SerializedName("provider")
    val provider: String? = null,

    @SerializedName("type")
    val type: String? = null
) : Parcelable