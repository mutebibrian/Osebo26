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

    // For backward compatibility, you can keep these but make them nullable
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
    // Helper function to get full name from firstName and lastName
    fun getFullName(): String {
        return buildString {
            firstName?.let { append(it) }
            lastName?.let {
                if (isNotEmpty()) append(" ")
                append(it)
            }
        }.trim().ifEmpty { name ?: "" }
    }

    // Renamed to avoid conflict with firstName property
    fun extractFirstName(): String {
        return firstName ?: name?.substringBefore(" ") ?: ""
    }

    // Renamed to avoid conflict with lastName property
    fun extractLastName(): String {
        return lastName ?: name?.substringAfterLast(" ") ?: ""
    }

    // Renamed to avoid conflict with name property
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