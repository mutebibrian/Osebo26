package com.devbrian.osebo

import com.devbrian.osebo.data.remote.dto.response.ShopPackageSubscriptionDto
import com.devbrian.osebo.data.remote.dto.response.ShopSubscriptionDto
import com.devbrian.osebo.data.remote.dto.response.ShopSubscriptionStatusResponse
import com.devbrian.osebo.models.Subscription
import com.google.gson.Gson
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SubscriptionStatusResolutionTest {
    @Test
    fun camelCaseActiveResponse_isRecognized() {
        val response = Gson().fromJson(
            """
                {
                  "success": true,
                  "status": "active",
                  "isActive": true,
                  "subscriptionId": "subscription-id",
                  "expiryDate": "2026-12-31"
                }
            """.trimIndent(),
            ShopSubscriptionStatusResponse::class.java,
        )

        assertTrue(response.resolvedIsActive)
        assertEquals("ACTIVE", response.resolvedStatus)
        assertEquals("subscription-id", response.resolvedSubscriptionId)
        assertEquals("2026-12-31", response.resolvedExpiry)
    }

    @Test
    fun nestedTrialResponse_isRecognized() {
        val response = ShopSubscriptionStatusResponse(
            subscription = Subscription(
                id = "trial-id",
                status = "TRIAL",
                isTrial = true,
                trialEndsAt = "2026-10-10",
            ),
        )

        assertTrue(response.resolvedIsActive)
        assertEquals("TRIAL", response.resolvedStatus)
        assertEquals("trial-id", response.resolvedSubscriptionId)
        assertEquals("2026-10-10", response.resolvedExpiry)
    }

    @Test
    fun explicitInactiveResponse_remainsInactive() {
        val response = ShopSubscriptionStatusResponse(status = "inactive")

        assertFalse(response.resolvedIsActive)
        assertEquals("INACTIVE", response.resolvedStatus)
    }

    @Test
    fun liveActivePackagesResponse_isRecognizedAsTrial() {
        val response = Gson().fromJson(
            """
                {
                  "hasActiveSubscription": true,
                  "hasHadSubscription": true,
                  "packageSubscriptions": [
                    {
                      "id": "package-subscription-id",
                      "is_trial": true,
                      "starts_at": "2026-09-15T13:57:02.000Z",
                      "ends_at": "2026-10-29T13:57:02.000Z",
                      "duration_days": 44,
                      "subscription": {
                        "id": "subscription-id",
                        "is_paid": true
                      },
                      "package": {
                        "id": "package-id",
                        "name": "Basic",
                        "tier": "basic",
                        "kind": "base",
                        "description": "Basic plan",
                        "unit_monthly_amount": "20000.00",
                        "features": [],
                        "is_active": true
                      }
                    }
                  ]
                }
            """.trimIndent(),
            ShopSubscriptionStatusResponse::class.java,
        )

        assertTrue(response.resolvedIsActive)
        assertEquals("TRIAL", response.resolvedStatus)
        assertEquals("subscription-id", response.resolvedSubscriptionId)
        assertEquals("basic", response.resolvedType)
        assertEquals("2026-10-29T13:57:02.000Z", response.resolvedExpiry)
    }

    @Test
    fun trialShopSubscription_mapsToTrialStatus() {
        // Real /shops shape, confirmed live: shop.subscription.packageSubscriptions[].
        val subscription = ShopSubscriptionDto(
            isPaid = true,
            packageSubscriptions = listOf(
                ShopPackageSubscriptionDto(
                    isTrial = true,
                    endsAt = "2099-01-01T00:00:00.000Z",
                ),
            ),
        )

        assertTrue(subscription.isTrialActive)
    }

    @Test
    fun cancelledShopPackageSubscription_isNotActive() {
        val subscription = ShopSubscriptionDto(
            isPaid = true,
            packageSubscriptions = listOf(
                ShopPackageSubscriptionDto(
                    isTrial = true,
                    endsAt = "2099-01-01T00:00:00.000Z",
                    cancelledAt = "2026-01-01T00:00:00.000Z",
                ),
            ),
        )

        assertFalse(subscription.isActiveStatus)
    }

    @Test
    fun expiredShopPackageSubscription_isNotActive() {
        val subscription = ShopSubscriptionDto(
            isPaid = true,
            packageSubscriptions = listOf(
                ShopPackageSubscriptionDto(
                    isTrial = true,
                    endsAt = "2020-01-01T00:00:00.000Z",
                ),
            ),
        )

        assertFalse(subscription.isActiveStatus)
    }
}
