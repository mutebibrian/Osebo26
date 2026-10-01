package com.devbrian.osebo.data.remote.api

import com.devbrian.osebo.data.remote.withAlternateKeys
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonTransformingSerializer

@Serializable(with = SupplierDtoSerializer::class)
data class SupplierDto(
    @SerialName("id")
    val id: String,

    @SerialName("shopId")
    val shopId: String? = null,

    @SerialName("name")
    val name: String,

    @SerialName("contactPerson")
    val contactPerson: String? = null,

    @SerialName("email")
    val email: String? = null,

    @SerialName("phone")
    val phone: String? = null,

    @SerialName("address")
    val address: String? = null,

    @SerialName("numberOfProducts")
    val numberOfProducts: Int? = null,

    @SerialName("totalPurchases")
    val totalPurchases: Double? = null,
)

@OptIn(kotlinx.serialization.ExperimentalSerializationApi::class)
object SupplierDtoSerializer : JsonTransformingSerializer<SupplierDto>(SupplierDto.serializer()) {
    override fun transformDeserialize(element: JsonElement): JsonElement =
        element.withAlternateKeys(
            "shopId" to listOf("shop_id"),
            "contactPerson" to listOf("contact_person"),
            "address" to listOf("location"),
            "numberOfProducts" to listOf("products", "productCount"),
            "totalPurchases" to listOf("total_purchases"),
        )
}

