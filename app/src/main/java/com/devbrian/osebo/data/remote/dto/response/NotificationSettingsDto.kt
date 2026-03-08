package com.devbrian.osebo.data.remote.dto.response

data class NotificationSettingsDto(
    val emailNotifications: Boolean,
    val pushNotifications: Boolean,
    val smsNotifications: Boolean,
    val marketingEmails: Boolean,
    val securityAlerts: Boolean,
    val subscriptionAlerts: Boolean,
    val salesNotifications: Boolean
)

