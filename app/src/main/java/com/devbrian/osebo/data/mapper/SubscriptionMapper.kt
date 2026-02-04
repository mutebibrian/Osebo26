package com.devbrian.osebo.data.mapper

import com.devbrian.osebo.data.remote.api.SubscriptionHistoryDto
import com.devbrian.osebo.data.remote.api.SubscriptionPlanDto
import com.devbrian.osebo.data.remote.dto.response.SubscriptionDto
import com.devbrian.osebo.domain.model.Subscription
import com.devbrian.osebo.domain.model.SubscriptionPlan
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SubscriptionMapper @Inject constructor() {

    // For current subscription
    fun toDomain(dto: SubscriptionDto): Subscription {
        return Subscription(
            id = dto.id ?: "",
            shopId = dto.shopId ?: "",
            planId = dto.planId ?: "",
            planName = dto.planName ?: "Unknown Plan",
            planType = dto.planType ?: "basic",
            maxLimit = dto.maxLimit ?: "0",
            price = dto.price ?: 0.0,
            currency = dto.currency ?: "UGX",
            status = dto.status ?: "inactive",
            startDate = dto.startDate ?: "",
            endDate = dto.endDate ?: "",
            daysLeft = dto.daysLeft ?: calculateDaysLeft(dto.endDate),
            autoRenew = dto.autoRenew ?: false,
            paymentMethod = dto.paymentMethod ?: "",
            packageName = dto.planName ?: "Unknown Package",
            expires = dto.endDate ?: "N/A",
            maxItems = dto.maxItems ?: 0
        )
    }

    // For subscription history
    fun historyToDomain(dto: SubscriptionHistoryDto): Subscription {
        return Subscription(
            id = dto.id ?: "",
            shopId = dto.shopId ?: "",
            planId = dto.planId ?: "",
            planName = dto.planName ?: "Unknown Plan",
            planType = dto.planType ?: "basic",
            maxLimit = dto.maxLimit ?: "0",
            price = dto.price ?: 0.0,
            currency = dto.currency ?: "UGX",
            status = dto.status ?: "inactive",
            startDate = dto.startDate ?: "",
            endDate = dto.endDate ?: "",
            daysLeft = dto.daysLeft ?: calculateDaysLeft(dto.endDate),
            autoRenew = dto.autoRenew ?: false,
            paymentMethod = dto.paymentMethod ?: "",
            packageName = dto.planName ?: "Unknown Package",
            expires = dto.endDate ?: "N/A",
            maxItems = dto.maxItems ?: 0
        )
    }

    fun planToDomain(dto: SubscriptionPlanDto): SubscriptionPlan {
        val calculatedMaxItems = when (dto.type?.lowercase()) {
            "basic" -> 500
            "pro" -> 5000
            "enterprise" -> Int.MAX_VALUE
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

    private fun calculateDaysLeft(endDate: String?): Int {
        if (endDate.isNullOrBlank()) return 0

        return try {
            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            dateFormat.timeZone = TimeZone.getTimeZone("UTC")

            val end = dateFormat.parse(endDate)
            val now = Calendar.getInstance().apply {
                timeZone = TimeZone.getTimeZone("UTC")
            }.time

            if (end != null) {
                val diff = end.time - now.time
                val days = diff / (1000 * 60 * 60 * 24)
                days.toInt().coerceAtLeast(0)
            } else {
                0
            }
        } catch (e: Exception) {
            0
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