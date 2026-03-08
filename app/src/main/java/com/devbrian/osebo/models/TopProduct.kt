package com.devbrian.osebo.models

import android.os.Parcelable
import com.devbrian.osebo.R
import kotlinx.parcelize.Parcelize
import java.text.NumberFormat
import java.util.*

@Parcelize
data class TopProduct(
    val id: String,
    val name: String,
    val sku: String,
    val quantity: Int,
    val revenue: Double,
    val imageUrl: String? = null,
    val trend: Double = 0.0
) : Parcelable {

    // Formatted revenue
    val formattedRevenue: String
        get() {
            val formatter = NumberFormat.getCurrencyInstance(Locale.US)
            return formatter.format(revenue)
        }

    // Formatted quantity
    val formattedQuantity: String
        get() = NumberFormat.getNumberInstance().format(quantity)

    // Trend display text
    val trendText: String
        get() = when {
            trend > 0 -> "+${String.format("%.1f", trend)}%"
            trend < 0 -> "${String.format("%.1f", trend)}%"
            else -> "0%"
        }

    // Trend color resource (to be used with ContextCompat.getColor)
    fun getTrendColor(): Int {
        return when {
            trend > 0 -> android.R.color.holo_green_dark
            trend < 0 -> android.R.color.holo_red_dark
            else -> android.R.color.darker_gray
        }
    }

    // Trend icon resource
    fun getTrendIcon(): Int {
        return when {
            trend > 0 -> R.drawable.ic_trend_up
            trend < 0 -> R.drawable.ic_trend_down
            else -> R.drawable.ic_trend_flat
        }
    }

    companion object {
        val EMPTY = TopProduct(
            id = "",
            name = "",
            sku = "",
            quantity = 0,
            revenue = 0.0
        )
    }
}