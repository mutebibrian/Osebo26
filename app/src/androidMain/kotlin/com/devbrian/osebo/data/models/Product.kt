package com.devbrian.osebo.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Product(
    val id: String,
    val name: String,
    val sku: String,
    val category: String? = null,
    val categoryId: String? = null,
    val price: Double,
    val cost: Double? = null,
    val stock: Double,
    val lowStockThreshold: Int? = null,
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
    val updatedAt: String? = null,

    // New fields from DTO/Entity
    val maxDiscount: Double? = null,
    val unit: String? = null,
    val allowsFloatQuantity: Boolean? = null,
    val shopId: String? = null,
    val shopName: String? = null,
    val photos: List<String>? = null
) : Parcelable {

    // Computed properties
    val displayPrice: String
        get() = "UGX ${String.format("%,.0f", price)}"

    val displayStock: String
        get() = if (allowsFloatQuantity == true) {
            String.format("%.1f", stock)
        } else {
            String.format("%.0f", stock)
        }

    val isLowStock: Boolean
        get() = lowStockThreshold != null && stock <= lowStockThreshold

    val isOutOfStock: Boolean
        get() = stock <= 0

    val hasBarcode: Boolean
        get() = !barcode.isNullOrEmpty()

    val displayName: String
        get() = if (sku.isNotEmpty()) "$name ($sku)" else name

    val categoryDisplay: String
        get() = getCategoryDisplayName(category ?: "OTHER")

    val profitMargin: Double
        get() = if (cost != null && cost > 0 && price > 0) {
            ((price - cost) / price) * 100
        } else {
            0.0
        }

    val totalValue: Double
        get() = price * stock

    val needsRestock: Boolean
        get() = lowStockThreshold != null && stock <= lowStockThreshold

    val primaryImage: String?
        get() = imageUrl ?: photos?.firstOrNull()

    val unitDisplay: String
        get() = unit ?: "piece"

    val allowsDecimalQuantity: Boolean
        get() = allowsFloatQuantity == true

    companion object {
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

        const val STATUS_ACTIVE = "ACTIVE"
        const val STATUS_INACTIVE = "INACTIVE"
        const val STATUS_DISCONTINUED = "DISCONTINUED"

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
                else -> category.replaceFirstChar { it.uppercase() }
            }
        }

        fun calculateProfitMargin(price: Double, cost: Double?): Double {
            return if (cost != null && cost > 0 && price > 0) {
                ((price - cost) / price) * 100
            } else {
                0.0
            }
        }

        fun calculateTotalValue(price: Double, stock: Double): Double {
            return price * stock
        }

        fun isLowStock(stock: Double, lowStockThreshold: Int?): Boolean {
            return lowStockThreshold != null && stock <= lowStockThreshold
        }

        fun isOutOfStock(stock: Double): Boolean {
            return stock <= 0
        }

        fun needsRestock(stock: Double, lowStockThreshold: Int?): Boolean {
            return lowStockThreshold != null && stock <= lowStockThreshold
        }

        /**
         * Create a sample product for testing
         */
        fun createSample(id: String = "1"): Product {
            return Product(
                id = id,
                name = "Sample Product $id",
                sku = "SKU$id",
                category = CATEGORY_ELECTRONICS,
                categoryId = "cat_$id",
                price = 50000.0,
                cost = 35000.0,
                stock = 50.0,  // Changed to Double
                lowStockThreshold = 10,
                imageUrl = null,
                description = "This is a sample product description",
                barcode = "123456789$id",
                supplierId = "sup_$id",
                supplierName = "Sample Supplier",
                taxRate = 18.0,
                weight = 1.5,
                dimensions = "10x20x30 cm",
                location = "Aisle 1, Shelf 2",
                isActive = true,
                createdAt = "2024-01-01T00:00:00Z",
                updatedAt = "2024-01-01T00:00:00Z",
                maxDiscount = 10.0,
                unit = "piece",
                allowsFloatQuantity = false,
                shopId = "shop_1",
                shopName = "Main Shop",
                photos = listOf("https://example.com/image1.jpg", "https://example.com/image2.jpg")
            )
        }
    }
}