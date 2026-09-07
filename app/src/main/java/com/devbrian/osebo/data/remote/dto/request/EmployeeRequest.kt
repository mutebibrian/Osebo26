package com.devbrian.osebo.data.remote.dto.request

import com.google.gson.annotations.SerializedName


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

    @SerializedName("shopId")  // Add this field to associate employee with a shop
    val shopId: String,

    @SerializedName("kin_name")
    val kinName: String? = null,

    @SerializedName("kin_phone")
    val kinPhone: String? = null,

    @SerializedName("residence")
    val residence: String? = null,

    @SerializedName("email")
    val email: String? = null
)


data class RoleDto(
    @SerializedName("id")
    val id: String,

    @SerializedName("name")
    val name: String,

    @SerializedName("description")
    val description: String?
)


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

    @SerializedName("role")
    val roleDto: RoleDto?,

    @SerializedName("shopId")
    val shopId: String? = null
) {
    val role: String get() = roleDto?.name ?: "staff"
}