package com.devbrian.osebo.data.remote.dto.response

data class ShopResponse(
    val id: String,
    val name: String,
    val address: String,
    val shopType: String,
    val registrationNumber: String?,
    val taxIdentificationNumber: String?,
    val description: String?,
    val createdAt: String,
    val updatedAt: String
)

