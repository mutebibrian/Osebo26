package com.devbrian.osebo.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.devbrian.osebo.data.remote.dto.response.ProductDto
import com.devbrian.osebo.models.Product

@Entity(tableName = "products")
data class ProductEntity(
    @PrimaryKey
    val id: String,
    val name: String,
    val sku: String,
    val description: String?,
    val category: String?,
    val categoryId: String?,
    val price: Double,
    val cost: Double?,
    val stock: Double,  // Changed from Int to Double
    val lowStockThreshold: Int?,
    val imageUrl: String?,
    val barcode: String?,
    val supplierId: String?,
    val supplierName: String?,
    val taxRate: Double?,
    val weight: Double?,
    val dimensions: String?,
    val location: String?,
    val isActive: Boolean,
    val maxDiscount: Double?,
    val unit: String?,
    val allowsFloatQuantity: Boolean,
    val shopId: String?,
    val shopName: String?,
    val photos: String?,  // Stored as JSON string
    val createdAt: String?,
    val updatedAt: String?,
    val lastSyncedAt: Long = System.currentTimeMillis()
) {
    fun toProduct(): Product {
        return Product(
            id = id,
            name = name,
            sku = sku,
            category = category,
            categoryId = categoryId,
            price = price,
            cost = cost,
            stock = stock,
            lowStockThreshold = lowStockThreshold,
            imageUrl = imageUrl,
            description = description,
            barcode = barcode,
            supplierId = supplierId,
            supplierName = supplierName,
            taxRate = taxRate,
            weight = weight,
            dimensions = dimensions,
            location = location,
            isActive = isActive,
            createdAt = createdAt,
            updatedAt = updatedAt,
            maxDiscount = maxDiscount,
            unit = unit,
            allowsFloatQuantity = allowsFloatQuantity,
            shopId = shopId,
            shopName = shopName,
            photos = photos?.split(",")?.filter { it.isNotEmpty() }
        )
    }

    companion object {
        fun fromProduct(product: Product, shopId: String? = null): ProductEntity {
            return ProductEntity(
                id = product.id,
                name = product.name,
                sku = product.sku,
                description = product.description,
                category = product.category,
                categoryId = product.categoryId,
                price = product.price,
                cost = product.cost,
                stock = product.stock,
                lowStockThreshold = product.lowStockThreshold,
                imageUrl = product.imageUrl,
                barcode = product.barcode,
                supplierId = product.supplierId,
                supplierName = product.supplierName,
                taxRate = product.taxRate,
                weight = product.weight,
                dimensions = product.dimensions,
                location = product.location,
                isActive = product.isActive,
                maxDiscount = product.maxDiscount,
                unit = product.unit,
                allowsFloatQuantity = product.allowsFloatQuantity == true,
                shopId = shopId ?: product.shopId,
                shopName = product.shopName,
                photos = product.photos?.joinToString(","),
                createdAt = product.createdAt,
                updatedAt = product.updatedAt
            )
        }

        fun fromDto(dto: ProductDto, shopId: String): ProductEntity {
            return ProductEntity(
                id = dto.id,
                name = dto.name,
                sku = dto.sku,
                description = dto.description,
                category = dto.stockCategory.name,
                categoryId = dto.stockCategory.id,
                price = dto.sellingPrice,
                cost = null,
                stock = dto.quantity,  // Now Double, matches the DTO
                lowStockThreshold = dto.lowQuantityMark,
                imageUrl = dto.photos?.firstOrNull(),
                barcode = dto.barcode,
                supplierId = null,
                supplierName = null,
                taxRate = null,
                weight = null,
                dimensions = null,
                location = null,
                isActive = true,
                maxDiscount = dto.maxDiscount,
                unit = dto.unitMeasure,
                allowsFloatQuantity = dto.allowsFloatQuantity,
                shopId = shopId,
                shopName = dto.shop.name,
                photos = dto.photos?.joinToString(","),
                createdAt = dto.createdAt,
                updatedAt = dto.updatedAt
            )
        }
    }
}