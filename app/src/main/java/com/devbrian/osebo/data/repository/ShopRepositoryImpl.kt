package com.devbrian.osebo.data.repository

import com.devbrian.osebo.data.ApiService
import com.devbrian.osebo.data.remote.dto.response.ShopDto
import com.devbrian.osebo.data.remote.dto.response.ShopSubscriptionDto
import com.devbrian.osebo.data.remote.dto.response.PackageDto
import com.devbrian.osebo.data.remote.dto.response.FeatureDto
import com.devbrian.osebo.models.Shop
import com.devbrian.osebo.models.ShopSubscription
import com.devbrian.osebo.models.SubscriptionPackage
import com.devbrian.osebo.models.Feature
import com.devbrian.osebo.utils.Resource
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ShopRepositoryImpl @Inject constructor(
    private val apiService: ApiService
) : ShopRepository {

    override suspend fun getShops(): Resource<List<Shop>> {
        return try {
            val response = apiService.getShops()
            if (response.isSuccessful) {
                val apiResponse = response.body()
                if (apiResponse?.success == true) {
                    val shopDtos = apiResponse.data ?: emptyList()
                    val shops = shopDtos.map { it.toShop() }
                    Resource.Success(shops)
                } else {
                    Resource.Error(apiResponse?.message ?: "Failed to fetch shops")
                }
            } else {
                Resource.Error("Network error: ${response.code()}")
            }
        } catch (e: IOException) {
            Resource.Error("Network error: ${e.message ?: "Check your internet connection"}")
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Unknown error")
        }
    }

    override suspend fun deleteShop(shopId: String) {
        try {
            val response = apiService.deleteShop(shopId)
            if (!response.isSuccessful) {
                throw Exception("Failed to delete shop: ${response.code()}")
            }
        } catch (e: Exception) {
            throw Exception("Failed to delete shop: ${e.message}")
        }
    }

    private fun ShopDto.toShop(): Shop {
        return Shop(
            id = this.id,
            name = this.name,
            address = this.address,
            description = this.description,
            shopType = this.shopType,
            phone = this.phone,
            email = this.email,
            registrationNumber = this.registrationNumber,
            taxIdentificationNumber = this.taxIdentificationNumber,
            ownerId = this.ownerId ?: "",
            isActive = this.isActive,
            status = this.status,
            createdAt = this.createdAt,
            updatedAt = this.updatedAt,
            totalRevenue = 0.0,
            totalExpenses = 0.0,
            profit = 0.0,
            totalProducts = 0,
            totalEmployees = 0,
            subscriptionStatus = this.subscription?.status ?: "inactive",
            subscriptionType = this.subscription?.packageType,
            subscriptionExpiry = this.subscription?.endsAt,
            planId = this.subscription?.packageDetails?.id,
            subscription = this.subscription?.toShopSubscription()
        )
    }

    private fun ShopSubscriptionDto.toShopSubscription(): ShopSubscription {
        return ShopSubscription(
            id = this.id,
            status = this.status ?: "inactive",
            packageType = this.packageType,
            subscriptionPackage = this.packageDetails?.toSubscriptionPackage(),
            startsAt = this.startsAt,
            endsAt = this.endsAt,
            isActive = this.isActive,
            durationDays = this.durationDays,
            isTrial = this.isTrial,
            payment = null
        )
    }

    private fun PackageDto.toSubscriptionPackage(): SubscriptionPackage {
        return SubscriptionPackage(
            id = this.id,
            name = this.name,
            tier = this.tier,
            type = this.type,
            description = this.description,
            unitMonthlyAmount = this.unitMonthlyAmount,
            features = this.features.map { it.toFeature() },
            isActive = this.isActive
        )
    }

    private fun FeatureDto.toFeature(): Feature {
        return Feature(
            name = this.name,
            included = this.included,
            description = this.description ?: ""
        )
    }
}