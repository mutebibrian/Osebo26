package com.devbrian.osebo.models.contact

import com.google.gson.annotations.SerializedName

// Support Ticket Models
data class SupportTicketRequest(
    @SerializedName("category")
    val category: String,

    @SerializedName("subject")
    val subject: String,

    @SerializedName("message")
    val message: String,

    @SerializedName("shop_id")
    val shopId: String? = null,

    @SerializedName("attachments")
    val attachments: List<String>? = null,

    @SerializedName("priority")
    val priority: String = "normal" // low, normal, high, urgent
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

    @SerializedName("message")
    val message: String,

    @SerializedName("status")
    val status: String, // open, in_progress, resolved, closed

    @SerializedName("priority")
    val priority: String,

    @SerializedName("shop_id")
    val shopId: String?,

    @SerializedName("user_id")
    val userId: String,

    @SerializedName("assigned_to")
    val assignedTo: String? = null,

    @SerializedName("created_at")
    val createdAt: String,

    @SerializedName("updated_at")
    val updatedAt: String,

    @SerializedName("resolved_at")
    val resolvedAt: String? = null
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
    val senderType: String, // user, support_agent

    @SerializedName("sender_id")
    val senderId: String,

    @SerializedName("sender_name")
    val senderName: String,

    @SerializedName("attachments")
    val attachments: List<String>? = null,

    @SerializedName("created_at")
    val createdAt: String
)

// FAQ Models
data class FAQ(
    @SerializedName("id")
    val id: String,

    @SerializedName("category")
    val category: String,

    @SerializedName("question")
    val question: String,

    @SerializedName("answer")
    val answer: String,

    @SerializedName("language")
    val language: String = "en",

    @SerializedName("views")
    val views: Int = 0,

    @SerializedName("is_active")
    val isActive: Boolean = true,

    @SerializedName("created_at")
    val createdAt: String,

    @SerializedName("updated_at")
    val updatedAt: String
)

// Contact Information Models


data class SocialMedia(
    @SerializedName("facebook")
    val facebook: String? = null,

    @SerializedName("twitter")
    val twitter: String? = null,

    @SerializedName("instagram")
    val instagram: String? = null,

    @SerializedName("linkedin")
    val linkedIn: String? = null,

    @SerializedName("whatsapp")
    val whatsapp: String? = null
)

// Contact Form Models
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


