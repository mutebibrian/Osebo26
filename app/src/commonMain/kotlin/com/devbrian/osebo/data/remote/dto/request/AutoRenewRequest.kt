package com.devbrian.osebo.data.remote.dto.request


import kotlinx.serialization.Serializable
@Serializable
data class AutoRenewRequest(
    val autoRenew: Boolean
)


