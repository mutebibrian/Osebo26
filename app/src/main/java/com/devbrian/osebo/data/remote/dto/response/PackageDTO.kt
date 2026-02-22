package com.devbrian.osebo.data.remote.dto.response


import com.google.gson.annotations.SerializedName
data class PackageDto(
    @SerializedName("id")
    val id: String,

    @SerializedName("name")
    val name: String,

    @SerializedName("tier")
    val tier: String,

    @SerializedName("type")
    val type: String,

    @SerializedName("description")
    val description: String,

    @SerializedName("unit_monthly_amount")
    val unitMonthlyAmount: String,

    @SerializedName("features")
    val features: List<FeatureDto>,

    @SerializedName("is_active")
    val isActive: Boolean
)



data class FeatureDto(
    @SerializedName("name")
    val name: String,

    @SerializedName("included")
    val included: Boolean,

    @SerializedName("description")
    val description: String
)