package com.devbrian.osebo.data.repository

import com.devbrian.osebo.data.remote.ApiResult
import com.devbrian.osebo.data.remote.KtorOseboApiService
import com.devbrian.osebo.data.remote.dto.request.CreateSubscriptionRequest
import com.devbrian.osebo.data.remote.dto.response.PackageDto
import com.devbrian.osebo.data.remote.dto.response.PaymentCheckResponse
import com.devbrian.osebo.data.remote.dto.response.SubscriptionResponse
import com.devbrian.osebo.ui.screens.SubscriptionBundleUi

/**
 * Wires the one subscription flow Android itself actually ships:
 * SubscriptionPackagesFragment (browse/select bundles) -> PaymentDialogFragment
 * (mobile-money checkout) -> polling for activation via
 * findSubscriptionByPaymentId. SubscriptionFragment's overview+history
 * screen (SubscriptionScreen on iOS) is fully built on Android too, but has
 * no nav-graph action reaching it from anywhere — confirmed dead code, so
 * it's left unwired here; its field-mapping logic remains the reference if
 * that screen is ever built. createSubscription/getSubscriptionPackages/
 * findSubscriptionByPaymentId are already in KtorOseboApiService and are
 * reused as-is.
 */
class SharedSubscriptionRepository(private val api: KtorOseboApiService) {
    suspend fun getPackages(): ApiResult<List<PackageDto>> =
        when (val result = api.getSubscriptionPackages()) {
            is ApiResult.Success -> ApiResult.Success(result.data.data.orEmpty(), result.httpCode)
            is ApiResult.Error -> result
            is ApiResult.NetworkError -> result
        }

    suspend fun createSubscription(
        shopId: String,
        packageIds: List<String>,
        phoneNumber: String,
        months: Int,
        isTrial: Boolean,
    ): ApiResult<SubscriptionResponse> =
        when (
            val result = api.createSubscription(
                shopId,
                CreateSubscriptionRequest(
                    shopId = shopId,
                    packageIds = packageIds,
                    customerPhone = phoneNumber,
                    duration = months,
                    isTrial = isTrial,
                ),
            )
        ) {
            is ApiResult.Success -> result.data.data?.let { ApiResult.Success(it, result.httpCode) }
                ?: ApiResult.Error(result.httpCode, result.data.message.ifBlank { "No response from server" })
            is ApiResult.Error -> result
            is ApiResult.NetworkError -> result
        }

    suspend fun pollPaymentStatus(paymentId: String): ApiResult<PaymentCheckResponse> =
        when (val result = api.findSubscriptionByPaymentId(paymentId)) {
            is ApiResult.Success -> result.data.data?.let { ApiResult.Success(it, result.httpCode) }
                ?: ApiResult.Error(result.httpCode, "No payment status returned")
            is ApiResult.Error -> result
            is ApiResult.NetworkError -> result
        }
}

/**
 * Mirrors SubscriptionPackagesFragment.toBundleUi() — isPopular has no
 * `is_popular` field to read on iOS's PackageDto yet, so it falls back to
 * Android's own OR'd heuristic (tier == "pro") rather than always false.
 */
fun PackageDto.toBundleUi(): SubscriptionBundleUi = SubscriptionBundleUi(
    id = id,
    name = displayName,
    description = description,
    price = formattedPrice.replace("/month", "/mo"),
    features = features.orEmpty().filter { it.included }.map { it.name },
    isPopular = tier.equals("pro", ignoreCase = true),
    isAddOn = kind.equals("custom", ignoreCase = true),
    isCustomPricing = isCustomPlan,
    hasFreeTrial = canTry,
    isActive = isActive,
)

/** Uganda-only +256 normalization, matching SubscriptionPaymentFragment/PaymentDialogFragment's formatPhoneNumber(). */
fun formatSubscriptionPhone(digitsOnly: String): String = when {
    digitsOnly.startsWith("256") -> "+$digitsOnly"
    digitsOnly.length == 10 && digitsOnly.startsWith("0") -> "+256${digitsOnly.drop(1)}"
    digitsOnly.length == 9 -> "+256$digitsOnly"
    else -> "+256$digitsOnly"
}

fun validateSubscriptionPhone(digitsOnly: String): String? = when {
    digitsOnly.isEmpty() -> "Phone number is required"
    digitsOnly.length < 9 -> "Use at least 9 digits"
    digitsOnly.length > 12 -> "Phone number is too long"
    else -> null
}

fun formatSubscriptionAmount(amount: Double): String {
    val whole = amount.toLong().toString()
    val grouped = StringBuilder()
    for ((index, char) in whole.reversed().withIndex()) {
        if (index != 0 && index % 3 == 0) grouped.append(',')
        grouped.append(char)
    }
    return "UGX ${grouped.reverse()}"
}
