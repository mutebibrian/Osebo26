package com.devbrian.osebo.data.remote.dto.response

import com.devbrian.osebo.models.Feature
import com.devbrian.osebo.models.SubscriptionPackage

fun fromSubscriptionPackage(pkg: SubscriptionPackage): PackageDto {
    return PackageDto(
        id = pkg.id,
        name = pkg.name,
        tier = pkg.tier,
        type = pkg.type,
        kind = pkg.kind,
        description = pkg.description,
        unitMonthlyAmount = pkg.unitMonthlyAmount,
        features = pkg.features.map { fromFeature(it) },
        isActive = pkg.isActive,
        canTry = pkg.canTry
    )
}

fun fromFeature(feature: Feature): FeatureDto {
    return FeatureDto(
        name = feature.name,
        included = feature.included,
        description = feature.description
    )
}
