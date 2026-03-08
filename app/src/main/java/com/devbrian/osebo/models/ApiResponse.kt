package com.devbrian.osebo.models

import com.google.gson.annotations.SerializedName

data class ApiResponse<T>(
    @SerializedName("status")
    val status: String,
    @SerializedName("message")
    val message: String,
    @SerializedName("data")
    val data: T? = null,
    @SerializedName("success")
    val success: Boolean



    
    
    
)



data class InventoryResponse(
    val items: List<InventoryItem>,
    val total: Int,
    val page: Int,
    val totalPages: Int
)


data class Category(
    val id: String,
    val name: String,
    val description: String?,
    val itemCount: Int
)


data class SalesResponse(
    val sales: List<Sale>,
    val total: Int,
    val page: Int,
    val totalPages: Int,
    val totalAmount: Double
)


data class SaleItemDetail(
    val id: String,
    val inventoryItemId: String,
    val itemName: String,
    val quantity: Int,
    val price: Double,
    val total: Double
)

data class Receipt(
    val id: String,
    val saleId: String,
    val content: String,
    val downloadUrl: String,
    val createdAt: String
)

data class DailySummary(
    val date: String,
    val totalSales: Double,
    val totalTransactions: Int,
    val averageSale: Double,
    val topItems: List<TopItem>
)

data class TopItem(
    val itemId: String,
    val itemName: String,
    val quantitySold: Int,
    val totalRevenue: Double
)

data class FinancialSummary(
    val revenue: Double,
    val expenses: Double,
    val profit: Double,
    val cashInHand: Double,
    val accountsReceivable: Double,
    val accountsPayable: Double
)

data class Expense(
    val id: String,
    val category: String,
    val amount: Double,
    val description: String,
    val date: String,
    val receiptUrl: String?,
    val createdAt: String
)






data class SalesReport(
    val period: String,
    val totalSales: Double,
    val totalTransactions: Int,
    val averageSale: Double,
    val data: List<SalesDataPoint>
)



data class InventoryReport(
    val totalItems: Int,
    val totalValue: Double,
    val lowStockItems: Int,
    val outOfStockItems: Int,
    val topCategories: List<CategorySummary>
)

data class CategorySummary(
    val category: String,
    val itemCount: Int,
    val totalValue: Double
)


data class FinancialReport(
    val period: String,
    val revenue: Double,
    val expenses: Double,
    val profit: Double,
    val expenseByCategory: List<ExpenseCategory>
)

data class ExpenseCategory(
    val category: String,
    val amount: Double,
    val percentage: Double
)

data class CustomersResponse(
    val customers: List<Customer>,
    val total: Int,
    val page: Int,
    val totalPages: Int
)


data class Supplier(
    val id: String,
    val name: String,
    val contactPerson: String?,
    val email: String?,
    val phone: String?,
    val address: String?,
    val productsSupplied: List<String>,
    val totalOrders: Int,
    val lastOrderDate: String?,
    val createdAt: String
)

data class ShopSettings(
    val currency: String,
    val timezone: String,
    val language: String,
    val taxRate: Double,
    val receiptFooter: String?,
    val lowStockThreshold: Int,
    val isBarcodeEnabled: Boolean,
    val isReceiptPrintingEnabled: Boolean,
    val updatedAt: String
)

data class UserPreferences(
    val notificationsEnabled: Boolean,
    val theme: String,
    val language: String,
    val currency: String,
    val updatedAt: String
)


