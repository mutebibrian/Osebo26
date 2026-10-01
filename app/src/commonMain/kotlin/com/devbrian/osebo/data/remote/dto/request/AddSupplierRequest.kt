package com.devbrian.osebo.data.remote.dto.request

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AddSupplierRequest(
    @SerialName("name")
    val name: String,

    @SerialName("contactPerson")
    val contactPerson: String? = null,

    @SerialName("email")
    val email: String? = null,

    @SerialName("phone")
    val phone: String,

    @SerialName("address")
    val address: String? = null
)

@Serializable
data class UpdateSupplierRequest(
    @SerialName("name")
    val name: String? = null,

    @SerialName("contactPerson")
    val contactPerson: String? = null,

    @SerialName("email")
    val email: String? = null,

    @SerialName("phone")
    val phone: String? = null,

    @SerialName("address")
    val address: String? = null,
)

