package com.devbrian.osebo.models

import com.google.gson.annotations.SerializedName

data class FinancialStatement(
    val data: List<LedgerTransaction>? = null,
    val total: Int = 0,
    val page: Int = 0,
    val limit: Int = 0,
    val inventory: Double = 0.0
) {
    
    val sales: Double
        get() = data?.filter { it.transaction_type == "sale" }
            ?.sumOf { it.payment?.amount ?: 0.0 } ?: 0.0

    val expenses: Double
        get() = data?.filter { it.transaction_type in listOf("procurement", "expense") }
            ?.sumOf { it.payment?.amount ?: 0.0 } ?: 0.0

    val purchases: Double
        get() = data?.filter { it.transaction_type == "procurement" }
            ?.sumOf { it.payment?.amount ?: 0.0 } ?: 0.0

    val netProfit: Double
        get() = sales - expenses

    val grossMargin: Double
        get() = sales - purchases
}


data class FinancialStatementResponse(
    val success: Boolean,
    val message: String,
    val data: FinancialStatementData
)

data class FinancialStatementData(
    val data: List<LedgerTransaction>,
    val total: Int,
    val page: Int,
    val limit: Int
)


data class LedgerTransaction(
    val id: String,
    @SerializedName("createdAt")
    val createdAt: String,
    @SerializedName("updatedAt")
    val updatedAt: String,
    val name: String,
    val description: String,
    @SerializedName("transactionId")
    val transactionId: String,
    @SerializedName("transaction_type")
    val transaction_type: String,
    val payment: PaymentInfo?,
    val shop: ShopInfo,
    @SerializedName("transaction_data")
    val transaction_data: Any? = null
)

data class PaymentInfo(
    val id: String,
    val method: String,
    val reference: String,
    val amount: Double,
    val status: String,
    val description: String,
    val type: String
)




data class TransactionData(
    val id: String,
    @SerializedName("createdAt")
    val createdAt: String,
    @SerializedName("updatedAt")
    val updatedAt: String,
    @SerializedName("total_price")
    val totalPrice: Double?,
    @SerializedName("paid_amount")
    val paidAmount: String?,
    @SerializedName("outstanding_balance")
    val outstandingBalance: String?,
    @SerializedName("payment_status")
    val paymentStatus: String?,
    val type: String?,
    @SerializedName("hasCustomPricing")
    val hasCustomPricing: Boolean?,
    val customer: CustomerInfo?,
    val shop: ShopInfo?,
    @SerializedName("saleStockItems")
    val saleStockItems: List<SaleStockItem>?,
    @SerializedName("salePayments")
    val salePayments: List<SalePayment>?,
    val quantity: Double?,
    @SerializedName("unit_purchase_price")
    val unitPurchasePrice: Double?,
    @SerializedName("purchase_price")
    val purchasePrice: Double?,
    val discount: Double?,
    @SerializedName("stockItem")
    val stockItem: StockItemInfo?,
    val supplier: SupplierInfo?
)

data class CustomerInfo(
    val id: String,
    val name: String,
    val location: String?,
    val email: String?,
    val phone: String?,
    @SerializedName("isDefault")
    val isDefault: Boolean,
    @SerializedName("totalSales")
    val totalSales: String,
    @SerializedName("outstandingBalance")
    val outstandingBalance: String,
    @SerializedName("numberOfSales")
    val numberOfSales: String
)

data class SaleStockItem(
    val id: String,
    val quantity: Double,
    val price: Double,
    val discount: Double,
    val amount: Double,
    @SerializedName("price_adjustment")
    val priceAdjustment: Double,
    @SerializedName("stockItem")
    val stockItem: StockItemInfo
)

data class SalePayment(
    val id: String,
    val payment: PaymentInfo
)

data class StockItemInfo(
    val id: String,
    val name: String,
    @SerializedName("selling_price")
    val sellingPrice: Double?,
    @SerializedName("unit_measure")
    val unitMeasure: String?,
    @SerializedName("quantity")
    val quantity: Double,
    val sku: String? = null,
    val barcode: String? = null
)

data class SupplierInfo(
    val id: String,
    val name: String,
    val location: String?,
    @SerializedName("contact_person")
    val contactPerson: String?,
    val email: String?,
    val phone: String?
)



 {
    companion object {
        const val TYPE_INCOME = "income"
        const val TYPE_EXPENSE = "expense"
        const val CATEGORY_SALES = "Sales"
        const val CATEGORY_OTHER = "Other"
    }
}






