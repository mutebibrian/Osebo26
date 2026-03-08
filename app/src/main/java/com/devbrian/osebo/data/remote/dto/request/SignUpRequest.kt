package com.devbrian.osebo.data.remote.dto.request

data class SignUpRequest(
    val firstName: String,
    val lastName: String,
    val email: String,
    val password: String,
    val phone: String,
    val title: String = "Shop Owner"
)

