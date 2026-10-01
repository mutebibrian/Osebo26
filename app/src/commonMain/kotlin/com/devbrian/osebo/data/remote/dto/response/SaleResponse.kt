package com.devbrian.osebo.data.remote.dto.response

import com.devbrian.osebo.data.remote.withAlternateKeys
import com.devbrian.osebo.models.ShopInfo
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonTransformingSerializer

@Serializable
data class SaleApiResponse(
    @SerialName("success")
    val success: Boolean,

    @SerialName("message")
    val message: String? = null,

    @SerialName("data")
    val data: SaleApiData? = null
)

@Serializable
data class SaleListApiResponse(
    @SerialName("success")
    val success: Boolean,

    @SerialName("message")
    val message: String? = null,

    @SerialName("data")
    val data: List<SaleApiData>? = null
)

@Serializable
data class SaleApiData(
    @SerialName("id")
    val id: String = "",

    @SerialName("invoiceNumber")
    val invoiceNumber: String? = null,

    @SerialName("createdAt")
    val createdAt: String? = null,

    @SerialName("total_price")
    val totalPrice: Double? = null,

    @SerialName("paid_amount")
    val paidAmountString: String? = null,

    @SerialName("outstanding_balance")
    val outstandingBalance: String? = null,

    @SerialName("payment_status")
    val paymentStatus: String? = null,

    @SerialName("type")
    val type: String? = null,

    @SerialName("customer")
    val customer: CustomerDto? = null,

    @SerialName("shop")
    val shop: ShopInfo? = null,

    @SerialName("saleStockItems")
    val saleStockItems: List<SaleStockItemDto>? = null,

    @SerialName("salePayments")
    val salePayments: List<SalePaymentDto>? = null
)

@OptIn(kotlinx.serialization.ExperimentalSerializationApi::class)
object SaleApiDataSerializer : JsonTransformingSerializer<SaleApiData>(SaleApiData.serializer()) {
    override fun transformDeserialize(element: JsonElement): JsonElement =
        element.withAlternateKeys(
            "invoiceNumber" to listOf("invoice_number"),
            "createdAt" to listOf("created_at"),
            "total_price" to listOf("totalPrice", "total_amount"),
            "paid_amount" to listOf("paidAmount"),
            "outstanding_balance" to listOf("outstandingBalance"),
            "payment_status" to listOf("paymentStatus", "status"),
            "saleStockItems" to listOf("sale_stock_items", "stockItems"),
            "salePayments" to listOf("sale_payments", "payments"),
        )
}

@Serializable
data class SaleStockItemDto(
    @SerialName("id")
    val id: String,

    @SerialName("quantity")
    val quantity: Double,


    @SerialName("price")
    val price: Double,

    @SerialName("discount")
    val discount: Double,

    @SerialName("amount")
    val amount: Double,

    @SerialName("stockItem")
    val stockItem: StockItemDto? = null
)

@Serializable
data class SalePaymentDto(
    @SerialName("id")
    val id: String,

    @SerialName("payment")
    val payment: PaymentDetailDto? = null
)

@Serializable
data class PaymentDetailDto(
    @SerialName("id")
    val id: String,

    @SerialName("method")
    val method: String,

    @SerialName("reference")
    val reference: String? = null,

    @SerialName("amount")
    val amount: Double,

    @SerialName("status")
    val status: String
)
