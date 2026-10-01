package com.devbrian.osebo.data.remote.dto.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AuthData(
    // Old flow (direct token)
    @SerialName("access_token")
    val accessToken: String? = null,

    @SerialName("refresh_token")
    val refreshToken: String? = null,

    @SerialName("user")
    val user: UserDto? = null,

    // New 2FA flow response (Step 1 - after signin)
    @SerialName("userId")
    val userId: String? = null,

    @SerialName("message")
    val dataMessage: String? = null,

    // New 2FA flow response (Step 2 - after verify-2fa)
    @SerialName("preAuthToken")
    val preAuthToken: String? = null,

    @SerialName("accounts")
    val accounts: List<AccountInfo>? = null
)


