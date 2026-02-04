package com.devbrian.osebo.domain.model

data class Account(
    val id: String,
    val businessName: String,
    val businessType: String,
    val registrationNumber: String,
    val taxId: String,
    val address: String,
    val status: String,
    val paymentMethod: String,
    val billingCycle: String,
    val nextBillingDate: String,
    val twoFactorEnabled: Boolean,
    val loginNotificationsEnabled: Boolean,
    val createdAt: String,
    val updatedAt: String
)