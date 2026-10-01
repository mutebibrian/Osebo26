package com.devbrian.osebo.data.remote.dto.response


import kotlinx.serialization.Serializable
@Serializable
data class NotificationSettingsDto(
    val emailNotifications: Boolean,
    val pushNotifications: Boolean,
    val smsNotifications: Boolean,
    val marketingEmails: Boolean,
    val securityAlerts: Boolean,
    val subscriptionAlerts: Boolean,
    val salesNotifications: Boolean
)


