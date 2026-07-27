package com.devbrian.osebo.data.remote.dto.response

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class PreAuthData(
    @SerializedName("preAuthToken")
    val preAuthToken: String,
    @SerializedName("accounts")
    val accounts: List<AccountInfo>
) : Parcelable

@Parcelize
data class AccountInfo(
    @SerializedName("accountId")
    val accountId: String,
    @SerializedName("ownerFirstName")
    val ownerFirstName: String,
    @SerializedName("ownerLastName")
    val ownerLastName: String,
    @SerializedName("isOwner")
    val isOwner: Boolean
) : Parcelable