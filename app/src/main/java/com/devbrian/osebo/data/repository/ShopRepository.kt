package com.devbrian.osebo.data.repository

import com.devbrian.osebo.data.models.Shop
import com.devbrian.osebo.utils.Resource

interface ShopRepository {
    suspend fun getShops(): Resource<List<Shop>>
    suspend fun deleteShop(shopId: String)
}
