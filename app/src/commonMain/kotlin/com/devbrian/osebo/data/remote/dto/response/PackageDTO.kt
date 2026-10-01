package com.devbrian.osebo.data.remote.dto.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PackageDto(
    @SerialName("id")
    val id: String,

    @SerialName("name")
    val name: String,

    @SerialName("tier")
    val tier: String,

    @SerialName("type")
    val type: String? = null,

    @SerialName("kind")
    val kind: String = "base",

    @SerialName("description")
    val description: String,

    @SerialName("unit_monthly_amount")
    val unitMonthlyAmount: String,

    @SerialName("features")
    val features: List<FeatureDto>? = null,

    @SerialName("is_active")
    val isActive: Boolean,

    @SerialName("canTry")
    val canTry: Boolean = false
) {

    val monthlyAmount: Double
        get() = unitMonthlyAmount.toDoubleOrNull() ?: 0.0

    val formattedPrice: String
        get() = when {
            monthlyAmount > 0 -> "UGX ${String.format("%,.0f", monthlyAmount)}/month"
            else -> "Contact Sales"
        }

    // Custom pricing/contact-sales is now determined by price, not tier or kind —
    // "custom" kind packages (add-ons) still have real prices.
    val isCustomPlan: Boolean
        get() = monthlyAmount == 0.0

    val featureNames: List<String>
        get() = features?.map { it.name } ?: emptyList()

    val includedFeaturesCount: Int
        get() = features?.count { it.included } ?: 0

    val displayName: String
        get() = when (tier.lowercase()) {
            "basic" -> "Basic Plan"
            "pro" -> "Pro Plan"
            "premium" -> "Premium Plan"
            "enterprise" -> "Enterprise Plan"
            else -> name
        }

    companion object {
        fun createSample(): PackageDto {
            return PackageDto(
                id = "pkg_123",
                name = "Premium Package",
                tier = "premium",
                type = "monthly",
                kind = "base",
                description = "Complete solution for growing businesses",
                unitMonthlyAmount = "50000.00",
                features = listOf(
                    FeatureDto.createSample("Inventory Management", true, "Manage up to 1000 products"),
                    FeatureDto.createSample("Sales Reports", true, "Detailed sales analytics"),
                    FeatureDto.createSample("Customer Management", true, "Track customer history"),
                    FeatureDto.createSample("Multi-user Access", true, "Up to 5 users"),
                    FeatureDto.createSample("API Access", false, "REST API integration")
                ),
                isActive = true,
                canTry = true
            )
        }

        fun createBasic(): PackageDto {
            return PackageDto(
                id = "pkg_456",
                name = "Basic Package",
                tier = "basic",
                type = "monthly",
                kind = "base",
                description = "Essential features for small businesses",
                unitMonthlyAmount = "20000.00",
                features = listOf(
                    FeatureDto.createSample("Inventory Management", true, "Manage up to 100 products"),
                    FeatureDto.createSample("Sales Reports", true, "Basic sales reports"),
                    FeatureDto.createSample("Customer Management", true, "Basic customer info"),
                    FeatureDto.createSample("Multi-user Access", false, null),
                    FeatureDto.createSample("API Access", false, null)
                ),
                isActive = true,
                canTry = true
            )
        }

        fun createPro(): PackageDto {
            return PackageDto(
                id = "pkg_789",
                name = "Pro Package",
                tier = "pro",
                type = "monthly",
                kind = "base",
                description = "Advanced features for growing businesses",
                unitMonthlyAmount = "35000.00",
                features = listOf(
                    FeatureDto.createSample("Inventory Management", true, "Manage up to 500 products"),
                    FeatureDto.createSample("Sales Reports", true, "Advanced sales analytics"),
                    FeatureDto.createSample("Customer Management", true, "Customer history & insights"),
                    FeatureDto.createSample("Multi-user Access", true, "Up to 3 users"),
                    FeatureDto.createSample("API Access", true, "Basic API access")
                ),
                isActive = true,
                canTry = true
            )
        }

        fun createCustom(): PackageDto {
            return PackageDto(
                id = "pkg_000",
                name = "SMS notifications",
                tier = "custom",
                type = "custom",
                kind = "custom",
                description = "Send SMS to your customers",
                unitMonthlyAmount = "20000.00",
                features = null,
                isActive = true,
                canTry = true
            )
        }

        fun fromSubscriptionPackage(pkg: com.devbrian.osebo.models.SubscriptionPackage): PackageDto {
            return PackageDto(
                id = pkg.id,
                name = pkg.name,
                tier = pkg.tier,
                type = pkg.type,
                kind = pkg.kind,
                description = pkg.description,
                unitMonthlyAmount = pkg.unitMonthlyAmount,
                features = pkg.features.map { FeatureDto.fromFeature(it) },
                isActive = pkg.isActive,
                canTry = pkg.canTry
            )
        }
    }
}

@Serializable
data class FeatureDto(
    @SerialName("name")
    val name: String,

    @SerialName("included")
    val included: Boolean,

    @SerialName("description")
    val description: String?
) {

    val isIncluded: Boolean
        get() = included

    val displayText: String
        get() = if (included) "✓ $name" else "✗ $name"

    companion object {
        fun createSample(name: String = "Feature", included: Boolean = true, description: String? = null): FeatureDto {
            return FeatureDto(
                name = name,
                included = included,
                description = description
            )
        }

        fun fromFeature(feature: com.devbrian.osebo.models.Feature): FeatureDto {
            return FeatureDto(
                name = feature.name,
                included = feature.included,
                description = feature.description
            )
        }
    }
}