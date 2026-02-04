package com.devbrian.osebo.data.repository

import com.devbrian.osebo.data.mapper.SubscriptionMapper
import com.devbrian.osebo.data.remote.api.OseboApiService
import com.devbrian.osebo.data.remote.dto.request.SubscribeRequest
import com.devbrian.osebo.domain.model.Subscription
import com.devbrian.osebo.domain.model.SubscriptionPlan
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

interface SubscriptionRepository {
    suspend fun getCurrentSubscription(): Subscription?
    suspend fun getSubscriptionPlans(): List<SubscriptionPlan>
    suspend fun subscribeToPlan(planId: String, paymentMethod: String): Subscription
    suspend fun cancelSubscription()
    suspend fun getSubscriptionHistory(): List<Subscription>
    suspend fun updateAutoRenew(autoRenew: Boolean): Boolean
}

@Singleton
class SubscriptionRepositoryImpl @Inject constructor(
    private val apiService: OseboApiService,
    private val mapper: SubscriptionMapper
) : SubscriptionRepository {

    override suspend fun getCurrentSubscription(): Subscription? {
        return try {
            val response = apiService.getCurrentSubscription()
            if (response.isSuccessful && response.body() != null) {
                mapper.toDomain(response.body()!!) // Uses SubscriptionDto
            } else {
                println("Failed to get current subscription: ${response.code()} - ${response.message()}")
                getMockCurrentSubscription()
            }
        } catch (e: Exception) {
            println("Error getting current subscription: ${e.message}")
            getMockCurrentSubscription()
        }
    }

    override suspend fun getSubscriptionHistory(): List<Subscription> {
        return try {
            val response = apiService.getSubscriptionHistory()
            if (response.isSuccessful && response.body() != null) {
                mapper.historyToDomainList(response.body()!!) // Uses SubscriptionHistoryDto
            } else {
                println("Failed to get subscription history: ${response.code()} - ${response.message()}")
                getMockSubscriptionHistory()
            }
        } catch (e: Exception) {
            println("Error getting subscription history: ${e.message}")
            getMockSubscriptionHistory()
        }
    }

    override suspend fun getSubscriptionPlans(): List<SubscriptionPlan> {
        return try {
            val response = apiService.getSubscriptionPlans()
            if (response.isSuccessful && response.body() != null) {
                mapper.planToDomainList(response.body()!!)
            } else {
                Timber.w("Failed to get subscription plans: ${response.code()} - ${response.message()}")
                getMockPlans() // Fallback to mock data
            }
        } catch (e: Exception) {
            Timber.e(e, "Error getting subscription plans")
            getMockPlans()
        }
    }

    override suspend fun subscribeToPlan(planId: String, paymentMethod: String): Subscription {
        return try {
            val request = SubscribeRequest(
                planId = planId,
                paymentMethod = paymentMethod,
                autoRenew = true
            )

            val response = apiService.subscribeToPlan(request)
            if (response.isSuccessful && response.body() != null) {
                mapper.toDomain(response.body()!!)
            } else {
                val errorMessage = "Failed to subscribe to plan: ${response.code()} - ${response.message()}"
                Timber.e(errorMessage)
                throw Exception(errorMessage)
            }
        } catch (e: Exception) {
            Timber.e(e, "Error subscribing to plan")
            throw Exception("Failed to subscribe to plan: ${e.message}")
        }
    }

    override suspend fun cancelSubscription() {
        try {
            val response = apiService.cancelSubscription()
            if (!response.isSuccessful) {
                val errorMessage = "Failed to cancel subscription: ${response.code()} - ${response.message()}"
                Timber.e(errorMessage)
                throw Exception(errorMessage)
            }
            Timber.i("Subscription cancelled successfully")
        } catch (e: Exception) {
            Timber.e(e, "Error cancelling subscription")
            throw Exception("Failed to cancel subscription: ${e.message}")
        }
    }



    override suspend fun updateAutoRenew(autoRenew: Boolean): Boolean {
        return try {
            val request = com.devbrian.osebo.data.remote.dto.request.AutoRenewRequest(autoRenew = autoRenew)
            val response = apiService.updateAutoRenew(request)
            if (response.isSuccessful) {
                Timber.i("Auto-renew updated to: $autoRenew")
                true
            } else {
                Timber.e("Failed to update auto-renew: ${response.code()} - ${response.message()}")
                false
            }
        } catch (e: Exception) {
            Timber.e(e, "Error updating auto-renew")
            false
        }
    }

    // Mock data methods
    private fun getMockCurrentSubscription(): Subscription? {
        return Subscription(
            id = "sub_123",
            shopId = "shop_456",
            planId = "2",
            planName = "Pro Plan",
            planType = "pro",
            maxLimit = "Unlimited Items",
            price = 150000.0,
            currency = "UGX",
            status = "active",
            startDate = "2024-01-01",
            endDate = "2024-12-31",
            daysLeft = 45,
            autoRenew = true,
            paymentMethod = "Mobile Money",
            packageName = "Pro Plan",
            expires = "2024-12-31",
            maxItems = 1000
        )
    }

    private fun getMockPlans(): List<SubscriptionPlan> {
        return listOf(
            SubscriptionPlan(
                id = "1",
                name = "Basic",
                type = "basic",
                price = "UGX 50,000",
                currency = "UGX",
                originalPrice = 50000.0,
                description = "Perfect for small businesses just starting out",
                features = listOf(
                    "✅ Up to 500 inventory items",
                    "✅ Basic sales tracking",
                    "✅ Email support",
                    "✅ Up to 3 shops",
                    "✅ 1 user account",
                    "❌ Advanced analytics",
                    "❌ API access"
                ),
                isPopular = false,
                maxUsers = 1,
                maxShops = 3,
                maxStorage = "5GB",
                billingCycle = "monthly",
                duration = "30 days",
                maxItems = 500
            ),
            SubscriptionPlan(
                id = "2",
                name = "Pro",
                type = "pro",
                price = "UGX 150,000",
                currency = "UGX",
                originalPrice = 150000.0,
                description = "For growing businesses with multiple shops",
                features = listOf(
                    "✅ Everything in Basic",
                    "✅ Up to 5,000 inventory items",
                    "✅ Advanced analytics & reports",
                    "✅ Priority email & chat support",
                    "✅ Up to 10 shops",
                    "✅ Up to 5 user accounts",
                    "✅ Basic API access"
                ),
                isPopular = true,
                maxUsers = 5,
                maxShops = 10,
                maxStorage = "20GB",
                billingCycle = "monthly",
                duration = "30 days",
                maxItems = 5000
            ),
            SubscriptionPlan(
                id = "3",
                name = "Enterprise",
                type = "enterprise",
                price = "UGX 300,000",
                currency = "UGX",
                originalPrice = 300000.0,
                description = "For large enterprises with custom needs",
                features = listOf(
                    "✅ Everything in Pro",
                    "✅ Unlimited inventory items",
                    "✅ Custom analytics & reports",
                    "✅ 24/7 phone & dedicated support",
                    "✅ Unlimited shops",
                    "✅ Unlimited user accounts",
                    "✅ Full API access",
                    "✅ Custom integrations",
                    "✅ White labeling"
                ),
                isPopular = false,
                maxUsers = Int.MAX_VALUE,
                maxShops = Int.MAX_VALUE,
                maxStorage = "100GB",
                billingCycle = "monthly",
                duration = "30 days",
                maxItems = Int.MAX_VALUE
            )
        )
    }

    private fun getMockSubscriptionHistory(): List<Subscription> {
        return listOf(
            Subscription(
                id = "sub_001",
                shopId = "shop_456",
                planId = "1",
                planName = "Basic Plan",
                planType = "basic",
                maxLimit = "500 Items",
                price = 50000.0,
                currency = "UGX",
                status = "completed",
                startDate = "2023-06-01",
                endDate = "2023-12-31",
                daysLeft = 0,
                autoRenew = false,
                paymentMethod = "Mobile Money",
                packageName = "Basic Plan",
                expires = "2023-12-31",
                maxItems = 500
            ),
            Subscription(
                id = "sub_002",
                shopId = "shop_456",
                planId = "2",
                planName = "Pro Plan",
                planType = "pro",
                maxLimit = "5000 Items",
                price = 150000.0,
                currency = "UGX",
                status = "active",
                startDate = "2024-01-01",
                endDate = "2024-12-31",
                daysLeft = 45,
                autoRenew = true,
                paymentMethod = "Mobile Money",
                packageName = "Pro Plan",
                expires = "2024-12-31",
                maxItems = 5000
            )
        )
    }
}