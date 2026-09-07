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

    // ✅ role is now a String (as returned by the backend in login responses)
    @SerializedName("role")
    val role: String? = null,

    // Alternative field names that might come from different API endpoints
    @SerializedName("name")
    val name: String? = null,

    @SerializedName("email_verified_at")
    val emailVerifiedAt: String? = null,

    @SerializedName("created_at")
    val createdAtOld: String? = null,

    @SerializedName("updated_at")
    val updatedAtOld: String? = null,

    @SerializedName("profile_image_url")
    val profileImageUrl: String? = null,

    @SerializedName("is_email_verified")
    val isEmailVerified: Boolean? = null,

    @SerializedName("email_verified")
    val emailVerified: Boolean? = null,

    @SerializedName("status")
    val status: String? = null,

    @SerializedName("user_type")
    val userType: String? = null,

    @SerializedName("permissions")
    val permissions: List<String>? = null
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

    fun getPhoneNumber(): String {
        return phone ?: ""
    }

    fun isEmailVerifiedCompat(): Boolean {
        return isVerified == true || isEmailVerified == true || emailVerified == true
    }

    fun isActiveCompat(): Boolean {
        return isActive == true && status != "inactive" && status != "disabled"
    }

    fun getRoleName(): String {
        return role ?: userType ?: "staff"
    }
}