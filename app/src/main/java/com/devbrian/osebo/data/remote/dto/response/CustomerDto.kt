package com.devbrian.osebo.data.remote.dto.response

import com.google.gson.annotations.SerializedName
import com.devbrian.osebo.models.Customer

data class CustomerDto(
    @SerializedName("id") val id: String,
    @SerializedName("name") val name: String,
    @SerializedName("phone") val phone: String?,
    @SerializedName("email") val email: String?,
    @SerializedName("location") val location: String?,
    @SerializedName("isDefault") val isDefault: Boolean,
    @SerializedName("numberOfSales") val numberOfSales: String?,
    @SerializedName("totalSales") val totalSales: Double?,
    @SerializedName("outstandingBalance") val outstandingBalance: Double?,
    @SerializedName("createdAt") val createdAt: String?,
    @SerializedName("updatedAt") val updatedAt: String?
) {
    fun toCustomer(): Customer {
        return Customer(
            id = this.id,
            name = this.name,
            phone = this.phone ?: "",
            email = this.email,
            address = this.location,
            location = this.location,
            isDefault = this.isDefault,
            totalSpent = this.totalSales ?: 0.0,
            lastPurchase = null,
            totalPurchases = this.numberOfSales?.toIntOrNull() ?: 0,
            customerSince = this.createdAt,
            loyaltyPoints = 0,
            status = "active",
            customerType = if (this.isDefault) "default" else "regular",
            createdAt = this.createdAt,
            updatedAt = this.updatedAt
        )
    }
}