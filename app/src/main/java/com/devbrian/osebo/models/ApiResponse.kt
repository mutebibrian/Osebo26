package com.devbrian.osebo.models

import com.google.gson.annotations.SerializedName

data class ApiResponse(
    @SerializedName("status")
    val status: String,
    @SerializedName("message")
    val message: String,
    @SerializedName("data")
    val data: Any? = null
)

// 2. InventoryResponse.kt
data class InventoryResponse(
    val items: List<InventoryItem>,
    val total: Int,
    val page: Int,
    val totalPages: Int
)

// 3. Category.kt
data class Category(
    val id: String,
    val name: String,
    val description: String?,
    val itemCount: Int
)

// 4. SalesResponse.kt
data class SalesResponse(
    val sales: List<Sale>,
    val total: Int,
    val page: Int,
    val totalPages: Int,
    val totalAmount: Double
)


// 6. SaleItemDetail.kt
data class SaleItemDetail(
    val id: String,
    val inventoryItemId: String,
    val itemName: String,
    val quantity: Int,
    val price: Double,
    val total: Double
)

// 7. Receipt.kt
data class Receipt(
    val id: String,
    val saleId: String,
    val content: String,
    val downloadUrl: String,
    val createdAt: String
)

// 8. DailySummary.kt
data class DailySummary(
    val date: String,
    val totalSales: Double,
    val totalTransactions: Int,
    val averageSale: Double,
    val topItems: List<TopItem>
)

// 9. TopItem.kt
data class TopItem(
    val itemId: String,
    val itemName: String,
    val quantitySold: Int,
    val totalRevenue: Double
)

// 10. FinancialSummary.kt
data class FinancialSummary(
    val revenue: Double,
    val expenses: Double,
    val profit: Double,
    val cashInHand: Double,
    val accountsReceivable: Double,
    val accountsPayable: Double
)

// 11. Expense.kt
data class Expense(
    val id: String,
    val category: String,
    val amount: Double,
    val description: String,
    val date: String,
    val receiptUrl: String?,
    val createdAt: String
)

// 12. SubscriptionPackage.kt
data class SubscriptionPackage(
    val id: String,
    val name: String,
    val description: String,
    val price: Double,
    val currency: String,
    val duration: Int, // in months
    val features: List<String>,
    val isPopular: Boolean = false
)

// 13. Subscription.kt

// 14. SubscriptionStatus.kt
data class SubscriptionStatus(
    val status: String,
    val daysRemaining: Int?,
    val nextBillingDate: String?,
    val canRenew: Boolean
)

// 15. SalesReport.kt
data class SalesReport(
    val period: String,
    val totalSales: Double,
    val totalTransactions: Int,
    val averageSale: Double,
    val data: List<SalesDataPoint>
)

// 16. SalesDataPoint.kt
data class SalesDataPoint(
    val date: String,
    val sales: Double,
    val transactions: Int
)

// 17. InventoryReport.kt
data class InventoryReport(
    val totalItems: Int,
    val totalValue: Double,
    val lowStockItems: Int,
    val outOfStockItems: Int,
    val topCategories: List<CategorySummary>
)

// 18. CategorySummary.kt
data class CategorySummary(
    val category: String,
    val itemCount: Int,
    val totalValue: Double
)

// 19. FinancialReport.kt
data class FinancialReport(
    val period: String,
    val revenue: Double,
    val expenses: Double,
    val profit: Double,
    val expenseByCategory: List<ExpenseCategory>
)

// 20. ExpenseCategory.kt
data class ExpenseCategory(
    val category: String,
    val amount: Double,
    val percentage: Double
)

// 21. CustomersResponse.kt
data class CustomersResponse(
    val customers: List<Customer>,
    val total: Int,
    val page: Int,
    val totalPages: Int
)


// 24. Supplier.kt
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

// 26. ShopSettings.kt
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

// 27. UserPreferences.kt
data class UserPreferences(
    val notificationsEnabled: Boolean,
    val theme: String,
    val language: String,
    val currency: String,
    val updatedAt: String
)