package com.devbrian.osebo.data.remote.dto.request


import kotlinx.serialization.Serializable
@Serializable
data class NotificationSettingsRequest(
    val emailNotifications: Boolean? = null,
    val pushNotifications: Boolean? = null,
    val smsNotifications: Boolean? = null,
    val marketingEmails: Boolean? = null,
    val securityAlerts: Boolean? = null,
    val subscriptionAlerts: Boolean? = null,
    val salesNotifications: Boolean? = null
)


@Serializable
data class SystemHealthDto(
    val status: String, 
    val uptime: Long,
    val database: String,
    val cache: String,
    val services: Map<String, String>
)

@Serializable
data class SystemStatusDto(
    val maintenance: Boolean,
    val message: String?,
    val estimatedEndTime: String?
)
@Serializable
data class SystemVersionDto(
    val version: String,
    val buildNumber: String,
    val releaseDate: String,
    val changelog: String?
)


