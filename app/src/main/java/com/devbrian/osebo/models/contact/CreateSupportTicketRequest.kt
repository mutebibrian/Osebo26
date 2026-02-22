package com.devbrian.osebo.models.contact

data class CreateSupportTicketRequest(
    val category: String,
    val subject: String,
    val message: String,
    val shopId: String? = null,
    val priority: String = "normal"
)