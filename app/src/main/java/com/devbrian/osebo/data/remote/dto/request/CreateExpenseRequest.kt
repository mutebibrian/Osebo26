package com.devbrian.osebo.data.remote.dto.request



import com.google.gson.annotations.SerializedName

data class CreateExpenseRequest(
    @SerializedName("description")
    val description: String,

    @SerializedName("amount")
    val amount: Double,

    @SerializedName("category_id")
    val categoryId: String,

    @SerializedName("date")
    val date: String,

    @SerializedName("notes")
    val notes: String? = null,

    @SerializedName("receipt_url")
    val receiptUrl: String? = null,

    @SerializedName("payment_method")
    val paymentMethod: String? = null,

    @SerializedName("reference")
    val reference: String? = null
)

data class UpdateExpenseRequest(
    @SerializedName("description")
    val description: String? = null,

    @SerializedName("amount")
    val amount: Double? = null,

    @SerializedName("category_id")
    val categoryId: String? = null,

    @SerializedName("date")
    val date: String? = null,

    @SerializedName("notes")
    val notes: String? = null,

    @SerializedName("status")
    val status: String? = null
)

data class CreateExpenseCategoryRequest(
    @SerializedName("name")
    val name: String,

    @SerializedName("description")
    val description: String,

    @SerializedName("color")
    val color: String? = null,

    @SerializedName("icon")
    val icon: String? = null
)

data class UpdateExpenseCategoryRequest(
    @SerializedName("name")
    val name: String? = null,

    @SerializedName("description")
    val description: String? = null,

    @SerializedName("color")
    val color: String? = null,

    @SerializedName("icon")
    val icon: String? = null
)