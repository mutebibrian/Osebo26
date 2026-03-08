package com.devbrian.osebo.data.remote.api


import com.google.gson.annotations.SerializedName

data class FaqDto(
    @SerializedName("id")
    val id: String,

    @SerializedName("question")
    val question: String,

    @SerializedName("answer")
    val answer: String,

    @SerializedName("category")
    val category: String
)

