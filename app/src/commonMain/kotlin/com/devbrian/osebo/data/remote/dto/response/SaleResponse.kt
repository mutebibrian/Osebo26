package com.devbrian.osebo.data.remote.dto.response

import com.devbrian.osebo.models.ShopInfo
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

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

    @SerialName(value = "invoiceNumber", alternate = ["invoice_number"])
    val invoiceNumber: String? = null,

    @SerialName(value = "createdAt", alternate = ["created_at"])
    val createdAt: String? = null,

    @SerialName(value = "total_price", alternate = ["totalPrice", "total_amount"])
    val totalPrice: Double? = null,

    @SerialName(value = "paid_amount", alternate = ["paidAmount"])
    val paidAmountString: String? = null,

    @SerialName(value = "outstanding_balance", alternate = ["outstandingBalance"])
    val outstandingBalance: String? = null,

    @SerialName(value = "payment_status", alternate = ["paymentStatus", "status"])
    val paymentStatus: String? = null,

    @SerialName("type")
    val type: String? = null,

    @SerialName("customer")
    val customer: CustomerDto? = null,

    @SerialName("shop")
    val shop: ShopInfo? = null,

    @SerialName(value = "saleStockItems", alternate = ["sale_stock_items", "stockItems"])
    val saleStockItems: List<SaleStockItemDto>? = null,

    @SerialName(value = "salePayments", alternate = ["sale_payments", "payments"])
    val salePayments: List<SalePaymentDto>? = null
)

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
