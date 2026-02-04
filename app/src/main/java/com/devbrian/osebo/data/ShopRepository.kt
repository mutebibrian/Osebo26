package com.devbrian.osebo.data

import com.devbrian.osebo.models.Shop
import kotlinx.coroutines.delay

class ShopRepository {

    // Fake in-memory data (replace later with Room / API / Firebase)
    private val shops = mutableListOf(
        Shop("1", "Osebo Mart", "Retail", "Kampala", "0700000000"),
        Shop("2", "Osebo Wholesale", "Wholesale", "Mukono", "0711111111")
    )

    suspend fun getShops(): List<Shop> {
        delay(800) // simulate network/db delay
        return shops.toList()
    }

    suspend fun deleteShop(shopId: String) {
        delay(300)
        shops.removeAll { it.id == shopId }
    }
}
