package com.devbrian.osebo.data.remote.dto.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ContactInfoDto(
    @SerialName("phone")
    val phone: String,

    @SerialName("email")
    val email: String,

    @SerialName("address")
    val address: String,

    @SerialName("working_hours")
    val workingHours: String,

    @SerialName("support_hours")
    val supportHours: String,

    @SerialName("emergency_contact")
    val emergencyContact: String?,

    @SerialName("social_media")
    val socialMedia: Map<String, String>
)


