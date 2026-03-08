package com.devbrian.osebo.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class SalesDataPoint(
    val label: String,
    val value: Double
) : Parcelable