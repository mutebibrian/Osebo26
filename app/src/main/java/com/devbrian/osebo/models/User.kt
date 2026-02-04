package com.devbrian.osebo.models

import com.google.gson.annotations.SerializedName

data class User(
@SerializedName("id")
val id: String,

@SerializedName("email")
val email: String,

@SerializedName("name")
val name: String,

@SerializedName("phone")
val phone: String? = null,

@SerializedName("role")
val role: String? = null,

@SerializedName("created_at")
val createdAt: String,

@SerializedName("updated_at")
val updatedAt: String,

@SerializedName("shop_id")  // Add shop_id to User model
val shopId: String? = null
)
