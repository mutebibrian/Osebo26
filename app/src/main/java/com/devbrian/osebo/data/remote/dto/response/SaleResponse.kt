package com.devbrian.osebo.data.remote.dto.response

import com.devbrian.osebo.models.ShopInfo
import com.google.gson.annotations.SerializedName

data class SaleApiResponse(
    @SerializedName("success")
    val success: Boolean,

    @SerializedName("message")
    val message: String? = null,

    @SerializedName("data")
    val data: SaleApiData? = null
)

data class SaleListApiResponse(
    @SerializedName("success")
    val success: Boolean,

    @SerializedName("message")
    val message: String? = null,

    @SerializedName("data")
    val data: List<SaleApiData>? = null
)

data class SaleApiData(
    @SerializedName("id")
    val id: String,

    
    @SerializedName("invoiceNumber")
    val invoiceNumber: String? = null,

    @SerializedName("createdAt")
    val createdAt: String? = null,

    
    @SerializedName("total_price")
    val totalPrice: Double? = null,

    
    @SerializedName("paid_amount")
    val paidAmountString: String? = null,

    @SerializedName("outstanding_balance")
    val outstandingBalance: String? = null,

    @SerializedName("payment_status")
    val paymentStatus: String? = null,

    @SerializedName("type")
    val type: String? = null,

    @SerializedName("customer")
    val customer: CustomerDto? = null,

    @SerializedName("shop")
    val shop: ShopInfo? = null,

    
    @SerializedName("saleStockItems")
    val saleStockItems: List<SaleStockItemDto>? = null,

    
    
    @SerializedName("salePayments")
    val salePayments: List<SalePaymentDto>? = null
)

data class SaleStockItemDto(
    @SerializedName("id")
    val id: String,

    @SerializedName("quantity")
    val quantity: Double,


    @SerializedName("price")
    val price: Double,

    @SerializedName("discount")
    val discount: Double,

    @SerializedName("amount")
    val amount: Double,

    @SerializedName("stockItem")
    val stockItem: StockItemDto? = null
)

data class SalePaymentDto(
    @SerializedName("id")
    val id: String,

    @SerializedName("payment")
    val payment: PaymentDetailDto? = null
)

data class PaymentDetailDto(
    @SerializedName("id")
    val id: String,

    @SerializedName("method")
    val method: String,

    @SerializedName("reference")
    val reference: String? = null,

    @SerializedName("amount")
    val amount: Double,

    @SerializedName("status")
    val status: String
)
