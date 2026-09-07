package com.devbrian.osebo.data.remote.dto.response

import com.google.gson.annotations.SerializedName

data class AuthData(
    // Old flow (direct token)
    @SerializedName("access_token")
    val accessToken: String? = null,

    @SerializedName("refresh_token")
    val refreshToken: String? = null,

    @SerializedName("user")
    val user: UserDto? = null,

    // New 2FA flow response (Step 1 - after signin)
    @SerializedName("userId")
    val userId: String? = null,

    @SerializedName("message")
    val dataMessage: String? = null,

    // New 2FA flow response (Step 2 - after verify-2fa)
    @SerializedName("preAuthToken")
    val preAuthToken: String? = null,

    @SerializedName("accounts")
    val accounts: List<AccountInfo>? = null
)


