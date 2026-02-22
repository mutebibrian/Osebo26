package com.devbrian.osebo.models


// Extension functions for Shop model
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

// Extension functions for Subscription model
fun Subscription.toPaymentStatusUi(): PaymentStatusUi {
    return PaymentStatusUi.fromStatus(this.status)
}

fun Subscription.isWithinTrialPeriod(): Boolean {
    return isTrial && trialEndsAt != null && !trialEndsAt.isNullOrEmpty()
}

// Extension for date formatting
fun String?.formatSubscriptionDate(): String {
    if (this.isNullOrEmpty()) return "N/A"

    return try {
        // Simple formatting - you can use SimpleDateFormat or LocalDateTime for better formatting
        this.substring(0, 10) // Get YYYY-MM-DD part
    } catch (e: Exception) {
        this
    }
}