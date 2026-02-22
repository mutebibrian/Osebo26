package com.devbrian.osebo.models


enum class PackageType(val value: String) {
    BASIC("BASIC"),
    PRO("PRO"),
    POPULAR("POPULAR");

    companion object {
        fun fromString(value: String): PackageType {
            return when (value.uppercase()) {
                "BASIC" -> BASIC
                "PRO" -> PRO
                "POPULAR" -> POPULAR
                else -> BASIC
            }
        }
    }
}

enum class SubscriptionStatus(val value: String) {
    ACTIVE("ACTIVE"),
    EXPIRED("EXPIRED"),
    PENDING("PENDING"),
    CANCELLED("CANCELLED"),
    TRIAL("TRIAL"),
    INACTIVE("INACTIVE");

    companion object {
        fun fromString(value: String): SubscriptionStatus {
            return when (value.uppercase()) {
                "ACTIVE" -> ACTIVE
                "EXPIRED" -> EXPIRED
                "PENDING" -> PENDING
                "CANCELLED" -> CANCELLED
                "TRIAL" -> TRIAL
                else -> INACTIVE
            }
        }
    }
}

enum class PaymentStatus(val value: String) {
    PENDING("PENDING"),
    COMPLETED("COMPLETED"),
    FAILED("FAILED"),
    CANCELLED("CANCELLED");

    companion object {
        fun fromString(value: String): PaymentStatus {
            return when (value.uppercase()) {
                "PENDING" -> PENDING
                "COMPLETED" -> COMPLETED
                "FAILED" -> FAILED
                "CANCELLED" -> CANCELLED
                else -> PENDING
            }
        }
    }
}

enum class PaymentMethod(val value: String, val displayName: String) {
    MOBILE_MONEY("mobile_money", "Mobile Money"),
    CREDIT_CARD("credit_card", "Credit Card"),
    BANK_TRANSFER("bank_transfer", "Bank Transfer");

    companion object {
        fun fromString(value: String): PaymentMethod {
            return when (value.lowercase()) {
                "mobile_money" -> MOBILE_MONEY
                "credit_card" -> CREDIT_CARD
                "bank_transfer" -> BANK_TRANSFER
                else -> MOBILE_MONEY
            }
        }
    }
}