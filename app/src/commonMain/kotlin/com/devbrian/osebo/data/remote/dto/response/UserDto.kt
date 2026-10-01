package com.devbrian.osebo.data.remote.dto.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UserDto(
    @SerialName("id")
    val id: String,

    @SerialName("firstName")
    val firstName: String?,

    @SerialName("lastName")
    val lastName: String?,

    @SerialName("email")
    val email: String?,

    @SerialName("phone")
    val phone: String?,

    @SerialName("title")
    val title: String? = null,

    @SerialName("isActive")
    val isActive: Boolean? = true,

    @SerialName("isVerified")
    val isVerified: Boolean? = null,

    @SerialName("photo")
    val photo: String? = null,

    @SerialName("createdAt")
    val createdAt: String?,

    @SerialName("updatedAt")
    val updatedAt: String?,

    // ✅ role is now a String (as returned by the backend in login responses)
    @SerialName("role")
    val role: String? = null,

    // Alternative field names that might come from different API endpoints
    @SerialName("name")
    val name: String? = null,

    @SerialName("email_verified_at")
    val emailVerifiedAt: String? = null,

    @SerialName("created_at")
    val createdAtOld: String? = null,

    @SerialName("updated_at")
    val updatedAtOld: String? = null,

    @SerialName("profile_image_url")
    val profileImageUrl: String? = null,

    @SerialName("is_email_verified")
    val isEmailVerified: Boolean? = null,

    @SerialName("email_verified")
    val emailVerified: Boolean? = null,

    @SerialName("status")
    val status: String? = null,

    @SerialName("user_type")
    val userType: String? = null,

    @SerialName("permissions")
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