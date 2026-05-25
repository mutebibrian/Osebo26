package com.devbrian.osebo.data.remote.dto.response


import com.google.gson.annotations.SerializedName

data class PreAuthData(
    @SerializedName("preAuthToken")
    val preAuthToken: String,
    @SerializedName("accounts")
    val accounts: List<AccountInfo>
)

