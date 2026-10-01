package com.devbrian.osebo.models

import kotlinx.serialization.Serializable

@Serializable
data class SalesDataPoint(
    val label: String,
    val value: Double
)
