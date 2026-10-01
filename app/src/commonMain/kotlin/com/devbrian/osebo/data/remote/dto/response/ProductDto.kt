package com.devbrian.osebo.data.remote.dto.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import com.devbrian.osebo.models.Product

@Serializable
data class ProductDto(
    @SerialName("id") val id: String,
    @SerialName("name") val name: String,
    @SerialName("sku") val sku: String,
    @SerialName("description") val description: String?,
    @SerialName("low_quantity_mark") val lowQuantityMark: Int,
    @SerialName("selling_price") val sellingPrice: Double,
    @SerialName("unit_measure") val unitMeasure: String,
    @SerialName("max_discount") val maxDiscount: Double,
    @SerialName("barcode") val barcode: String?,
    @SerialName("photos") val photos: List<String>?,
    @SerialName("quantity") val quantity: Double,  // Changed from Int to Double
    @SerialName("stock_category") val stockCategory: StockCategoryDto,
    @SerialName("shop") val shop: ShopDto,
    @SerialName("allowsFloatQuantity") val allowsFloatQuantity: Boolean,
    @SerialName("createdAt") val createdAt: String?,
    @SerialName("updatedAt") val updatedAt: String?
) {
    fun toProduct(): Product {
        return Product(
            id = this.id,
            name = this.name,
            sku = this.sku,
            category = this.stockCategory.name,
            categoryId = this.stockCategory.id,
            price = this.sellingPrice,
            cost = null,
            stock = this.quantity,  // Now works with Double
            lowStockThreshold = this.lowQuantityMark,
            imageUrl = this.photos?.firstOrNull(),
            description = this.description,
            barcode = this.barcode,
            supplierId = null,
            supplierName = null,
            taxRate = null,
            weight = null,
            dimensions = null,
            location = null,
            isActive = true,
            createdAt = this.createdAt,
            updatedAt = this.updatedAt,
            maxDiscount = this.maxDiscount,
            unit = this.unitMeasure,
            allowsFloatQuantity = this.allowsFloatQuantity,
            shopId = this.shop.id,
            shopName = this.shop.name,
            photos = this.photos
        )
    }
}