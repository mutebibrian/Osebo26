package com.devbrian.osebo.models

import com.google.gson.annotations.SerializedName

data class User(
    @SerializedName("id")
    val id: String,

    @SerializedName("email")
    val email: String?,

    @SerializedName("firstName")
    val firstName: String?,

    @SerializedName("lastName")
    val lastName: String?,

    @SerializedName("name")
    val name: String? = null,

    @SerializedName("phone")
    val phone: String? = null,

    @SerializedName("title")
    val title: String? = null,

    @SerializedName("isActive")
    val isActive: Boolean? = true,

    @SerializedName("isVerified")
    val isVerified: Boolean? = null,

    @SerializedName("photo")
    val photo: String? = null,

    @SerializedName("createdAt")
    val createdAt: String?,

    @SerializedName("updatedAt")
    val updatedAt: String?,

    @SerializedName("shop_id")
    val shopId: String? = null,

    @SerializedName("role")
    val role: Role? = null,

    
    @SerializedName("created_at")
    val createdAtOld: String? = null,

    @SerializedName("updated_at")
    val updatedAtOld: String? = null,

    @SerializedName("email_verified_at")
    val emailVerifiedAt: String? = null
) {
    
    fun getFullName(): String {
        return if (firstName != null && lastName != null) {
            "$firstName $lastName"
        } else {
            name ?: email?.substringBefore("@") ?: "User"
        }
    }

    
    fun getDisplayName(): String {
        return when {
            firstName != null && lastName != null -> "$firstName $lastName"
            name != null && name.isNotEmpty() -> name
            else -> email?.substringBefore("@") ?: "User"
        }
    }



    
    fun hasShop(): Boolean {
        return shopId != null && shopId.isNotEmpty()
    }
}

data class Role(
    @SerializedName("id")
    val id: String,

    @SerializedName("name")
    val name: String,

    @SerializedName("description")
    val description: String?
)

