package com.devbrian.osebo.data.mapper

import com.devbrian.osebo.data.remote.api.SubscriptionHistoryDto
import com.devbrian.osebo.data.remote.api.SubscriptionPlanDto
import com.devbrian.osebo.data.remote.dto.response.SubscriptionDto
import com.devbrian.osebo.domain.model.SubscriptionPlan
import com.devbrian.osebo.models.Subscription
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SubscriptionMapper @Inject constructor() {

    fun toDomain(dto: SubscriptionDto): Subscription {
        return Subscription(
            id = dto.id ?: "",
            shopId = dto.shop_id ?: "",
            packageType = dto.package_type ?: extractPackageTypeFromPlanId(dto.plan_id) ?: "BASIC",
            packageName = dto.package_name ?: extractPackageNameFromPlanId(dto.plan_id) ?: "Unknown Package",
            amount = dto.amount ?: dto.price ?: 0.0,
            currency = dto.currency ?: "UGX",
            phoneNumber = dto.phone_number,
            status = mapStatus(dto.status),
            months = calculateMonths(dto.start_date, dto.end_date),
            startDate = dto.start_date ?: "",
            endDate = dto.end_date ?: "",
            transactionId = dto.transaction_id,
            paymentMethod = dto.payment_method,
            isTrial = dto.is_trial ?: false,
            trialEndsAt = dto.trial_ends_at,
            autoRenew = dto.auto_renew ?: false,
            createdAt = dto.created_at ?: "",
            updatedAt = dto.updated_at ?: ""
        )
    }

    fun historyToDomain(dto: SubscriptionHistoryDto): Subscription {
        return Subscription(
            id = dto.id,
            shopId = dto.shopId ?: "",
            packageType = dto.planType ?: extractPackageTypeFromPlanId(dto.planId) ?: "BASIC",
            packageName = dto.planName ?: extractPackageNameFromPlanId(dto.planId) ?: "Unknown Package",
            amount = dto.amount,
            currency = dto.currency,
            phoneNumber = null,
            status = mapStatus(dto.status),
            months = calculateMonths(dto.startDate, dto.endDate),
            startDate = dto.startDate ?: dto.date ?: "",
            endDate = dto.endDate ?: "",
            transactionId = null,
            paymentMethod = dto.paymentMethod,
            isTrial = isTrialStatus(dto.status),
            trialEndsAt = null,
            autoRenew = dto.autoRenew ?: false,
            createdAt = dto.date ?: "",
            updatedAt = dto.date ?: ""
        )
    }

    fun planToDomain(dto: SubscriptionPlanDto): SubscriptionPlan {
        val calculatedMaxItems = when (dto.type?.uppercase()) {
            "BASIC" -> 500
            "PRO" -> 5000
            "ENTERPRISE", "POPULAR" -> Int.MAX_VALUE
            else -> 0
        }

        return SubscriptionPlan(
            id = dto.id ?: "",
            name = dto.name ?: "Unknown Plan",
            type = dto.type ?: "basic",
            price = formatPriceForDisplay(dto.price, dto.currency),
            currency = dto.currency ?: "UGX",
            originalPrice = dto.price ?: 0.0,
            description = dto.description ?: "",
            features = dto.features ?: emptyList(),
            isPopular = dto.isPopular ?: false,
            maxUsers = dto.maxUsers ?: 0,
            maxShops = dto.maxShops ?: 0,
            maxStorage = dto.maxStorage ?: "",
            billingCycle = dto.billingCycle ?: "monthly",
            duration = dto.duration ?: "30 days",
            maxItems = dto.maxItems ?: calculatedMaxItems
        )
    }

    
    private fun extractPackageTypeFromPlanId(planId: String?): String? {
        return when (planId?.uppercase()) {
            "BASIC", "PRO_001", "PLAN_001" -> "BASIC"
            "PRO", "PRO_002", "PLAN_002" -> "PRO"
            "ENTERPRISE", "POPULAR", "PRO_003", "PLAN_003" -> "POPULAR"
            else -> null
        }
    }

    private fun extractPackageNameFromPlanId(planId: String?): String? {
        return when (planId?.uppercase()) {
            "BASIC", "PRO_001", "PLAN_001" -> "Basic Plan"
            "PRO", "PRO_002", "PLAN_002" -> "Pro Plan"
            "ENTERPRISE", "POPULAR", "PRO_003", "PLAN_003" -> "Enterprise Plan"
            else -> null
        }
    }

    private fun isTrialStatus(status: String?): Boolean {
        return status?.uppercase() == "TRIAL"
    }

    private fun mapStatus(status: String?): String {
        return when (status?.uppercase()) {
            "ACTIVE", "SUCCESS", "COMPLETED" -> "ACTIVE"
            "INACTIVE", "FAILED", "EXPIRED" -> "EXPIRED"
            "PENDING", "PROCESSING" -> "PENDING"
            "TRIAL" -> "TRIAL"
            "CANCELLED", "CANCELED" -> "CANCELLED"
            else -> "PENDING"
        }
    }

    private fun calculateMonths(startDate: String?, endDate: String?): Int {
        if (startDate.isNullOrBlank() || endDate.isNullOrBlank()) return 1

        return try {
            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val start = dateFormat.parse(startDate)
            val end = dateFormat.parse(endDate)

            if (start != null && end != null) {
                val diff = end.time - start.time
                val days = diff / (1000 * 60 * 60 * 24)
                val months = days / 30
                months.toInt().coerceAtLeast(1)
            } else {
                1
            }
        } catch (e: Exception) {
            1
        }
    }

    private fun formatPriceForDisplay(price: Double?, currency: String?): String {
        return when {
            price == null || price == 0.0 -> "Free"
            currency.isNullOrEmpty() -> "%,.0f".format(price)
            else -> {
                val formattedPrice = "%,.0f".format(price)
                "$currency $formattedPrice"
            }
        }
    }

    fun toDomainList(dtos: List<SubscriptionDto>): List<Subscription> {
        return dtos.map { toDomain(it) }
    }

    fun historyToDomainList(dtos: List<SubscriptionHistoryDto>): List<Subscription> {
        return dtos.map { historyToDomain(it) }
    }

    fun planToDomainList(dtos: List<SubscriptionPlanDto>): List<SubscriptionPlan> {
        return dtos.map { planToDomain(it) }
    }
}

