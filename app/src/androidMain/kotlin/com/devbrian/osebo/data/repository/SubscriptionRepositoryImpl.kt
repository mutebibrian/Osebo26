package com.devbrian.osebo.data.repository

import com.devbrian.osebo.data.ApiService
import com.devbrian.osebo.data.models.Shop
import com.devbrian.osebo.data.models.ShopSubscription
import com.devbrian.osebo.data.remote.dto.request.CreateSubscriptionRequest
import com.devbrian.osebo.data.remote.dto.response.FeatureDto
import com.devbrian.osebo.data.remote.dto.response.PackageDto
import com.devbrian.osebo.data.remote.dto.response.PaymentCheckResponse
import com.devbrian.osebo.data.remote.dto.response.PaymentDto
import com.devbrian.osebo.data.remote.dto.response.ShopDto
import com.devbrian.osebo.data.remote.dto.response.ShopSubscriptionDto
import com.devbrian.osebo.data.remote.dto.response.ShopSubscriptionStatusResponse
import com.devbrian.osebo.data.remote.dto.response.SubscriptionResponse
import com.devbrian.osebo.models.*
import com.devbrian.osebo.utils.Resource
import java.io.IOException

class SubscriptionRepositoryImpl(
    private val apiService: ApiService
) : SubscriptionRepository {

    override suspend fun getShops(): Resource<List<Shop>> {
        return try {
            val response = apiService.getShops()
            if (response.isSuccessful) {
                val apiResponse = response.body()
                if (apiResponse?.success == true) {
                    val shops = (apiResponse.data ?: emptyList()).map { it.toShop() }
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

    private fun ShopDto.toShop(): Shop {
        val subscription = this.subscription?.toShopSubscription()
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
            subscription = subscription
        )
    }

    private fun ShopSubscriptionDto.toShopSubscription(): ShopSubscription {
        // ✅ Removed the unused variable and the extra parameters
        return ShopSubscription(
            id = this.id,
            status = this.status,
            packageType = this.packageType,
            startsAt = this.startsAt,
            endsAt = this.endsAt,
            isActive = this.isActive,
            durationDays = this.durationDays,
            isTrial = this.isTrial
        )
    }

    private fun PackageDto.toSubscriptionPackage(): SubscriptionPackage {
        return SubscriptionPackage(
            id = this.id,
            name = this.name,
            tier = this.tier,
            type = this.type ?: "monthly",
            kind = this.kind,
            description = this.description,
            unitMonthlyAmount = this.unitMonthlyAmount,
            features = this.features?.map { it.toFeature() } ?: emptyList(),
            isActive = this.isActive,
            canTry = this.canTry
        )
    }

    private fun FeatureDto.toFeature(): Feature {
        return Feature(
            name = this.name,
            included = this.included,
            description = this.description ?: ""
        )
    }

    override suspend fun getSubscriptionPackages(): Resource<List<SubscriptionPackage>> {
        return try {
            val response = apiService.getSubscriptionPackages()
            if (response.isSuccessful) {
                val apiResponse = response.body()
                if (apiResponse?.success == true) {
                    val packages = apiResponse.data?.map { it.toSubscriptionPackage() } ?: emptyList()
                    Resource.Success(packages)
                } else {
                    Resource.Error(apiResponse?.message ?: "Failed to fetch packages")
                }
            } else {
                Resource.Error("Network error: ${response.code()}")
            }
        } catch (e: IOException) {
            Resource.Error("Network error: ${e.message ?: "Check your internet connection"}")
        } catch (e: Exception) {
            Resource.Error("Failed to load packages: ${e.message ?: "Unknown error"}")
        }
    }

    override suspend fun getSubscriptionDetails(subscriptionId: String): Resource<Subscription> {
        return try {
            val response = apiService.getSubscriptionDetails(subscriptionId)
            if (response.isSuccessful) {
                val apiResponse = response.body()
                if (apiResponse?.success == true) {
                    apiResponse.data?.let { Resource.Success(it) }
                        ?: Resource.Error("Subscription not found")
                } else {
                    Resource.Error(apiResponse?.message ?: "Failed to fetch subscription details")
                }
            } else {
                Resource.Error("Network error: ${response.code()}")
            }
        } catch (e: IOException) {
            Resource.Error("Network error: ${e.message ?: "Check your internet connection"}")
        } catch (e: Exception) {
            Resource.Error("Failed to load subscription details: ${e.message ?: "Unknown error"}")
        }
    }

    override suspend fun getShopActiveSubscription(shopId: String): Resource<Subscription> {
        return try {
            val response = apiService.getShopSubscriptions(shopId)
            if (response.isSuccessful) {
                val apiResponse = response.body()
                if (apiResponse?.success == true) {
                    val subscriptions = apiResponse.data ?: emptyList()

                    val activeSubscription = subscriptions.firstOrNull {
                        it.status.equals("ACTIVE", ignoreCase = true) ||
                                it.isTrial == true ||
                                it.status.equals("TRIAL", ignoreCase = true)
                    }

                    when {
                        activeSubscription != null -> Resource.Success(activeSubscription)
                        subscriptions.isNotEmpty() -> Resource.Success(subscriptions.first())
                        else -> Resource.Error("No active subscription found")
                    }
                } else {
                    Resource.Error(apiResponse?.message ?: "Failed to fetch subscriptions")
                }
            } else {
                Resource.Error("Failed to load subscriptions: ${response.code()}")
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Network error")
        }
    }

    override suspend fun createSubscription(
        shopId: String,
        request: CreateSubscriptionRequest
    ): Resource<SubscriptionResponse> {
        return try {
            val response = apiService.createSubscription(shopId, request)
            if (response.isSuccessful) {
                val apiResponse = response.body()
                if (apiResponse?.success == true) {
                    apiResponse.data?.let { Resource.Success(it) }
                        ?: Resource.Error("No data in response")
                } else {
                    Resource.Error(apiResponse?.message ?: "Failed to create subscription")
                }
            } else {
                Resource.Error("Error ${response.code()}: ${response.message()}")
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Network error")
        }
    }

    override suspend fun renewSubscription(
        shopId: String,
        request: RenewSubscriptionRequest
    ): Resource<SubscriptionResponse> {
        return try {
            val response = apiService.renewSubscription(shopId, request)
            if (response.isSuccessful) {
                val apiResponse = response.body()
                if (apiResponse?.success == true && apiResponse.data != null) {
                    Resource.Success(apiResponse.data)
                } else {
                    Resource.Error(apiResponse?.message ?: "Failed to renew subscription")
                }
            } else {
                Resource.Error("Network error: ${response.code()}")
            }
        } catch (e: IOException) {
            Resource.Error("Network error: ${e.message ?: "Check your internet connection"}")
        } catch (e: Exception) {
            Resource.Error("Failed to renew subscription: ${e.message ?: "Unknown error"}")
        }
    }

    override suspend fun checkShopSubscription(shopId: String): Resource<ShopSubscriptionStatusResponse> {
        return try {
            val response = apiService.checkShopSubscription(shopId)
            if (response.isSuccessful) {
                val apiResponse = response.body()
                if (apiResponse?.success == true) {
                    Resource.Success(
                        apiResponse.data ?: ShopSubscriptionStatusResponse(
                            success = true,
                            message = "Subscription check successful",
                            status = "inactive",
                            type = null,
                            expiryDate = null,
                            isActive = false,
                            daysRemaining = null,
                            canActivate = true,
                            shopId = shopId,
                            shopName = null,
                            subscriptionId = null,
                            subscription = null
                        )
                    )
                } else {
                    Resource.Error(apiResponse?.message ?: "Failed to check subscription")
                }
            } else {
                Resource.Error("Network error: ${response.code()}")
            }
        } catch (e: IOException) {
            Resource.Error("Network error: ${e.message ?: "Check your internet connection"}")
        } catch (e: Exception) {
            Resource.Error("Failed to check subscription: ${e.message ?: "Unknown error"}")
        }
    }

    override suspend fun cancelSubscription(
        shopId: String,
        subscriptionId: String
    ): Resource<Unit> {
        return try {
            val response = apiService.cancelSubscription(shopId, subscriptionId)
            if (response.isSuccessful) {
                Resource.Success(Unit)
            } else {
                Resource.Error("Network error: ${response.code()}")
            }
        } catch (e: IOException) {
            Resource.Error("Network error: ${e.message ?: "Check your internet connection"}")
        } catch (e: Exception) {
            Resource.Error("Failed to cancel subscription: ${e.message ?: "Unknown error"}")
        }
    }

    override suspend fun cancelPackage(
        shopId: String,
        subscriptionId: String,
        packageId: String
    ): Resource<Unit> {
        return try {
            val response = apiService.cancelPackage(shopId, subscriptionId, packageId)
            if (response.isSuccessful) {
                Resource.Success(Unit)
            } else {
                Resource.Error("Network error: ${response.code()}")
            }
        } catch (e: IOException) {
            Resource.Error("Network error: ${e.message ?: "Check your internet connection"}")
        } catch (e: Exception) {
            Resource.Error("Failed to cancel package: ${e.message ?: "Unknown error"}")
        }
    }

    override suspend fun getPaymentHistory(
        shopId: String,
        page: Int,
        limit: Int,
        provider: String?
    ): Resource<List<Payment>> {
        return try {
            val response = apiService.getSubscriptionPayments(shopId, page, limit, provider)
            if (response.isSuccessful) {
                val apiResponse = response.body()
                if (apiResponse?.success == true) {
                    val payments = (apiResponse.data ?: emptyList()).map { it.toPayment() }
                    Resource.Success(payments)
                } else {
                    Resource.Error(apiResponse?.message ?: "Failed to fetch payment history")
                }
            } else {
                Resource.Error("Failed to load payment history: ${response.code()}")
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to load payment history")
        }
    }

    private fun PaymentDto.toPayment(): Payment {
        return Payment(
            id = this.id,
            subscriptionId = this.subscriptionId ?: "",
            amount = this.amount,
            currency = this.currency ?: "UGX",
            status = this.status,
            phoneNumber = this.phoneNumber,
            transactionId = this.transactionId,
            paymentMethod = this.paymentMethod,
            paymentDate = this.paymentDate ?: this.createdAt,
            createdAt = this.createdAt,
            updatedAt = this.updatedAt
        )
    }

    override suspend fun getUserShopsSubscriptions(): Resource<List<Subscription>> {
        return try {
            val response = apiService.getUserShopsSubscriptions()
            if (response.isSuccessful) {
                val apiResponse = response.body()
                if (apiResponse?.success == true) {
                    Resource.Success(apiResponse.data ?: emptyList())
                } else {
                    Resource.Error(apiResponse?.message ?: "Failed to fetch subscriptions")
                }
            } else {
                Resource.Error("Network error: ${response.code()}")
            }
        } catch (e: IOException) {
            Resource.Error("Network error: ${e.message ?: "Check your internet connection"}")
        } catch (e: Exception) {
            Resource.Error("Failed to load subscriptions: ${e.message ?: "Unknown error"}")
        }
    }

    override suspend fun findSubscriptionByPaymentId(paymentId: String): Resource<PaymentCheckResponse> {
        return try {
            val response = apiService.findSubscriptionByPaymentId(paymentId)
            if (response.isSuccessful) {
                val apiResponse = response.body()
                if (apiResponse?.success == true) {
                    apiResponse.data?.let { Resource.Success(it) }
                        ?: Resource.Error("Subscription not found for this payment")
                } else {
                    Resource.Error(apiResponse?.message ?: "Failed to fetch subscription")
                }
            } else {
                Resource.Error("Network error: ${response.code()}")
            }
        } catch (e: IOException) {
            Resource.Error("Network error: ${e.message ?: "Check your internet connection"}")
        } catch (e: Exception) {
            Resource.Error("Failed to find subscription: ${e.message ?: "Unknown error"}")
        }
    }
}