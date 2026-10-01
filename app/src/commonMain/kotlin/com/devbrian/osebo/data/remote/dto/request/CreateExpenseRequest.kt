package com.devbrian.osebo.data.remote.dto.request

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CreateExpenseRequest(
    @SerialName("name")
    val name: String,  // ADD THIS - server requires name field

    @SerialName("description")
    val description: String,

    @SerialName("amount")
    val amount: Double,

    @SerialName("expense_category_id")  // CHANGE from category_id to expense_category_id
    val expenseCategoryId: String,

    @SerialName("date")
    val date: String,

    @SerialName("notes")
    val notes: String? = null,

    @SerialName("receipt_url")
    val receiptUrl: String? = null,

    @SerialName("payment_method")
    val paymentMethod: String? = null,

    @SerialName("reference")
    val reference: String? = null
)

@Serializable
data class UpdateExpenseRequest(
    @SerialName("description")
    val description: String? = null,

    @SerialName("amount")
    val amount: Double? = null,

    @SerialName("expense_category_id")  // CHANGE from category_id to expense_category_id
    val expenseCategoryId: String? = null,

    @SerialName("date")
    val date: String? = null,

    @SerialName("notes")
    val notes: String? = null,

    @SerialName("status")
    val status: String? = null
)

@Serializable
data class CreateExpenseCategoryRequest(
    @SerialName("name")
    val name: String,

    @SerialName("description")
    val description: String?,

    @SerialName("color")
    val color: String? = null,

    @SerialName("icon")
    val icon: String? = null
)

@Serializable
data class UpdateExpenseCategoryRequest(
    @SerialName("name")
    val name: String? = null,

    @SerialName("description")
    val description: String? = null,

    @SerialName("color")
    val color: String? = null,

    @SerialName("icon")
    val icon: String? = null
)