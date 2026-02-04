package com.devbrian.osebo.models

data class Session(
    val id: String,
    val device: String,
    val browser: String,
    val ipAddress: String,
    val location: String,
    val lastActive: String,
    val isCurrent: Boolean = false
)