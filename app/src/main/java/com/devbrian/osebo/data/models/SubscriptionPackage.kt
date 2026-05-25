package com.devbrian.osebo.models

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize
import java.io.Serializable

@Parcelize
data class SubscriptionPackage(
    @SerializedName("id")
    val id: String = "",

    @SerializedName("name")
    val name: String = "",

    @SerializedName("tier")
    val tier: String = "",

    @SerializedName("type")
    val type: String = "monthly",

    @SerializedName("description")
    val description: String = "",

    @SerializedName("unit_monthly_amount")
    val unitMonthlyAmount: String = "0.00",

    @SerializedName("features")
    val features: List<Feature> = emptyList(),

    @SerializedName("is_active")
    val isActive: Boolean = true,

    @SerializedName("code")
    val code: String? = null,

    @SerializedName("display_name")
    val displayNameFromApi: String? = null,

    @SerializedName("display_price")
    val displayPriceFromApi: String? = null,

    @SerializedName("billing_frequency")
    val billingFrequency: String = "monthly",

    @SerializedName("billing_period")
    val billingPeriod: Int = 1,

    @SerializedName("billing_interval")
    val billingInterval: String = "month",

    @SerializedName("trial_period_days")
    val trialPeriodDays: Int = 0,

    @SerializedName("has_trial")
    val hasTrial: Boolean = false,

    @SerializedName("is_popular")
    val isPopularFromApi: Boolean = false,

    @SerializedName("is_custom")
    val isCustomFromApi: Boolean = false,

    @SerializedName("max_employees")
    val maxEmployees: Int = 0,

    @SerializedName("max_products")
    val maxProducts: Int = 0,

    @SerializedName("max_customers")
    val maxCustomers: Int = 0,

    @SerializedName("max_shops")
    val maxShops: Int = 1,

    @SerializedName("max_storage_mb")
    val maxStorageMb: Int = 0,

    @SerializedName("max_transactions")
    val maxTransactions: Int = 0,

    @SerializedName("support_level")
    val supportLevel: String = "basic",

    @SerializedName("api_access")
    val apiAccess: Boolean = false,

    @SerializedName("analytics")
    val analytics: Boolean = false,

    @SerializedName("pos_integration")
    val posIntegration: Boolean = false,

    @SerializedName("ecommerce")
    val ecommerce: Boolean = false,

    @SerializedName("inventory_management")
    val inventoryManagement: Boolean = true,

    @SerializedName("multi_currency")
    val multiCurrency: Boolean = false,

    @SerializedName("multi_language")
    val multiLanguage: Boolean = false,

    @SerializedName("white_label")
    val whiteLabel: Boolean = false,

    @SerializedName("dedicated_support")
    val dedicatedSupport: Boolean = false,

    @SerializedName("onboarding_support")
    val onboardingSupport: Boolean = false,

    @SerializedName("created_at")
    val createdAt: String? = null,

    @SerializedName("updated_at")
    val updatedAt: String? = null
) : Parcelable, Serializable {

    val displayName: String
        get() = when {
            !displayNameFromApi.isNullOrBlank() -> displayNameFromApi
            !name.isNullOrBlank() -> name
            else -> when (tier.lowercase()) {
                "basic" -> "Basic Plan"
                "pro" -> "Pro Plan"
                "premium" -> "Premium Plan"
                "enterprise" -> "Enterprise Plan"
                "custom" -> "Enterprise Plan"
                else -> "Subscription Plan"
            }
        }

    val price: Double
        get() = unitMonthlyAmount.toDoubleOrNull() ?: 0.0

    val currency: String
        get() = "UGX"

    val displayPrice: String
        get() = when {
            isCustom -> "Custom Pricing"
            price <= 0 -> "Free"
            displayPriceFromApi?.isNotBlank() == true -> displayPriceFromApi
            else -> "$currency ${String.format("%,.0f", price)}/month"
        }

    val isPopular: Boolean
        get() = isPopularFromApi || tier.lowercase() == "pro"

    val isCustom: Boolean
        get() = isCustomFromApi || tier.lowercase() == "custom"

    val hasFreeTrial: Boolean
        get() = hasTrial || trialPeriodDays > 0

    val freeTrialDays: Int
        get() = if (trialPeriodDays > 0) trialPeriodDays else 30

    val featureList: List<String>
        get() = if (features.isNotEmpty()) {
            features.filter { it.included }.map { it.name }
        } else {
            buildList {
                add("✓ Up to $maxEmployees employees")
                add("✓ Up to $maxProducts products")
                add("✓ Up to $maxCustomers customers")
                add("✓ ${supportLevel.replaceFirstChar { it.uppercase() }} Support")
                add("✓ Inventory Management")

                if (posIntegration) add("✓ POS Integration")
                if (ecommerce) add("✓ E-commerce Integration")
                if (analytics) add("✓ Advanced Analytics")
                if (apiAccess) add("✓ API Access")
                if (multiCurrency) add("✓ Multi-Currency")
                if (multiLanguage) add("✓ Multi-Language")
                if (whiteLabel) add("✓ White Label")
                if (dedicatedSupport) add("✓ Dedicated Account Manager")
                if (onboardingSupport) add("✓ Onboarding & Training")
                if (maxStorageMb > 0) add("✓ ${maxStorageMb}MB Storage")
                if (maxTransactions > 0) add("✓ Up to $maxTransactions transactions/month")
            }
        }

    fun getAnnualPrice(discountPercentage: Int = 10): Double {
        return price * 12 * (100 - discountPercentage) / 100
    }

    fun getQuarterlyPrice(discountPercentage: Int = 5): Double {
        return price * 3 * (100 - discountPercentage) / 100
    }

    fun isSuitableForShop(employeeCount: Int, productCount: Int): Boolean {
        return if (maxEmployees > 0 && employeeCount > maxEmployees) false
        else if (maxProducts > 0 && productCount > maxProducts) false
        else true
    }

    companion object {
        val EMPTY = SubscriptionPackage()

        val SAMPLE_BASIC = SubscriptionPackage(
            id = "basic_123",
            name = "Basic",
            tier = "basic",
            description = "Perfect for small businesses just getting started",
            unitMonthlyAmount = "50000",
            isPopularFromApi = false,
            isCustomFromApi = false,
            maxEmployees = 5,
            maxProducts = 100,
            maxCustomers = 200,
            supportLevel = "email"
        )

        val SAMPLE_PRO = SubscriptionPackage(
            id = "pro_123",
            name = "Pro",
            tier = "pro",
            description = "Advanced features for growing businesses",
            unitMonthlyAmount = "100000",
            isPopularFromApi = true,
            isCustomFromApi = false,
            maxEmployees = 20,
            maxProducts = 500,
            maxCustomers = 1000,
            supportLevel = "priority",
            posIntegration = true,
            analytics = true,
            apiAccess = true
        )

        val SAMPLE_ENTERPRISE = SubscriptionPackage(
            id = "enterprise_123",
            name = "Enterprise",
            tier = "custom",
            description = "Custom solutions for large organizations",
            unitMonthlyAmount = "0",
            isPopularFromApi = false,
            isCustomFromApi = true,
            maxEmployees = -1,
            maxProducts = -1,
            maxCustomers = -1,
            supportLevel = "dedicated",
            posIntegration = true,
            ecommerce = true,
            analytics = true,
            apiAccess = true,
            multiCurrency = true,
            multiLanguage = true,
            whiteLabel = true,
            dedicatedSupport = true,
            onboardingSupport = true
        )

        fun fromTier(tier: String): SubscriptionPackage {
            return when (tier.lowercase()) {
                "basic" -> SAMPLE_BASIC.copy(id = "tier_basic", tier = "basic")
                "pro" -> SAMPLE_PRO.copy(id = "tier_pro", tier = "pro")
                "enterprise", "custom" -> SAMPLE_ENTERPRISE.copy(id = "tier_enterprise", tier = "custom")
                else -> EMPTY
            }
        }
    }
}


@Parcelize
data class Feature(
    @SerializedName("id")
    val id: String? = null,

    @SerializedName("name")
    val name: String = "",

    @SerializedName("included")
    val included: Boolean = false,

    @SerializedName("description")
    val description: String = "",

    @SerializedName("icon")
    val icon: String? = null,

    @SerializedName("display_order")
    val displayOrder: Int = 0,

    @SerializedName("category")
    val category: String? = null
) : Parcelable, Serializable
