package com.devbrian.osebo.data.remote.dto.response

import com.google.gson.annotations.SerializedName

data class UserDto(
    @SerializedName("id")
    val id: String,

    @SerializedName("firstName")
    val firstName: String?,

    @SerializedName("lastName")
    val lastName: String?,

    @SerializedName("email")
    val email: String?,

    @SerializedName("phone")
    val phone: String?,

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

    @SerializedName("role")
    val role: RoleDto? = null,

    
    @SerializedName("name")
    val name: String? = null,

    @SerializedName("email_verified_at")
    val emailVerifiedAt: String? = null,

    @SerializedName("created_at")
    val createdAtOld: String? = null,

    @SerializedName("updated_at")
    val updatedAtOld: String? = null,

    @SerializedName("profile_image_url")
    val profileImageUrl: String? = null
) {
    
    fun getFullName(): String {
        return buildString {
            firstName?.let { append(it) }
            lastName?.let {
                if (isNotEmpty()) append(" ")
                append(it)
            }
        }.trim().ifEmpty { name ?: "" }
    }

    
    fun extractFirstName(): String {
        return firstName ?: name?.substringBefore(" ") ?: ""
    }

    
    fun extractLastName(): String {
        return lastName ?: name?.substringAfterLast(" ") ?: ""
    }

    
    fun getDisplayName(): String {
        return name ?: getFullName()
    }
}

data class RoleDto(
    @SerializedName("id")
    val id: String,

    @SerializedName("name")
    val name: String,

    @SerializedName("description")
    val description: String?
)

