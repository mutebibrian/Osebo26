package com.devbrian.osebo.data.remote.dto.request

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


@Serializable
data class EmployeeRequest(
    @SerialName("firstName")
    val firstName: String,

    @SerialName("lastName")
    val lastName: String,

    @SerialName("password")
    val password: String,

    @SerialName("phone")
    val phone: String,

    @SerialName("title")
    val title: String,

    @SerialName("role")
    val role: String,

    @SerialName("shopId")  // Add this field to associate employee with a shop
    val shopId: String,

    @SerialName("kin_name")
    val kinName: String? = null,

    @SerialName("kin_phone")
    val kinPhone: String? = null,

    @SerialName("residence")
    val residence: String? = null,

    @SerialName("email")
    val email: String? = null
)


@Serializable
data class RoleDto(
    @SerialName("id")
    val id: String,

    @SerialName("name")
    val name: String,

    @SerialName("description")
    val description: String?
)


@Serializable
data class EmployeeData(
    @SerialName("id")
    val id: String,

    @SerialName("firstName")
    val firstName: String,

    @SerialName("lastName")
    val lastName: String,

    @SerialName("email")
    val email: String?,

    @SerialName("phone")
    val phone: String,

    @SerialName("title")
    val title: String?,

    @SerialName("isActive")
    val isActive: Boolean = true,

    @SerialName("residence")
    val residence: String?,

    @SerialName("kin_name")
    val kinName: String?,

    @SerialName("kin_phone")
    val kinPhone: String?,

    @SerialName("role")
    val roleDto: RoleDto?,

    @SerialName("shopId")
    val shopId: String? = null
) {
    val role: String get() = roleDto?.name ?: "staff"
}