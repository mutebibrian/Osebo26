package com.devbrian.osebo.models

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class Shop(
    @SerializedName("id")
    val id: String = "",

    @SerializedName("name")
    val name: String = "",

    @SerializedName("description")
    val description: String? = null,

    @SerializedName("location")
    val location: String? = null,

    @SerializedName("category")
    val category: String? = null,

    @SerializedName("logo_url")
    val logoUrl: String? = null,

    @SerializedName("total_revenue")
    val totalRevenue: Double? = null,

    @SerializedName("total_expenses")
    val totalExpenses: Double? = null,

    @SerializedName("profit")
    val profit: Double? = null,

    @SerializedName("total_products")
    val totalProducts: Int? = null,

    @SerializedName("total_employees")
    val totalEmployees: Int? = null,

    @SerializedName("owner_id")
    val ownerId: String = "",

    @SerializedName("is_active")
    val isActive: Boolean = false,

    @SerializedName("subscription_status")
    val subscriptionStatus: String = "inactive",

    @SerializedName("created_at")
    val createdAt: String = "",

    @SerializedName("updated_at")
    val updatedAt: String = ""
) : Parcelable