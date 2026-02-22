package com.devbrian.osebo.models.contact

import com.google.gson.annotations.SerializedName

data class ContactFormRequest(
    @SerializedName("name")
    val name: String,

    @SerializedName("email")
    val email: String,

    @SerializedName("phone")
    val phone: String? = null,

    @SerializedName("subject")
    val subject: String,

    @SerializedName("message")
    val message: String,

    @SerializedName("shop_id")
    val shopId: String? = null
)

data class SupportTicket(
    @SerializedName("id")
    val id: String,

    @SerializedName("ticket_number")
    val ticketNumber: String,

    @SerializedName("category")
    val category: String,

    @SerializedName("subject")
    val subject: String,

    @SerializedName("status")
    val status: String,

    @SerializedName("priority")
    val priority: String,

    @SerializedName("created_at")
    val createdAt: String,

    @SerializedName("updated_at")
    val updatedAt: String,

    @SerializedName("user_id")
    val userId: String? = null,

    @SerializedName("shop_id")
    val shopId: String? = null
)

data class SupportTicketResponse(
    @SerializedName("ticket")
    val ticket: SupportTicket,

    @SerializedName("messages")
    val messages: List<TicketMessage> = emptyList()
)

data class TicketMessage(
    @SerializedName("id")
    val id: String,

    @SerializedName("ticket_id")
    val ticketId: String,

    @SerializedName("message")
    val message: String,

    @SerializedName("sender_type")
    val senderType: String,

    @SerializedName("sender_id")
    val senderId: String,

    @SerializedName("sender_name")
    val senderName: String? = null,

    @SerializedName("created_at")
    val createdAt: String
)

data class FAQ(
    @SerializedName("id")
    val id: String,

    @SerializedName("category")
    val category: String,

    @SerializedName("question")
    val question: String,

    @SerializedName("answer")
    val answer: String,

    @SerializedName("created_at")
    val createdAt: String,

    @SerializedName("updated_at")
    val updatedAt: String
)

