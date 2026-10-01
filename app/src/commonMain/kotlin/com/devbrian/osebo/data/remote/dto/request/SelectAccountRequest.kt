package com.devbrian.osebo.data.remote.dto.request


import kotlinx.serialization.Serializable
// SelectAccountRequest.kt
@Serializable
data class SelectAccountRequest(
    val preAuthToken: String,
    val accountId: String
)

// SwitchAccountRequest.kt
@Serializable
data class SwitchAccountRequest(
    val accountId: String
)

// AccountsByPhoneRequest.kt (optional)
@Serializable
data class AccountsByPhoneRequest(
    val username: String
)