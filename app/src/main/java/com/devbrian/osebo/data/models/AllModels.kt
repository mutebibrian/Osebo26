package com.devbrian.osebo.models

import com.google.gson.annotations.SerializedName




data class InventoryItem(
    @SerializedName("id") val id: String,
    @SerializedName("name") val name: String,
    @SerializedName("category") val category: String,
    @SerializedName("price") val price: Double,
    @SerializedName("cost_price") val costPrice: Double,
    @SerializedName("quantity") val quantity: Int,
    @SerializedName("min_stock_level") val minStockLevel: Int? = null,
    @SerializedName("barcode") val barcode: String? = null,
    @SerializedName("description") val description: String? = null,
    @SerializedName("image_url") val imageUrl: String? = null,
    @SerializedName("shop_id") val shopId: String,
    @SerializedName("created_at") val createdAt: String,
    @SerializedName("updated_at") val updatedAt: String
)








data class Notification(
    @SerializedName("id") val id: String,
    @SerializedName("title") val title: String,
    @SerializedName("message") val message: String,
    @SerializedName("type") val type: String,
    @SerializedName("is_read") val isRead: Boolean,
    @SerializedName("created_at") val createdAt: String,
    @SerializedName("data") val data: Map<String, Any>? = null
)


data class UploadResponse(
    @SerializedName("url") val url: String,
    @SerializedName("file_name") val fileName: String,
    @SerializedName("file_size") val fileSize: Long
)



