package com.devbrian.osebo.models

object SubscriptionConstants {

    // ==================== API ENDPOINTS ====================
    // Based on your API response: https://dev-api.osebo.ai/api/package
    const val ENDPOINT_PACKAGES = "api/package"  // ✅ Correct endpoint from your API
    const val ENDPOINT_CREATE = "subscription"
    const val ENDPOINT_AUTHORIZE_PAYMENT = "subscription/AuthorizePayment"
    const val ENDPOINT_CHECK_STATUS = "subscription/CheckShopSubscription"
    const val ENDPOINT_POLL_PAYMENT = "subscription/PollPaymentStatus"
    const val ENDPOINT_FIND_BY_SHOP = "subscription/FindByShoplid"
    const val ENDPOINT_ACTIVATE_TRIAL = "subscription/activate-trial/{shopId}"
    const val ENDPOINT_RENEW = "subscription/renew/{subscriptionId}"
    const val ENDPOINT_CANCEL = "subscription/cancel/{subscriptionId}"
    const val ENDPOINT_PAYMENT_HISTORY = "subscription/{subscriptionId}/payments"

    // ==================== PAYMENT POLLING ====================
    const val POLLING_INTERVAL = 5000L // 5 seconds
    const val MAX_POLLING_ATTEMPTS = 60 // 5 minutes total
    const val POLLING_TIMEOUT = 300000L // 5 minutes in milliseconds

    // ==================== PACKAGE TIERS (from API) ====================
    const val TIER_BASIC = "basic"
    const val TIER_PRO = "pro"
    const val TIER_CUSTOM = "custom"

    // ==================== CURRENCIES ====================
    const val CURRENCY_UGX = "UGX"

    // ==================== DEFAULT PACKAGES (Fallback if API fails) ====================
    // These match your API response structure exactly
    val DEFAULT_PACKAGES = listOf(
        SubscriptionPackage(
            id = "2929b8e2-e6b2-4c3f-9c4a-014894be034f", // Use actual UUID from API
            name = "Basic",
            tier = TIER_BASIC,
            type = "monthly",
            description = "Basic plan, billed monthly.",
            unitMonthlyAmount = "20000.00",
            features = listOf(
                Feature(
                    name = "Inventory",
                    included = true,
                    description = "Basic inventory management"
                ),
                Feature(
                    name = "Reports",
                    included = true,
                    description = "Basic sales reports"
                )
            ),
            isActive = true
        ),
        SubscriptionPackage(
            id = "9dff53fc-3071-47c1-8429-c80d7448e52a", // Use actual UUID from API
            name = "Pro",
            tier = TIER_PRO,
            type = "monthly",
            description = "Pro plan, billed monthly.",
            unitMonthlyAmount = "50000.00",
            features = listOf(
                Feature(
                    name = "Inventory",
                    included = true,
                    description = "Advanced inventory management"
                ),
                Feature(
                    name = "Reports",
                    included = true,
                    description = "Advanced sales reports"
                ),
                Feature(
                    name = "Support",
                    included = true,
                    description = "Priority support"
                )
            ),
            isActive = true
        ),
        SubscriptionPackage(
            id = "1ad6f0b7-e84a-49ec-ae0d-84019f8a9888", // Use actual UUID from API
            name = "Talk to us",
            tier = TIER_CUSTOM,
            type = "monthly",
            description = "Custom plan. Contact sales for pricing.",
            unitMonthlyAmount = "0.00",
            features = listOf(
                Feature(
                    name = "Inventory",
                    included = true,
                    description = "Unlimited inventory"
                ),
                Feature(
                    name = "Reports",
                    included = true,
                    description = "All sales and analytics"
                ),
                Feature(
                    name = "Support",
                    included = true,
                    description = "24/7 support"
                ),
                Feature(
                    name = "API",
                    included = true,
                    description = "API access"
                )
            ),
            isActive = true
        )
    )

    // ==================== STATUS MESSAGES ====================
    const val MSG_PAYMENT_PENDING = "Please follow the prompts sent to your mobile phone to complete the payment."
    const val MSG_OPERATOR_CHARGES = "NOTE: Local operator charges may apply!"
    const val MSG_TRIAL_ACTIVATED = "Free trial activated successfully!"
    const val MSG_SUBSCRIPTION_ACTIVE = "Subscription activated successfully!"
    const val MSG_PAYMENT_COMPLETED = "Payment completed successfully!"
    const val MSG_PAYMENT_FAILED = "Payment failed. Please try again."
    const val MSG_PAYMENT_TIMEOUT = "Payment timeout. Please check your mobile money."
    const val MSG_NO_ACTIVE_SUBSCRIPTION = "No active subscription found"
    const val MSG_SUBSCRIPTION_CANCELLED = "Subscription cancelled successfully"
    const val MSG_SUBSCRIPTION_RENEWED = "Subscription renewed successfully"

    // ==================== SUBSCRIPTION STATUS ====================
    const val STATUS_ACTIVE = "ACTIVE"
    const val STATUS_PENDING = "PENDING"
    const val STATUS_EXPIRED = "EXPIRED"
    const val STATUS_CANCELLED = "CANCELLED"
    const val STATUS_TRIAL = "TRIAL"
    const val STATUS_INACTIVE = "INACTIVE"

    // ==================== PAYMENT STATUS ====================
    const val PAYMENT_COMPLETED = "completed"
    const val PAYMENT_PENDING = "pending"
    const val PAYMENT_FAILED = "failed"
    const val PAYMENT_CANCELLED = "cancelled"
    const val PAYMENT_SUCCESS = "success"

    // ==================== PAYMENT METHODS ====================
    const val METHOD_MOBILE_MONEY = "mobile_money"
    const val METHOD_CREDIT_CARD = "credit_card"
    const val METHOD_BANK_TRANSFER = "bank_transfer"

    // ==================== BILLING CYCLES ====================
    const val BILLING_MONTHLY = "monthly"
    const val BILLING_QUARTERLY = "quarterly"
    const val BILLING_BIANNUAL = "biannual"
    const val BILLING_ANNUAL = "annual"

    // ==================== FEATURE NAMES ====================
    const val FEATURE_INVENTORY = "Inventory"
    const val FEATURE_REPORTS = "Reports"
    const val FEATURE_SUPPORT = "Support"
    const val FEATURE_ANALYTICS = "Analytics"
    const val FEATURE_API = "API"
}