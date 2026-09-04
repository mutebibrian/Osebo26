package com.devbrian.osebo.models

import com.devbrian.osebo.data.models.Shop


fun Shop.toSubscriptionStatusUi(): SubscriptionStatusUi {
    return SubscriptionStatusUi.fromStatus(
        status = this.subscriptionStatus,
        type = this.subscriptionType,
        canActivate = this.needsSubscription
    )
}

fun Shop.getSubscriptionActionText(): String {
    return when (subscriptionStatus.lowercase()) {
        "active" -> "Manage"
        "expired" -> "Renew"
        "trial" -> "Upgrade"
        "pending" -> "Complete Payment"
        else -> "Activate"
    }
}


fun Subscription.toPaymentStatusUi(): PaymentStatusUi {
    return PaymentStatusUi.fromStatus(this.status)
}

fun Subscription.isWithinTrialPeriod(): Boolean {
    return isTrial && trialEndsAt != null && !trialEndsAt.isNullOrEmpty()
}


fun String?.formatSubscriptionDate(): String {
    if (this.isNullOrEmpty()) return "N/A"

    return try {
        
        this.substring(0, 10) 
    } catch (e: Exception) {
        this
    }
}


