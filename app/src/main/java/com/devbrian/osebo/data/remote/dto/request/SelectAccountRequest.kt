package com.devbrian.osebo.data.remote.dto.request

// SelectAccountRequest.kt
data class SelectAccountRequest(
    val preAuthToken: String,
    val accountId: String
)

// SwitchAccountRequest.kt
data class SwitchAccountRequest(
    val accountId: String
)

// AccountsByPhoneRequest.kt (optional)
data class AccountsByPhoneRequest(
    val username: String
)