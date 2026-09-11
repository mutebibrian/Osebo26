package com.devbrian.osebo.utils

import android.content.Context
import com.devbrian.osebo.data.PreferenceManager
import com.devbrian.osebo.models.Subscription

object SubscriptionStatusManager {

    fun updateSubscriptionStatus(context: Context, subscription: Subscription) {
        val prefs = PreferenceManager.getInstance(context)

        prefs.saveSubscriptionStatus(
            if (subscription.isActiveStatus) "ACTIVE" else subscription.effectiveStatus
        )
        prefs.saveSubscriptionId(subscription.id)
        prefs.saveSubscriptionType(subscription.packageType)
        prefs.saveSubscriptionExpiry(subscription.endDate ?: "")
        prefs.savePackageId(subscription.actualPackageId)

        println("✅ Subscription status updated in preferences:")
        println("   - Status: ${subscription.effectiveStatus}")
        println("   - Active: ${subscription.isActiveStatus}")
        println("   - ID: ${subscription.id}")
        println("   - Package: ${subscription.packageType}")
        println("   - Expiry: ${subscription.endDate}")
    }

    fun activateSubscription(context: Context, shopId: String, subscription: Subscription) {
        val prefs = PreferenceManager.getInstance(context)

        prefs.saveCurrentShopId(shopId)
        prefs.saveCurrentShopUuid(shopId)
        prefs.saveSubscriptionStatus("ACTIVE")
        prefs.saveSubscriptionId(subscription.id)
        prefs.saveSubscriptionType(subscription.packageType)
        prefs.saveSubscriptionExpiry(subscription.endDate ?: "")
        prefs.savePackageId(subscription.actualPackageId)

        println("✅ Subscription ACTIVATED for shop: $shopId")
        prefs.debugSubscriptionInfo()
    }

    fun isSubscriptionActive(context: Context): Boolean {
        val prefs = PreferenceManager.getInstance(context)
        return prefs.hasActiveSubscription()
    }

    fun clearSubscriptionStatus(context: Context) {
        val prefs = PreferenceManager.getInstance(context)
        prefs.clearSubscriptionInfo()
        println("✅ Subscription status cleared")
    }
}
