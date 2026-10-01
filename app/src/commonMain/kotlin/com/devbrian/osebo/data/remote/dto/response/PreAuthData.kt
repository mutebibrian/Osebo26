package com.devbrian.osebo.data.remote.dto.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PreAuthData(
    @SerialName("preAuthToken")
    val preAuthToken: String,
    @SerialName("accounts")
    val accounts: List<AccountInfo>
)

@Serializable
data class AccountInfo(
    @SerialName("accountId")
    val accountId: String,
    @SerialName("ownerFirstName")
    val ownerFirstName: String,
    @SerialName("ownerLastName")
    val ownerLastName: String,
    @SerialName("isOwner")
    val isOwner: Boolean
)