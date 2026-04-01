package com.devbrian.osebo.data.remote.dto.request

import com.google.gson.annotations.SerializedName

// ✅ Sent to API when creating an employee (role is a String)
data class EmployeeRequest(
    @SerializedName("firstName")
    val firstName: String,

    @SerializedName("lastName")
    val lastName: String,

    @SerializedName("password")
    val password: String,

    @SerializedName("phone")
    val phone: String,

    @SerializedName("title")
    val title: String,

    @SerializedName("role")
    val role: String,

    @SerializedName("kin_name")
    val kinName: String? = null,

    @SerializedName("kin_phone")
    val kinPhone: String? = null,

    @SerializedName("residence")
    val residence: String? = null,

    @SerializedName("email")
    val email: String? = null
)

// ✅ Role object returned by API in responses
data class RoleDto(
    @SerializedName("id")
    val id: String,

    @SerializedName("name")
    val name: String,

    @SerializedName("description")
    val description: String?
)

// ✅ Employee data returned by API — role is an OBJECT not a String
data class EmployeeData(
    @SerializedName("id")
    val id: String,

    @SerializedName("firstName")
    val firstName: String,

    @SerializedName("lastName")
    val lastName: String,

    @SerializedName("email")
    val email: String?,

    @SerializedName("phone")
    val phone: String,

    @SerializedName("title")
    val title: String?,

    @SerializedName("isActive")
    val isActive: Boolean = true,

    @SerializedName("residence")
    val residence: String?,

    @SerializedName("kin_name")
    val kinName: String?,

    @SerializedName("kin_phone")
    val kinPhone: String?,

    // ✅ role is an object in API responses: {"id":"...","name":"staff","description":"..."}
    @SerializedName("role")
    val roleDto: RoleDto?,

    @SerializedName("shopId")
    val shopId: String? = null
) {
    // ✅ Convenience property to get role name as String (used throughout the app)
    val role: String get() = roleDto?.name ?: "staff"
}