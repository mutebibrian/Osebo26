package com.devbrian.osebo.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.devbrian.osebo.models.Sale
import com.google.gson.JsonParser

@Entity(tableName = "sales")
data class SaleEntity(
    @PrimaryKey
    val id: String,
    val invoiceNumber: String,
    val customerId: String?,
    val customerName: String,
    val totalAmount: Double,
    val paidAmount: Double,
    val change: Double,
    val saleType: String,
    val status: String,
    val items: String,
    val paymentMethod: String?,
    val notes: String?,
    val createdAt: String,
    val shopId: String,
    val isPendingSync: Boolean = false,
    val syncAction: String? = null
) {
    fun toSale(): Sale {
        // Parse items count from JSON if needed
        val itemCount = try {
            val itemsJson = com.google.gson.JsonParser.parseString(this.items).asJsonArray
            itemsJson.size()
        } catch (e: Exception) {
            println("❌ Error parsing items JSON: ${e.message}")
            0
        }

        // Map the status correctly
        val displayStatus = when (this.status.uppercase()) {
            "COMPLETED" -> "COMPLETED"
            "PENDING" -> "PENDING"
            "PARTIAL" -> "PARTIAL"
            "CANCELLED" -> "CANCELLED"
            "REFUNDED" -> "REFUNDED"
            else -> {
                // If it's something else, check if it might be from API
                when (this.status.lowercase()) {
                    "completed" -> "COMPLETED"
                    "pending" -> "PENDING"
                    "partial" -> "PARTIAL"
                    "cancelled" -> "CANCELLED"
                    "refunded" -> "REFUNDED"
                    else -> "PENDING"
                }
            }
        }

        return Sale(
            id = this.id,
            customerName = this.customerName,
            amount = this.totalAmount,
            date = this.createdAt,
            itemsCount = itemCount,
            status = displayStatus,
            paymentMethod = this.paymentMethod,
            discount = null,
            tax = null
        )
    }
}