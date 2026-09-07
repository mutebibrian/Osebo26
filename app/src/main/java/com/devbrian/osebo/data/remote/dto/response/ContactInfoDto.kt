package com.devbrian.osebo.data.remote.dto.response

import com.google.gson.annotations.SerializedName

data class ContactInfoDto(
    @SerializedName("phone")
    val phone: String,

    @SerializedName("email")
    val email: String,

    @SerializedName("address")
    val address: String,

    @SerializedName("working_hours")
    val workingHours: String,

    @SerializedName("support_hours")
    val supportHours: String,

    @SerializedName("emergency_contact")
    val emergencyContact: String?,

    @SerializedName("social_media")
    val socialMedia: Map<String, String>
)


