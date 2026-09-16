package com.devbrian.osebo.data.remote.api


import com.google.gson.annotations.SerializedName

// Field names beyond id/status/timestamps are provisional — the backend has
// no populated example response yet (every field we haven't confirmed
// against a real payload is nullable so parsing degrades gracefully instead
// of crashing once we do see real data).
data class TransferDto(
    @SerializedName("id")
    val id: String,

    @SerializedName("source_shop")
    val sourceShop: String? = null,

    @SerializedName("target_shop")
    val targetShop: String? = null,

    @SerializedName("source_shop_name")
    val sourceShopName: String? = null,

    @SerializedName("target_shop_name")
    val targetShopName: String? = null,

    @SerializedName("stock_item_id")
    val stockItemId: String? = null,

    @SerializedName("stock_item_name")
    val stockItemName: String? = null,

    @SerializedName("quantity")
    val quantity: Double? = null,

    @SerializedName("items")
    val items: List<TransferItemDto>? = null,

    @SerializedName("status")
    val status: String? = null,

    @SerializedName("created_at")
    val createdAt: String? = null,

    @SerializedName("updated_at")
    val updatedAt: String? = null
)

data class TransferItemDto(
    @SerializedName("stockItemId")
    val stockItemId: String? = null,

    @SerializedName("stock_item_name")
    val stockItemName: String? = null,

    @SerializedName("quantity")
    val quantity: Double? = null
)
