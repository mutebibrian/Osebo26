package com.devbrian.osebo.data.remote.dto.request

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CreateCustomerRequest(
    @SerialName("name") val name: String,
    @SerialName("phone") val phone: String,
    @SerialName("email") val email: String? = null,
    @SerialName("location") val location: String? = null,
    @SerialName("isDefault") val isDefault: Boolean = false
)

@Serializable
data class UpdateCustomerRequest(
    @SerialName("name") val name: String? = null,
    @SerialName("phone") val phone: String? = null,
    @SerialName("email") val email: String? = null,
    @SerialName("location") val location: String? = null,
    @SerialName("isDefault") val isDefault: Boolean? = null
)


