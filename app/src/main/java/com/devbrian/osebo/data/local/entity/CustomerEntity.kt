package com.devbrian.osebo.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.devbrian.osebo.models.Customer

@Entity(tableName = "customers")
data class CustomerEntity(
    @PrimaryKey
    val id: String,
    val name: String,
    val phone: String,
    val email: String?,
    val location: String?,
    val isDefault: Boolean,
    val totalSpent: Double,
    val lastPurchase: String?,
    val totalPurchases: Int,
    val customerSince: String?,
    val loyaltyPoints: Int,
    val status: String,
    val createdAt: String?,
    val updatedAt: String?,
    val shopId: String,
    val isPendingSync: Boolean = false,
    val syncAction: String? = null
) {
    fun toCustomer(): Customer {
        return Customer(
            id = this.id,
            name = this.name,
            phone = this.phone,
            email = this.email,
            address = this.location,
            location = this.location,
            isDefault = this.isDefault,
            totalSpent = this.totalSpent,
            lastPurchase = this.lastPurchase,
            totalPurchases = this.totalPurchases,
            customerSince = this.customerSince,
            loyaltyPoints = this.loyaltyPoints,
            status = this.status,
            customerType = if (this.totalSpent > 500000) "vip" else "regular",
            createdAt = this.createdAt,
            updatedAt = this.updatedAt
        )
    }
}