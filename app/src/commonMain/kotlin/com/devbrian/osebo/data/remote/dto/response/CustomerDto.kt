package com.devbrian.osebo.data.remote.dto.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import com.devbrian.osebo.models.Customer

@Serializable
data class CustomerDto(
    @SerialName("id") val id: String,
    @SerialName("name") val name: String,
    @SerialName("phone") val phone: String?,
    @SerialName("email") val email: String?,
    @SerialName("location") val location: String?,
    @SerialName("isDefault") val isDefault: Boolean,
    @SerialName("numberOfSales") val numberOfSales: String?,
    @SerialName("totalSales") val totalSales: Double?,
    @SerialName("outstandingBalance") val outstandingBalance: Double?,
    @SerialName("createdAt") val createdAt: String?,
    @SerialName("updatedAt") val updatedAt: String?
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


