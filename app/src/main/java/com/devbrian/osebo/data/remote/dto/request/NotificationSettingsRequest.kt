package com.devbrian.osebo.data.remote.dto.request

data class NotificationSettingsRequest(
    val emailNotifications: Boolean? = null,
    val pushNotifications: Boolean? = null,
    val smsNotifications: Boolean? = null,
    val marketingEmails: Boolean? = null,
    val securityAlerts: Boolean? = null,
    val subscriptionAlerts: Boolean? = null,
    val salesNotifications: Boolean? = null
)


data class SystemHealthDto(
    val status: String, 
    val uptime: Long,
    val database: String,
    val cache: String,
    val services: Map<String, String>
)

data class SystemStatusDto(
    val maintenance: Boolean,
    val message: String?,
    val estimatedEndTime: String?
)
data class SystemVersionDto(
    val version: String,
    val buildNumber: String,
    val releaseDate: String,
    val changelog: String?
)

