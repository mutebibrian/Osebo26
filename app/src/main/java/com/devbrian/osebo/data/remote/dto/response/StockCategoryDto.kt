package com.devbrian.osebo.data.remote.dto.response

import com.google.gson.annotations.SerializedName


data class StockCategoryDto(
    @SerializedName("id") val id: String,
    @SerializedName("name") val name: String,
    @SerializedName("description") val description: String?
)