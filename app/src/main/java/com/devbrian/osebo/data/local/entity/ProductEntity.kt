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
    val shopId: String
) {
    fun toProduct(): Product {
        return Product(
            id = this.id,
            name = this.name,
            sku = this.sku,
            category = this.category,
            price = this.price,
            cost = this.cost,
            stock = this.stock,
            lowStockThreshold = this.lowStockThreshold,
            imageUrl = this.imageUrl,
            description = this.description,
            barcode = this.barcode,
            supplierId = this.supplierId,
            supplierName = this.supplierName,
            taxRate = this.taxRate,
            weight = this.weight,
            dimensions = this.dimensions,
            location = this.location,
            isActive = this.isActive
        )
    }

    companion object {
        fun fromProduct(product: Product, shopId: String, isPendingSync: Boolean = false, syncAction: String? = null): ProductEntity {
            return ProductEntity(
                id = product.id,
                name = product.name,
                sku = product.sku,
                category = product.category,
                price = product.price,
                cost = product.cost,
                stock = product.stock,
                lowStockThreshold = product.lowStockThreshold,
                imageUrl = product.imageUrl,
                description = product.description,
                barcode = product.barcode,
                supplierId = product.supplierId,
                supplierName = product.supplierName,
                taxRate = product.taxRate,
                weight = product.weight,
                dimensions = product.dimensions,
                location = product.location,
                isActive = product.isActive,
                lastSyncedAt = System.currentTimeMillis(),
                isPendingSync = isPendingSync,
                syncAction = syncAction,
                shopId = shopId
            )
        }
    }
}

