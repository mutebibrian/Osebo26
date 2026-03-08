package com.devbrian.osebo.data.repository

import com.devbrian.osebo.data.ApiService
import com.devbrian.osebo.data.remote.dto.request.ActivateTrialRequest
import com.devbrian.osebo.data.remote.dto.request.CheckPaymentStatusRequest
import com.devbrian.osebo.data.remote.dto.request.CreateSubscriptionRequest
import com.devbrian.osebo.data.remote.dto.request.InitiatePaymentRequest
import com.devbrian.osebo.data.remote.dto.response.FeatureDto
import com.devbrian.osebo.data.remote.dto.response.InitiatePaymentResponse
import com.devbrian.osebo.data.remote.dto.response.PackageDto
import com.devbrian.osebo.data.remote.dto.response.PaymentDto
import com.devbrian.osebo.data.remote.dto.response.PaymentPollResponse
import com.devbrian.osebo.data.remote.dto.response.PaymentStatusResponse
import com.devbrian.osebo.data.remote.dto.response.ShopDto
import com.devbrian.osebo.data.remote.dto.response.ShopSubscriptionDto
import com.devbrian.osebo.data.remote.dto.response.ShopSubscriptionStatusResponse
import com.devbrian.osebo.data.remote.dto.response.SubscriptionResponse
import com.devbrian.osebo.models.*
import com.devbrian.osebo.utils.Resource
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SubscriptionRepositoryImpl @Inject constructor(
    private val apiService: ApiService
) : SubscriptionRepository {

    
    override suspend fun getShops(): Resource<List<Shop>> {
        return try {
            println("🔍 ========== GET SHOPS START ==========")
            println("🔍 API CALL: Fetching shops...")
            val response = apiService.getShops()
            println("🔍 API Response code: ${response.code()}")

            if (response.isSuccessful) {
                val apiResponse = response.body()
                println("🔍 API Response success: ${apiResponse?.success}")

                if (apiResponse?.success == true) {
                    val shopDtos = apiResponse.data ?: emptyList()
                    println("🔍 Number of shops from API: ${shopDtos.size}")

                    
                    shopDtos.forEachIndexed { index, dto ->
                        println("🔍 Shop DTO $index - ID: ${dto.id}, Name: ${dto.name}")
                        println("🔍 Shop DTO $index - Subscription present: ${dto.subscription != null}")
                    }

                    
                    val shops = shopDtos.map { dto ->
                        dto.toShop()
                    }

                    println("🔍 Mapped ${shops.size} shops")
                    if (shops.isNotEmpty()) {
                        println("🔍 First mapped shop - subscription present: ${shops.first().subscription != null}")
                    }

                    println("🔍 ========== GET SHOPS END ==========")
                    Resource.Success(shops)
                } else {
                    Resource.Error(apiResponse?.message ?: "Failed to fetch shops")
                }
            } else {
                Resource.Error("Network error: ${response.code()}")
            }
        } catch (e: IOException) {
            println("🔍 IOException: ${e.message}")
            Resource.Error("Network error: ${e.message ?: "Check your internet connection"}")
        } catch (e: Exception) {
            println("🔍 Exception: ${e.message}")
            e.printStackTrace()
            Resource.Error(e.message ?: "Unknown error")
        }
    }
    
    private fun ShopDto.toShop(): Shop {
        println("🔍 toShop() called for: ${this.name}")
        println("🔍 toShop() - subscription DTO present: ${this.subscription != null}")

        if (this.subscription != null) {
            println("🔍 toShop() - subscription DTO id: ${this.subscription.id}")
            println("🔍 toShop() - subscription DTO status: ${this.subscription.status}")
        }

        val subscription = this.subscription?.toShopSubscription()
        println("🔍 toShop() - mapped subscription: $subscription")

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
        println("🔍 toShopSubscription() called for id: ${this.id}")
        println("🔍 toShopSubscription() - status: ${this.status}")
        println("🔍 toShopSubscription() - isTrial: ${this.isTrial}")

        val subscriptionPackage = this.packageDetails?.toSubscriptionPackage()
        println("🔍 toShopSubscription() - mapped package: $subscriptionPackage")

        return ShopSubscription(
            id = this.id,
            status = this.status,
            packageType = this.packageType,
            subscriptionPackage = subscriptionPackage,
            startsAt = this.startsAt,
            endsAt = this.endsAt,
            isActive = this.isActive,
            durationDays = this.durationDays,
            isTrial = this.isTrial,
            payment = null
        )
    }

    private fun PackageDto.toSubscriptionPackage(): SubscriptionPackage {
        println("🔍 toSubscriptionPackage() called for: ${this.name}")
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

    
    override suspend fun checkPaymentStatus(
        paymentId: String,
        request: CheckPaymentStatusRequest
    ): Resource<PaymentStatusResponse> {
        return try {
            val response = apiService.checkPaymentStatus(paymentId, request)
            if (response.isSuccessful) {
                val apiResponse = response.body()
                if (apiResponse?.success == true && apiResponse.data != null) {
                    Resource.Success(apiResponse.data)
                } else {
                    Resource.Error(apiResponse?.message ?: "Failed to check payment status")
                }
            } else {
                Resource.Error("Network error: ${response.code()}")
            }
        } catch (e: IOException) {
            Resource.Error("Network error: ${e.message ?: "Check your internet connection"}")
        } catch (e: Exception) {
            Resource.Error("Failed to check payment: ${e.message ?: "Unknown error"}")
        }
    }

    
    override suspend fun getSubscriptionDetails(
        shopId: String,
        subscriptionId: String
    ): Resource<Subscription> {
        return try {
            val response = apiService.getSubscriptionDetails(shopId, subscriptionId)
            if (response.isSuccessful) {
                val apiResponse = response.body()
                if (apiResponse?.success == true) {
                    apiResponse.data?.let {
                        Resource.Success(it)
                    } ?: Resource.Error("Subscription not found")
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
            println("🔍 Fetching subscriptions for shop: $shopId")
            val response = apiService.getShopSubscriptions(shopId)
            println("🔍 Response code: ${response.code()}")

            if (response.isSuccessful) {
                val apiResponse = response.body()
                println("🔍 API Response success: ${apiResponse?.success}")

                if (apiResponse?.success == true) {
                    val subscriptions = apiResponse.data ?: emptyList()
                    println("🔍 Found ${subscriptions.size} subscriptions")

                    // Log each subscription for debugging
                    subscriptions.forEachIndexed { index, sub ->
                        println("🔍 Subscription $index:")
                        println("   - id: ${sub.id}")
                        println("   - status: ${sub.status}")
                        println("   - packageType: ${sub.packageType}")
                        println("   - isTrial: ${sub.isTrial}")
                        println("   - startDate: ${sub.startDate}")
                        println("   - endDate: ${sub.endDate}")
                        println("   - isActive: ${sub.isActive}")
                    }

                    // Find active subscription (ACTIVE or TRIAL)
                    val activeSubscription = subscriptions.firstOrNull {
                        it.status.equals("ACTIVE", ignoreCase = true) ||
                                it.isTrial == true ||
                                it.status.equals("TRIAL", ignoreCase = true)
                    }

                    if (activeSubscription != null) {
                        println("✅ Active subscription found: ${activeSubscription.id}")
                        Resource.Success(activeSubscription)
                    } else {
                        println("⚠️ No active subscription found")
                        // Return the most recent subscription if any
                        if (subscriptions.isNotEmpty()) {
                            println("⚠️ Returning most recent subscription instead")
                            Resource.Success(subscriptions.first())
                        } else {
                            Resource.Error("No active subscription found")
                        }
                    }
                } else {
                    val errorMsg = apiResponse?.message ?: "Failed to fetch subscriptions"
                    println("❌ API Error: $errorMsg")
                    Resource.Error(errorMsg)
                }
            } else {
                println("❌ HTTP Error ${response.code()}: ${response.errorBody()?.string()}")
                Resource.Error("Failed to load subscriptions: ${response.code()}")
            }
        } catch (e: Exception) {
            println("❌ Exception: ${e.message}")
            e.printStackTrace()
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
                if (apiResponse?.success == true && apiResponse.data != null) {
                    Resource.Success(apiResponse.data)
                } else {
                    Resource.Error(apiResponse?.message ?: "Failed to create subscription")
                }
            } else {
                Resource.Error("Network error: ${response.code()}")
            }
        } catch (e: IOException) {
            Resource.Error("Network error: ${e.message ?: "Check your internet connection"}")
        } catch (e: Exception) {
            Resource.Error("Failed to create subscription: ${e.message ?: "Unknown error"}")
        }
    }

    
    override suspend fun initiatePayment(
        shopId: String,
        request: InitiatePaymentRequest
    ): Resource<InitiatePaymentResponse> {
        return try {
            val response = apiService.initiatePayment(shopId, request)
            if (response.isSuccessful) {
                val apiResponse = response.body()
                if (apiResponse?.success == true && apiResponse.data != null) {
                    Resource.Success(apiResponse.data)
                } else {
                    Resource.Error(apiResponse?.message ?: "Failed to initiate payment")
                }
            } else {
                Resource.Error("Network error: ${response.code()}")
            }
        } catch (e: IOException) {
            Resource.Error("Network error: ${e.message ?: "Check your internet connection"}")
        } catch (e: Exception) {
            Resource.Error("Failed to initiate payment: ${e.message ?: "Unknown error"}")
        }
    }


    override suspend fun getPaymentStatus(
        shopId: String,
        transactionId: String
    ): Resource<PaymentStatusResponse> {
        return try {
            val response = apiService.getPaymentStatus(shopId, transactionId)
            if (response.isSuccessful) {
                val apiResponse = response.body()
                if (apiResponse?.success == true && apiResponse.data != null) {
                    Resource.Success(apiResponse.data)
                } else {
                    Resource.Error(apiResponse?.message ?: "Failed to get payment status")
                }
            } else if (response.code() == 404) {
                Resource.Error("Payment not found")
            } else {
                Resource.Error("Network error: ${response.code()}")
            }
        } catch (e: IOException) {
            Resource.Error("Network error: ${e.message ?: "Check your internet connection"}")
        } catch (e: Exception) {
            Resource.Error("Failed to get payment status: ${e.message ?: "Unknown error"}")
        }
    }

    
    override suspend fun pollPaymentStatus(
        shopId: String,
        request: PollPaymentStatusRequest
    ): Resource<PaymentPollResponse> {
        return try {
            val response = apiService.pollPaymentStatus(shopId, request)
            if (response.isSuccessful) {
                val apiResponse = response.body()
                if (apiResponse?.success == true) {
                    Resource.Success(apiResponse.data ?: PaymentPollResponse(
                        success = true,
                        message = "Payment pending",
                        status = "pending",
                        nextPollSeconds = 5,
                        transactionId = request.transactionId,
                        amount = null,
                        currency = null,
                        paymentMethod = null,
                        paymentDate = null,
                        data = null
                    ))
                } else {
                    Resource.Error(apiResponse?.message ?: "Failed to poll payment status")
                }
            } else {
                Resource.Error("Network error: ${response.code()}")
            }
        } catch (e: IOException) {
            Resource.Error("Network error: ${e.message ?: "Check your internet connection"}")
        } catch (e: Exception) {
            Resource.Error("Failed to poll payment: ${e.message ?: "Unknown error"}")
        }
    }


    override suspend fun getPaymentHistory(
        shopId: String,
        subscriptionId: String
    ): Resource<List<Payment>> {
        return try {
            // Don't make API call if subscriptionId is empty
            if (subscriptionId.isEmpty()) {
                println("⚠️ Cannot fetch payment history - subscriptionId is empty")
                return Resource.Success(emptyList())
            }

            println("🔍 Fetching payment history for subscription: $subscriptionId")

            // First, get the subscription details which contain payment info
            val response = apiService.getSubscriptionDetails(shopId, subscriptionId)

            if (response.isSuccessful) {
                val apiResponse = response.body()
                if (apiResponse?.success == true) {
                    val subscription = apiResponse.data

                    // Extract payment from subscription if it exists
                    val payments = mutableListOf<Payment>()

                    subscription?.payment?.let { payment ->
                        payments.add(payment)
                    }

                    println("✅ Found ${payments.size} payments from subscription")
                    Resource.Success(payments)
                } else {
                    Resource.Error(apiResponse?.message ?: "Failed to fetch subscription details")
                }
            } else {
                println("❌ Error ${response.code()}: ${response.errorBody()?.string()}")
                Resource.Error("Failed to load payment history: ${response.code()}")
            }
        } catch (e: Exception) {
            println("❌ Exception: ${e.message}")
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

    
    override suspend fun checkShopSubscription(shopId: String): Resource<ShopSubscriptionStatusResponse> {
        return try {
            val response = apiService.checkShopSubscription(shopId)
            if (response.isSuccessful) {
                val apiResponse = response.body()
                if (apiResponse?.success == true) {
                    Resource.Success(apiResponse.data ?: ShopSubscriptionStatusResponse(
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
                    ))
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
                val apiResponse = response.body()
                if (apiResponse?.success == true) {
                    Resource.Success(Unit)
                } else {
                    Resource.Error(apiResponse?.message ?: "Failed to cancel subscription")
                }
            } else {
                Resource.Error("Network error: ${response.code()}")
            }
        } catch (e: IOException) {
            Resource.Error("Network error: ${e.message ?: "Check your internet connection"}")
        } catch (e: Exception) {
            Resource.Error("Failed to cancel subscription: ${e.message ?: "Unknown error"}")
        }
    }

    
    override suspend fun renewSubscription(
        shopId: String,
        subscriptionId: String,
        request: RenewSubscriptionRequest
    ): Resource<SubscriptionResponse> {
        return try {
            val response = apiService.renewSubscription(shopId, subscriptionId, request)
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

    
    override suspend fun activateFreeTrial(
        shopId: String,
        packageId: String
    ): Resource<SubscriptionResponse> {
        return try {
            val packagesResult = getSubscriptionPackages()

            return when (packagesResult) {
                is Resource.Success -> {
                    val selectedPackage = packagesResult.data.find { it.id == packageId }

                    if (selectedPackage == null) {
                        return Resource.Error("Package not found")
                    }

                    val tier = selectedPackage.tier

                    val request = ActivateTrialRequest(
                        shopId = shopId,
                        tier = tier,
                        packageId = packageId
                    )

                    val response = apiService.activateFreeTrial(shopId, shopId, request)

                    if (response.isSuccessful) {
                        val apiResponse = response.body()
                        if (apiResponse?.success == true && apiResponse.data != null) {
                            Resource.Success(apiResponse.data)
                        } else {
                            Resource.Error(apiResponse?.message ?: "Failed to activate trial")
                        }
                    } else {
                        Resource.Error("Network error: ${response.code()}")
                    }
                }
                is Resource.Error -> {
                    Resource.Error(packagesResult.message)
                }
                is Resource.Loading -> {
                    Resource.Error("Please try again")
                }
            }
        } catch (e: IOException) {
            Resource.Error("Network error: ${e.message ?: "Check your internet connection"}")
        } catch (e: Exception) {
            Resource.Error("Failed to activate trial: ${e.message ?: "Unknown error"}")
        }
    }
}

