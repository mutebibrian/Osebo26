package com.devbrian.osebo.models

data class Product(
    val id: String,
    val name: String,
    val sku: String,
    val category: String,
    val price: Double,
    val cost: Double? = null,
    val stock: Int,
    val lowStockThreshold: Int,
    val imageUrl: String? = null,
    val description: String? = null,
    val barcode: String? = null,
    val supplierId: String? = null,
    val supplierName: String? = null,
    val taxRate: Double? = null,
    val weight: Double? = null,
    val dimensions: String? = null,
    val location: String? = null,
    val isActive: Boolean = true,
    val createdAt: String? = null,
    val updatedAt: String? = null
) {
    companion object {
        // Common Categories
        const val CATEGORY_ELECTRONICS = "ELECTRONICS"
        const val CATEGORY_CLOTHING = "CLOTHING"
        const val CATEGORY_FOOD = "FOOD"
        const val CATEGORY_BEVERAGES = "BEVERAGES"
        const val CATEGORY_OFFICE_SUPPLIES = "OFFICE_SUPPLIES"
        const val CATEGORY_FURNITURE = "FURNITURE"
        const val CATEGORY_COSMETICS = "COSMETICS"
        const val CATEGORY_PHARMACEUTICALS = "PHARMACEUTICALS"
        const val CATEGORY_HARDWARE = "HARDWARE"
        const val CATEGORY_OTHER = "OTHER"

        // Status Constants
        const val STATUS_ACTIVE = "ACTIVE"
        const val STATUS_INACTIVE = "INACTIVE"
        const val STATUS_DISCONTINUED = "DISCONTINUED"

        // Helper methods
        fun getCategoryDisplayName(category: String): String {
            return when (category.uppercase()) {
                CATEGORY_ELECTRONICS -> "Electronics"
                CATEGORY_CLOTHING -> "Clothing"
                CATEGORY_FOOD -> "Food"
                CATEGORY_BEVERAGES -> "Beverages"
                CATEGORY_OFFICE_SUPPLIES -> "Office Supplies"
                CATEGORY_FURNITURE -> "Furniture"
                CATEGORY_COSMETICS -> "Cosmetics"
                CATEGORY_PHARMACEUTICALS -> "Pharmaceuticals"
                CATEGORY_HARDWARE -> "Hardware"
                else -> category
            }
        }

        fun calculateProfitMargin(price: Double, cost: Double?): Double {
            return if (cost != null && cost > 0) {
                ((price - cost) / price) * 100
            } else {
                0.0
            }
        }

        fun calculateTotalValue(price: Double, stock: Int): Double {
            return price * stock
        }

        fun isLowStock(stock: Int, lowStockThreshold: Int): Boolean {
            return stock <= lowStockThreshold
        }

        fun isOutOfStock(stock: Int): Boolean {
            return stock == 0
        }

        fun needsRestock(stock: Int, lowStockThreshold: Int): Boolean {
            return stock <= lowStockThreshold
        }
    }
}