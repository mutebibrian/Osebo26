package com.devbrian.osebo.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.devbrian.osebo.models.Product
import java.util.Date

@Entity(tableName = "products")
data class ProductEntity(
    @PrimaryKey
    val id: String,
    val name: String,
    val sku: String,
    val category: String,
    val categoryId: String? = null,
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
    val lastSyncedAt: Long = System.currentTimeMillis(),
    val isPendingSync: Boolean = false,
    val syncAction: String? = null,
    val shopId: String,

    // Additional fields from DTO
    val maxDiscount: Double = 0.0,
    val unitMeasure: String = "piece",
    val allowsFloatQuantity: Boolean = false,
    val shopName: String? = null,
    val createdAt: String? = null,
    val updatedAt: String? = null,
    val photos: List<String>? = null
) {
    fun toProduct(): Product {
        return Product(
            id = this.id,
            name = this.name,
            sku = this.sku,
            category = this.category,
            categoryId = this.categoryId,
            price = this.price,
            cost = this.cost,
            stock = this.stock,
            lowStockThreshold = this.lowStockThreshold,
            imageUrl = this.imageUrl ?: this.photos?.firstOrNull(),
            description = this.description,
            barcode = this.barcode,
            supplierId = this.supplierId,
            supplierName = this.supplierName,
            taxRate = this.taxRate,
            weight = this.weight,
            dimensions = this.dimensions,
            location = this.location,
            isActive = this.isActive,
            maxDiscount = this.maxDiscount,
            unit = this.unitMeasure,
            allowsFloatQuantity = this.allowsFloatQuantity,
            shopId = this.shopId,
            shopName = this.shopName,
            createdAt = this.createdAt,
            updatedAt = this.updatedAt,
            photos = this.photos
        )
    }

    companion object {
        fun fromProduct(product: Product, shopId: String, isPendingSync: Boolean = false, syncAction: String? = null): ProductEntity {
            return ProductEntity(
                id = product.id,
                name = product.name,
                sku = product.sku,
                category = product.category ?: "",
                categoryId = product.categoryId,
                price = product.price,
                cost = product.cost,
                stock = product.stock,
                lowStockThreshold = product.lowStockThreshold ?: 0,
                imageUrl = product.imageUrl,
                description = product.description,
                barcode = product.barcode,
                supplierId = product.supplierId,
                supplierName = product.supplierName,
                taxRate = product.taxRate,
                weight = product.weight,
                dimensions = product.dimensions,
                location = product.location,
                isActive = product.isActive ?: true,
                lastSyncedAt = System.currentTimeMillis(),
                isPendingSync = isPendingSync,
                syncAction = syncAction,
                shopId = shopId,
                maxDiscount = product.maxDiscount ?: 0.0,
                unitMeasure = product.unit ?: "piece",
                allowsFloatQuantity = product.allowsFloatQuantity ?: false,
                shopName = product.shopName,
                createdAt = product.createdAt,
                updatedAt = product.updatedAt,
                photos = product.photos
            )
        }

        /**
         * Create ProductEntity from DTO
         */
        fun fromDto(
            id: String,
            name: String,
            sku: String,
            category: String,
            categoryId: String?,
            price: Double,
            stock: Int,
            lowStockThreshold: Int,
            barcode: String?,
            shopId: String,
            shopName: String?,
            description: String? = null,
            imageUrl: String? = null,
            maxDiscount: Double = 0.0,
            unitMeasure: String = "piece",
            allowsFloatQuantity: Boolean = false,
            createdAt: String? = null,
            updatedAt: String? = null,
            photos: List<String>? = null
        ): ProductEntity {
            return ProductEntity(
                id = id,
                name = name,
                sku = sku,
                category = category,
                categoryId = categoryId,
                price = price,
                cost = null,
                stock = stock,
                lowStockThreshold = lowStockThreshold,
                imageUrl = imageUrl,
                description = description,
                barcode = barcode,
                supplierId = null,
                supplierName = null,
                taxRate = null,
                weight = null,
                dimensions = null,
                location = null,
                isActive = true,
                lastSyncedAt = System.currentTimeMillis(),
                isPendingSync = false,
                syncAction = null,
                shopId = shopId,
                maxDiscount = maxDiscount,
                unitMeasure = unitMeasure,
                allowsFloatQuantity = allowsFloatQuantity,
                shopName = shopName,
                createdAt = createdAt,
                updatedAt = updatedAt,
                photos = photos
            )
        }
    }
}