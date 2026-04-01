package com.devbrian.osebo.data.repository

import com.devbrian.osebo.data.ApiService
import com.devbrian.osebo.data.PreferenceManager
import com.devbrian.osebo.data.local.AppDatabase
import com.devbrian.osebo.data.local.entity.SaleEntity
import com.devbrian.osebo.data.local.entity.SyncQueueEntity
import com.devbrian.osebo.data.remote.dto.request.SaleItemRequest
import com.devbrian.osebo.data.remote.dto.request.SaleRequest
import com.devbrian.osebo.data.remote.dto.request.PaymentRequest
import com.devbrian.osebo.data.remote.dto.response.PaymentData
import com.devbrian.osebo.data.remote.dto.response.SaleApiResponse
import com.devbrian.osebo.data.remote.dto.response.SaleListApiResponse
import com.devbrian.osebo.models.CartItem
import com.devbrian.osebo.models.Customer
import com.devbrian.osebo.models.Sale
import com.devbrian.osebo.models.SaleData
import com.devbrian.osebo.models.ShopInfo
import com.devbrian.osebo.utils.NetworkUtils
import com.devbrian.osebo.utils.Resource
import com.google.gson.Gson
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SalesRepository @Inject constructor(
    private val database: AppDatabase,
    private val apiService: ApiService,
    private val preferenceManager: PreferenceManager,
    private val gson: Gson
) {

    /**
     * Get recent sales for the current shop
     */
    fun getRecentSales(limit: Int = 20): Flow<List<Sale>> {
        val shopId = preferenceManager.getCurrentShopId()
        return flow {
            try {
                println("📊 Repository - Fetching recent sales for shop: $shopId")

                if (shopId.isEmpty()) {
                    println("⚠️ Repository - Shop ID is empty, returning empty list")
                    emit(emptyList())
                    return@flow
                }

                val entities = database.saleDao().getRecentSales(shopId, limit).first()
                println("📊 Repository - Raw entities count: ${entities.size}")

                entities.forEachIndexed { index, entity ->
                    println("📊 Repository - Entity[$index]: id=${entity.id}, amount=${entity.totalAmount}, status=${entity.status}, createdAt=${entity.createdAt}")
                }

                val sales = entities.map { entity ->
                    try {
                        entity.toSale()
                    } catch (e: Exception) {
                        println("❌ Repository - Error converting entity to sale: ${e.message}")
                        null
                    }
                }.filterNotNull()
                    .filter { it.amount > 0 }
                    .take(limit)

                println("📊 Repository - Converted sales count: ${sales.size}")
                emit(sales)
            } catch (e: Exception) {
                println("❌ Repository - Error getting recent sales: ${e.message}")
                e.printStackTrace()
                emit(emptyList())
            }
        }
    }

    /**
     * Get sale by ID
     */
    suspend fun getSaleById(saleId: String): Sale? {
        return try {
            val entity = database.saleDao().getSaleById(saleId)
            entity?.toSale()
        } catch (e: Exception) {
            println("❌ Repository - Error getting sale by ID: ${e.message}")
            null
        }
    }

    /**
     * Create a new sale
     */
    suspend fun createSale(
        customer: Customer?,
        cartItems: List<CartItem>,
        paidAmount: Double,
        saleType: String = "sale",
        paymentMethod: String = "cash",
        notes: String? = null
    ): Resource<SaleData> {
        val shopId = preferenceManager.getCurrentShopId()

        // Debug shop ID
        println("🔍 CREATE SALE - Shop ID from preferences: '$shopId'")
        println("🔍 CREATE SALE - Shop ID length: ${shopId.length}")
        println("🔍 CREATE SALE - Shop ID empty? ${shopId.isEmpty()}")

        if (shopId.isEmpty()) {
            println("❌ CREATE SALE - ERROR: No shop selected!")
            return Resource.Error("No shop selected. Please select a shop first.")
        }

        // Check for temporary products
        val hasTempProducts = cartItems.any { it.product.id.startsWith("temp_") }
        if (hasTempProducts) {
            println("⚠️ CREATE SALE - Cart contains temporary products")
            return Resource.Error("Some products haven't been synced yet. Please wait.")
        }

        val subtotal = cartItems.sumOf { it.subtotal }
        val totalAmount = subtotal

        val tempId = "temp_${System.currentTimeMillis()}"
        val invoiceNumber = generateInvoiceNumber()

        val saleItems = cartItems.map { item ->
            SaleItemRequest(
                stockItemId = item.product.id,
                quantity = item.quantity,
                price = item.unitPrice,
                discount = item.discount,
                isCustomPrice = item.isCustomPrice
            )
        }

        val request = SaleRequest(
            customerId = customer?.id,
            paidAmount = paidAmount.toString(),
            saleType = saleType,
            stockItems = saleItems,
            notes = notes
        )

        println("📝 CREATE SALE - Request prepared:")
        println("   - Shop ID: $shopId")
        println("   - Customer ID: ${customer?.id}")
        println("   - Paid Amount: $paidAmount")
        println("   - Items: ${saleItems.size}")

        val initialStatus = when {
            paidAmount >= totalAmount -> "COMPLETED"
            paidAmount > 0 -> "PARTIAL"
            else -> "PENDING"
        }

        val saleEntity = SaleEntity(
            id = tempId,
            invoiceNumber = invoiceNumber,
            customerId = customer?.id,
            customerName = customer?.name ?: "Walk-in Customer",
            totalAmount = totalAmount,
            paidAmount = paidAmount,
            change = (paidAmount - totalAmount).coerceAtLeast(0.0),
            saleType = saleType,
            status = initialStatus,
            items = gson.toJson(cartItems),
            paymentMethod = paymentMethod,
            notes = notes,
            createdAt = System.currentTimeMillis().toString(),
            shopId = shopId,
            isPendingSync = true,
            syncAction = "CREATE"
        )

        println("📝 Repository - Inserting temp sale: $tempId, amount: $totalAmount, status: $initialStatus")
        database.saleDao().insertSale(saleEntity)

        return if (NetworkUtils.isNetworkAvailable(preferenceManager.getContext())) {
            syncCreateSale(saleEntity, request, paymentMethod)
        } else {
            println("📦 Repository - No network, queuing sale for later sync")
            queueForSync(saleEntity, "CREATE")
            Resource.Success(
                SaleData(
                    id = tempId,
                    invoiceNumber = invoiceNumber,
                    totalAmount = totalAmount,
                    paidAmount = paidAmount,
                    change = (paidAmount - totalAmount).coerceAtLeast(0.0),
                    paymentMethod = paymentMethod,
                    createdAt = saleEntity.createdAt,
                    shop = null
                )
            )
        }
    }

    /**
     * Sync a created sale with the server
     */
    private suspend fun syncCreateSale(
        entity: SaleEntity,
        request: SaleRequest,
        originalPaymentMethod: String
    ): Resource<SaleData> {
        return try {
            val shopId = preferenceManager.getCurrentShopId()

            // Double-check shop ID before sync
            println("🔍 SYNC CREATE SALE - Shop ID check:")
            println("   - From preferences: '$shopId'")
            println("   - From entity: '${entity.shopId}'")
            println("   - Match? ${shopId == entity.shopId}")

            if (shopId.isEmpty()) {
                println("❌ SYNC CREATE SALE - ERROR: Shop ID is empty! Cannot sync sale")
                queueForSync(entity, "CREATE")
                return Resource.Error("No shop selected. Please select a shop first.")
            }

            println("📤 Syncing sale to server: ${entity.id}")
            println("📤 Request details - Customer: ${request.customerId}, Amount: ${request.paidAmount}, Type: ${request.saleType}")
            println("📤 Shop ID being sent as header: $shopId")

            // Make the API call - shopId is passed as header automatically by Retrofit
            val response = apiService.createSale(shopId, request)

            if (response.isSuccessful) {
                val apiResponse = response.body()
                if (apiResponse?.success == true) {
                    val apiData = apiResponse.data
                    if (apiData != null) {
                        println("✅ Sale synced successfully: ${apiData.id}")
                        println("📦 API Response - Shop: ${apiData.shop?.name}")

                        // Calculate total amount from total_price
                        val totalAmount = apiData.totalPrice ?: entity.totalAmount

                        // Calculate paid amount from paid_amount string
                        val paidAmount = try {
                            apiData.paidAmountString?.toDouble() ?: entity.paidAmount
                        } catch (e: Exception) {
                            entity.paidAmount
                        }

                        // Calculate change
                        val change = (paidAmount - totalAmount).coerceAtLeast(0.0)

                        // Get payment method from salePayments if available
                        val paymentMethod = try {
                            apiData.salePayments?.firstOrNull()?.payment?.method ?: originalPaymentMethod
                        } catch (e: Exception) {
                            originalPaymentMethod
                        }

                        // Create SaleData object
                        val saleData = SaleData(
                            id = apiData.id,
                            invoiceNumber = apiData.invoiceNumber,
                            totalAmount = totalAmount,
                            paidAmount = paidAmount,
                            change = change,
                            paymentMethod = paymentMethod,
                            createdAt = apiData.createdAt,
                            shop = apiData.shop
                        )

                        // Create updated entity with real ID
                        val updatedEntity = SaleEntity(
                            id = saleData.id,
                            invoiceNumber = saleData.invoiceNumber ?: entity.invoiceNumber,
                            customerId = entity.customerId,
                            customerName = entity.customerName,
                            totalAmount = saleData.totalAmount,
                            paidAmount = saleData.paidAmount,
                            change = saleData.change,
                            saleType = entity.saleType,
                            status = "COMPLETED",
                            items = entity.items,
                            paymentMethod = saleData.paymentMethod,
                            notes = entity.notes,
                            createdAt = saleData.createdAt ?: entity.createdAt,
                            shopId = shopId,
                            isPendingSync = false,
                            syncAction = null
                        )

                        println("🗑️ Deleting temp record: ${entity.id}")
                        database.saleDao().deleteSaleById(entity.id)

                        println("💾 Inserting real record: ${saleData.id}")
                        database.saleDao().insertSale(updatedEntity)

                        val verifyInsert = database.saleDao().getSaleById(saleData.id)
                        if (verifyInsert != null) {
                            println("✅ Verified real record exists in DB: ${verifyInsert.id}")
                        } else {
                            println("⚠️ Warning: Real record not found after insert!")
                        }

                        println("✅ Replaced temp ID ${entity.id} with real ID ${saleData.id}")

                        Resource.Success(saleData)
                    } else {
                        println("❌ Sync failed: No data returned from API")
                        queueForSync(entity, "CREATE")
                        Resource.Error("Failed to sync sale: No data returned")
                    }
                } else {
                    val errorMsg = apiResponse?.message ?: "Unknown error"
                    println("❌ Sync failed: $errorMsg")
                    queueForSync(entity, "CREATE")
                    Resource.Error(errorMsg)
                }
            } else {
                println("❌ Sync failed with code: ${response.code()}")
                val errorBody = response.errorBody()?.string()
                println("❌ Error body: $errorBody")

                // Check if it's the X-Shop header error
                if (errorBody?.contains("X-Shop header is required") == true) {
                    println("❌ CRITICAL: X-Shop header is missing or invalid!")
                    println("🔍 Current shop ID in preferences: '${preferenceManager.getCurrentShopId()}'")
                    println("🔍 Current shop UUID: '${preferenceManager.getCurrentShopUuid()}'")
                    preferenceManager.debugSubscriptionInfo()
                }

                queueForSync(entity, "CREATE")
                Resource.Error("Network error: ${response.code()}")
            }
        } catch (e: Exception) {
            println("❌ Sync error: ${e.message}")
            e.printStackTrace()
            queueForSync(entity, "CREATE")
            Resource.Error(e.message ?: "Network error")
        }
    }

    /**
     * Get sales for a specific customer
     */
    suspend fun getCustomerSales(customerId: String): Resource<List<SaleData>> {
        val shopId = preferenceManager.getCurrentShopId()
        if (shopId.isEmpty()) {
            return Resource.Error("No shop selected")
        }

        return try {
            if (!NetworkUtils.isNetworkAvailable(preferenceManager.getContext())) {
                val localSales = database.saleDao().getSalesByCustomer(customerId).first()
                val saleDataList = localSales.map { entity ->
                    SaleData(
                        id = entity.id,
                        invoiceNumber = entity.invoiceNumber,
                        totalAmount = entity.totalAmount,
                        paidAmount = entity.paidAmount,
                        change = entity.change,
                        paymentMethod = entity.paymentMethod ?: "cash",
                        createdAt = entity.createdAt,
                        shop = null
                    )
                }
                return Resource.Success(saleDataList)
            }

            println("📤 Fetching customer sales for: $customerId")
            val response = apiService.getCustomerSales(shopId, customerId)

            if (response.isSuccessful) {
                val apiResponse = response.body()
                if (apiResponse?.success == true) {
                    val apiDataList = apiResponse.data ?: emptyList()
                    println("✅ Received ${apiDataList.size} customer sales")

                    // Convert ApiData list to SaleData list
                    val saleDataList = apiDataList.map { apiData ->
                        val totalAmount = apiData.totalPrice ?: 0.0
                        val paidAmount = try {
                            apiData.paidAmountString?.toDouble() ?: 0.0
                        } catch (e: Exception) {
                            0.0
                        }
                        val change = (paidAmount - totalAmount).coerceAtLeast(0.0)
                        val paymentMethod = try {
                            apiData.salePayments?.firstOrNull()?.payment?.method ?: "cash"
                        } catch (e: Exception) {
                            "cash"
                        }

                        SaleData(
                            id = apiData.id,
                            invoiceNumber = apiData.invoiceNumber,
                            totalAmount = totalAmount,
                            paidAmount = paidAmount,
                            change = change,
                            paymentMethod = paymentMethod,
                            createdAt = apiData.createdAt,
                            shop = apiData.shop
                        )
                    }
                    Resource.Success(saleDataList)
                } else {
                    Resource.Error(apiResponse?.message ?: "Failed to fetch customer sales")
                }
            } else {
                Resource.Error("Network error: ${response.code()}")
            }
        } catch (e: Exception) {
            println("❌ Error fetching customer sales: ${e.message}")
            Resource.Error(e.message ?: "Network error")
        }
    }

    /**
     * Get detailed sale information
     */
    suspend fun getSaleDetails(saleId: String): Resource<SaleData> {
        val shopId = preferenceManager.getCurrentShopId()
        if (shopId.isEmpty()) {
            return Resource.Error("No shop selected")
        }

        return try {
            if (!NetworkUtils.isNetworkAvailable(preferenceManager.getContext())) {
                val localSale = database.saleDao().getSaleById(saleId)
                if (localSale != null) {
                    val saleData = SaleData(
                        id = localSale.id,
                        invoiceNumber = localSale.invoiceNumber,
                        totalAmount = localSale.totalAmount,
                        paidAmount = localSale.paidAmount,
                        change = localSale.change,
                        paymentMethod = localSale.paymentMethod ?: "cash",
                        createdAt = localSale.createdAt,
                        shop = null
                    )
                    return Resource.Success(saleData)
                } else {
                    return Resource.Error("Sale not found")
                }
            }

            println("📤 Fetching sale details for: $saleId")
            val response = apiService.getSale(shopId, saleId)

            if (response.isSuccessful) {
                val apiResponse = response.body()
                if (apiResponse?.success == true) {
                    val apiData = apiResponse.data
                    if (apiData != null) {
                        println("✅ Sale details received")

                        val totalAmount = apiData.totalPrice ?: 0.0
                        val paidAmount = try {
                            apiData.paidAmountString?.toDouble() ?: 0.0
                        } catch (e: Exception) {
                            0.0
                        }
                        val change = (paidAmount - totalAmount).coerceAtLeast(0.0)
                        val paymentMethod = try {
                            apiData.salePayments?.firstOrNull()?.payment?.method ?: "cash"
                        } catch (e: Exception) {
                            "cash"
                        }

                        val saleData = SaleData(
                            id = apiData.id,
                            invoiceNumber = apiData.invoiceNumber,
                            totalAmount = totalAmount,
                            paidAmount = paidAmount,
                            change = change,
                            paymentMethod = paymentMethod,
                            createdAt = apiData.createdAt,
                            shop = apiData.shop
                        )
                        Resource.Success(saleData)
                    } else {
                        Resource.Error("Sale not found")
                    }
                } else {
                    Resource.Error(apiResponse?.message ?: "Failed to fetch sale details")
                }
            } else {
                Resource.Error("Network error: ${response.code()}")
            }
        } catch (e: Exception) {
            println("❌ Error fetching sale details: ${e.message}")
            Resource.Error(e.message ?: "Network error")
        }
    }

    /**
     * Get complete sale history for a customer
     */
    suspend fun getCustomerSaleHistory(customerId: String): Resource<List<SaleData>> {
        val shopId = preferenceManager.getCurrentShopId()
        if (shopId.isEmpty()) {
            return Resource.Error("No shop selected")
        }

        return try {
            if (!NetworkUtils.isNetworkAvailable(preferenceManager.getContext())) {
                val localSales = database.saleDao().getSalesByCustomer(customerId).first()
                val saleDataList = localSales.map { entity ->
                    SaleData(
                        id = entity.id,
                        invoiceNumber = entity.invoiceNumber,
                        totalAmount = entity.totalAmount,
                        paidAmount = entity.paidAmount,
                        change = entity.change,
                        paymentMethod = entity.paymentMethod ?: "cash",
                        createdAt = entity.createdAt,
                        shop = null
                    )
                }
                return Resource.Success(saleDataList)
            }

            println("📤 Fetching customer sale history for: $customerId")
            val response = apiService.getCustomerSaleHistory(shopId, customerId)

            if (response.isSuccessful) {
                val apiResponse = response.body()
                if (apiResponse?.success == true) {
                    val apiDataList = apiResponse.data ?: emptyList()
                    println("✅ Received ${apiDataList.size} customer sale history")

                    // Convert ApiData list to SaleData list
                    val saleDataList = apiDataList.map { apiData ->
                        val totalAmount = apiData.totalPrice ?: 0.0
                        val paidAmount = try {
                            apiData.paidAmountString?.toDouble() ?: 0.0
                        } catch (e: Exception) {
                            0.0
                        }
                        val change = (paidAmount - totalAmount).coerceAtLeast(0.0)
                        val paymentMethod = try {
                            apiData.salePayments?.firstOrNull()?.payment?.method ?: "cash"
                        } catch (e: Exception) {
                            "cash"
                        }

                        SaleData(
                            id = apiData.id,
                            invoiceNumber = apiData.invoiceNumber,
                            totalAmount = totalAmount,
                            paidAmount = paidAmount,
                            change = change,
                            paymentMethod = paymentMethod,
                            createdAt = apiData.createdAt,
                            shop = apiData.shop
                        )
                    }
                    Resource.Success(saleDataList)
                } else {
                    Resource.Error(apiResponse?.message ?: "Failed to fetch customer sale history")
                }
            } else {
                Resource.Error("Network error: ${response.code()}")
            }
        } catch (e: Exception) {
            println("❌ Error fetching customer sale history: ${e.message}")
            Resource.Error(e.message ?: "Network error")
        }
    }

    /**
     * Record a payment for an existing sale
     */
    suspend fun recordPayment(
        saleId: String,
        customerId: String?,
        paidAmount: Double,
        paymentMethod: String,
        reference: String? = null
    ): Resource<PaymentData> {
        val shopId = preferenceManager.getCurrentShopId()
        if (shopId.isEmpty()) {
            return Resource.Error("No shop selected")
        }

        val request = PaymentRequest(
            saleId = saleId,
            amount = paidAmount,
            paymentMethod = paymentMethod,
            reference = reference,
            notes = null
        )

        return try {
            if (!NetworkUtils.isNetworkAvailable(preferenceManager.getContext())) {
                return Resource.Error("No internet connection")
            }

            println("📤 Recording payment for sale: $saleId")
            val response = apiService.recordPayment(shopId, request)

            if (response.isSuccessful) {
                val apiResponse = response.body()
                if (apiResponse?.success == true) {
                    val paymentData = apiResponse.data
                    if (paymentData != null) {
                        println("✅ Payment recorded successfully: ${paymentData.id}")

                        updateLocalSalePaymentStatus(saleId, paidAmount, paymentMethod)

                        Resource.Success(paymentData)
                    } else {
                        Resource.Error("No payment data received")
                    }
                } else {
                    println("❌ Payment recording failed: ${apiResponse?.message}")
                    Resource.Error(apiResponse?.message ?: "Failed to record payment")
                }
            } else {
                println("❌ Payment recording failed with code: ${response.code()}")
                Resource.Error("Network error: ${response.code()}")
            }
        } catch (e: Exception) {
            println("❌ Error recording payment: ${e.message}")
            Resource.Error(e.message ?: "Network error")
        }
    }

    /**
     * Update local sale payment status after successful payment
     */
    private suspend fun updateLocalSalePaymentStatus(saleId: String, paidAmount: Double, paymentMethod: String) {
        try {
            val sale = database.saleDao().getSaleById(saleId)
            if (sale != null) {
                val newTotalPaid = sale.paidAmount + paidAmount
                val newStatus = when {
                    newTotalPaid >= sale.totalAmount -> "COMPLETED"
                    newTotalPaid > 0 -> "PARTIAL"
                    else -> "PENDING"
                }

                val updatedSale = sale.copy(
                    paidAmount = newTotalPaid,
                    status = newStatus,
                    paymentMethod = paymentMethod
                )
                database.saleDao().updateSale(updatedSale)
                println("✅ Updated local sale status: $saleId to $newStatus")
            }
        } catch (e: Exception) {
            println("❌ Error updating local sale: ${e.message}")
        }
    }

    /**
     * Queue a sale for later sync when offline
     */
    private suspend fun queueForSync(entity: SaleEntity, action: String) {
        val syncItem = SyncQueueEntity(
            entityType = "SALE",
            entityId = entity.id,
            action = action,
            data = gson.toJson(entity),
            shopId = entity.shopId,
            status = "PENDING",
            retryCount = 0,
            createdAt = System.currentTimeMillis()
        )
        database.syncQueueDao().insertSyncItem(syncItem)
        println("📦 Queued for sync: $action - ${entity.id}")
    }

    /**
     * Generate a unique invoice number
     */
    private fun generateInvoiceNumber(): String {
        val dateFormat = SimpleDateFormat("yyyyMMdd", Locale.getDefault())
        val date = dateFormat.format(Date())
        val random = (1000..9999).random()
        return "INV-$date-$random"
    }

    /**
     * Calculate change amount
     */
    fun calculateChange(tendered: Double, total: Double): Double {
        return (tendered - total).coerceAtLeast(0.0)
    }

    /**
     * Calculate subtotal from cart items
     */
    fun calculateSubtotal(items: List<CartItem>): Double {
        return items.sumOf { it.subtotal }
    }

    /**
     * Calculate total tax from cart items
     */
    fun calculateTotalTax(items: List<CartItem>): Double {
        return items.sumOf { item ->
            val price = item.customPrice ?: item.product.price
            val taxRate = item.product.taxRate ?: 0.0
            price * item.quantity * taxRate / 100
        }
    }

    /**
     * Get complete sale summary
     */
    data class SaleSummary(
        val subtotal: Double,
        val totalDiscount: Double,
        val tax: Double,
        val total: Double,
        val itemCount: Int
    )

    fun getSaleSummary(items: List<CartItem>): SaleSummary {
        val subtotal = items.sumOf { it.unitPrice * it.quantity }
        val totalDiscount = items.sumOf { it.totalDiscount }
        val tax = calculateTotalTax(items)
        val total = items.sumOf { it.subtotal } + tax

        return SaleSummary(
            subtotal = subtotal,
            totalDiscount = totalDiscount,
            tax = tax,
            total = total,
            itemCount = items.size
        )
    }
}