package com.devbrian.osebo.models.contact

import com.google.gson.annotations.SerializedName

data class ContactInfo(
    @SerializedName("email")
    val email: String,

    @SerializedName("phone")
    val phone: String,

    @SerializedName("whatsapp")
    val whatsapp: String?,

    @SerializedName("address")
    val address: String?,
    @SerializedName("working_hours")
    val workingHours: String?,



)




