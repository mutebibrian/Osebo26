package com.devbrian.osebo.models


import com.google.gson.annotations.SerializedName

data class Permission(
    @SerializedName("id")
    val id: String,

    @SerializedName("name")
    val name: String,

    @SerializedName("description")
    val description: String,

    @SerializedName("category")
    val category: String, 

    @SerializedName("module")
    val module: String 
)

