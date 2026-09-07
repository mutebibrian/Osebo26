package com.devbrian.osebo.data.models


import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class UserRole(
    @SerializedName("id")
    val id: String = "",

    @SerializedName("name")
    val name: String = "",

    @SerializedName("description")
    val description: String = "",

    @SerializedName("permissions")
    val permissions: List<String> = emptyList(),

    @SerializedName("shop_id")
    val shopId: String = "",

    @SerializedName("created_at")
    val createdAt: String = "",

    @SerializedName("updated_at")
    val updatedAt: String = ""
) : Parcelable


