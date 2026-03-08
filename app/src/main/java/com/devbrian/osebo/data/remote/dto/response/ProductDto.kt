package com.devbrian.osebo.data.remote.dto.response

import com.google.gson.annotations.SerializedName
import com.devbrian.osebo.models.Product

data class ProductDto(
    @SerializedName("id") val id: String,
    @SerializedName("name") val name: String,
    @SerializedName("sku") val sku: String,
    @SerializedName("description") val description: String?,
    @SerializedName("low_quantity_mark") val lowQuantityMark: Int,
    @SerializedName("selling_price") val sellingPrice: Double,
    @SerializedName("unit_measure") val unitMeasure: String,
    @SerializedName("max_discount") val maxDiscount: Double,
    @SerializedName("barcode") val barcode: String?,
    @SerializedName("photos") val photos: List<String>?,
    @SerializedName("quantity") val quantity: Int,
    @SerializedName("stock_category") val stockCategory: StockCategoryDto,
    @SerializedName("shop") val shop: ShopDto,
    @SerializedName("allowsFloatQuantity") val allowsFloatQuantity: Boolean,
    @SerializedName("createdAt") val createdAt: String?,
    @SerializedName("updatedAt") val updatedAt: String?
) {
    fun toProduct(): Product {
        return Product(
            id = this.id,
            name = this.name,
            sku = this.sku,
            category = this.stockCategory.name,  
            price = this.sellingPrice,
            cost = null,  
            stock = this.quantity,
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
            updatedAt = this.updatedAt
        )
    }
}




