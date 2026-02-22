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
import com.devbrian.osebo.models.CartItem
import com.devbrian.osebo.models.Customer
import com.devbrian.osebo.models.Sale
import com.devbrian.osebo.models.SaleData
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

    // ==================== LOCAL SALES (OFFLINE STORAGE) ====================

    fun getRecentSales(limit: Int = 20): Flow<List<Sale>> {
        val shopId = preferenceManager.getCurrentShopId()
        return flow {
            try {
                println("📊 Repository - Fetching recent sales for shop: $shopId")
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

    suspend fun getSaleById(saleId: String): Sale? {
        return try {
            val entity = database.saleDao().getSaleById(saleId)
            entity?.toSale()
        } catch (e: Exception) {
            println("❌ Repository - Error getting sale by ID: ${e.message}")
            null
        }
    }

    // ==================== CREATE SALE (OFFLINE-FIRST) ====================

    suspend fun createSale(
        customer: Customer?,
        cartItems: List<CartItem>,
        paidAmount: Double,
        saleType: String = "sale",
        paymentMethod: String = "cash",
        notes: String? = null
    ): Resource<SaleData> {
        val shopId = preferenceManager.getCurrentShopId()
        if (shopId.isEmpty()) {
            return Resource.Error("No shop selected")
        }

        // Calculate totals
        val subtotal = cartItems.sumOf { it.subtotal }
        val totalAmount = subtotal

        // Generate temp ID for offline use
        val tempId = "temp_${System.currentTimeMillis()}"
        val invoiceNumber = generateInvoiceNumber()

        // Create sale items for API
        val saleItems = cartItems.map { item ->
            SaleItemRequest(
                stockItemId = item.product.id,
                quantity = item.quantity,
                price = item.unitPrice,
                discount = item.discount,
                isCustomPrice = item.isCustomPrice
            )
        }

        // Create API request
        val request = SaleRequest(
            customerId = customer?.id,
            paidAmount = paidAmount.toString(),
            saleType = saleType,
            stockItems = saleItems,
            notes = notes
        )

        // Determine initial status
        val initialStatus = when {
            paidAmount >= totalAmount -> "COMPLETED"
            paidAmount > 0 -> "PARTIAL"
            else -> "PENDING"
        }

        // Save to local DB with pending sync flag
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

        // If online, try to sync immediately
        return if (NetworkUtils.isNetworkAvailable(preferenceManager.getContext())) {
            syncCreateSale(saleEntity, request)
        } else {
            queueForSync(saleEntity, "CREATE")
            Resource.Success(
                SaleData(
                    id = tempId,
                    invoiceNumber = invoiceNumber,
                    customerId = customer?.id,
                    customerName = customer?.name,
                    totalAmount = totalAmount,
                    paidAmount = paidAmount,
                    change = (paidAmount - totalAmount).coerceAtLeast(0.0),
                    saleType = saleType,
                    status = initialStatus,
                    paymentMethod = paymentMethod,
                    createdAt = saleEntity.createdAt,
                    items = null
                )
            )
        }
    }

    private suspend fun syncCreateSale(
        entity: SaleEntity,
        request: SaleRequest
    ): Resource<SaleData> {
        return try {
            val shopId = preferenceManager.getCurrentShopId()
            println("📤 Syncing sale to server: ${entity.id}")
            println("📤 Request details - Customer: ${request.customerId}, Amount: ${request.paidAmount}, Type: ${request.saleType}")

            val response = apiService.createSale(shopId, request)

            if (response.isSuccessful) {
                val apiResponse = response.body()
                if (apiResponse?.success == true) {
                    val saleData = apiResponse.data
                    if (saleData != null) {
                        println("✅ Sale synced successfully: ${saleData.id}")
                        println("📦 API Response - ID: ${saleData.id}")
                        println("📦 API Response - Total Price: ${saleData.totalPrice}")
                        println("📦 API Response - Paid Amount: ${saleData.paidAmountString}")
                        println("📦 API Response - Payment Status: ${saleData.paymentStatus}")
                        println("📦 API Response - Status: ${saleData.status}")
                        println("📦 API Response - Type: ${saleData.type}")

                        // Get the actual amounts using helper functions
                        val actualTotalAmount = saleData.getActualTotalAmount()
                        val actualPaidAmount = saleData.getActualPaidAmount()

                        // Log the comparison for debugging
                        println("📊 Comparison - Total: $actualTotalAmount, Paid: $actualPaidAmount")
                        println("📊 Is fully paid? ${actualPaidAmount >= actualTotalAmount}")

                        val actualStatus = saleData.getActualStatus()
                        val actualSaleType = saleData.getActualSaleType()
                        val actualPaymentMethod = saleData.getActualPaymentMethod(entity.paymentMethod ?: "cash")

                        println("✅ Processed data: amount=$actualTotalAmount, paid=$actualPaidAmount, status=$actualStatus, paymentMethod=$actualPaymentMethod")

                        // Create new entity with real ID from server
                        val updatedEntity = SaleEntity(
                            id = saleData.id,
                            invoiceNumber = saleData.invoiceNumber ?: entity.invoiceNumber,
                            customerId = saleData.customer?.id ?: saleData.customerId ?: entity.customerId,
                            customerName = saleData.customer?.name ?: saleData.customerName ?: entity.customerName,
                            totalAmount = actualTotalAmount,
                            paidAmount = actualPaidAmount,
                            change = (actualPaidAmount - actualTotalAmount).coerceAtLeast(0.0),
                            saleType = actualSaleType,
                            status = actualStatus,
                            items = entity.items,
                            paymentMethod = actualPaymentMethod,
                            notes = entity.notes,
                            createdAt = saleData.createdAt ?: entity.createdAt,
                            shopId = shopId,
                            isPendingSync = false,
                            syncAction = null
                        )

                        // Delete the old temp record
                        println("🗑️ Deleting temp record: ${entity.id}")
                        database.saleDao().deleteSaleById(entity.id)

                        // Insert the new record with real ID
                        println("💾 Inserting real record: ${saleData.id} with status: $actualStatus")
                        database.saleDao().insertSale(updatedEntity)

                        // Verify the record was inserted
                        val verifyInsert = database.saleDao().getSaleById(saleData.id)
                        if (verifyInsert != null) {
                            println("✅ Verified real record exists in DB: ${verifyInsert.id}, amount: ${verifyInsert.totalAmount}, status: ${verifyInsert.status}")
                        } else {
                            println("❌ Failed to verify real record in DB!")
                        }

                        println("✅ Replaced temp ID ${entity.id} with real ID ${saleData.id}")

                        // Create SaleData object from response
                        val resultSaleData = SaleData(
                            id = saleData.id,
                            invoiceNumber = saleData.invoiceNumber,
                            customerId = saleData.customer?.id,
                            customerName = saleData.customer?.name,
                            totalAmount = actualTotalAmount,
                            paidAmount = actualPaidAmount,
                            change = (actualPaidAmount - actualTotalAmount).coerceAtLeast(0.0),
                            saleType = actualSaleType,
                            status = actualStatus,
                            paymentMethod = actualPaymentMethod,
                            createdAt = saleData.createdAt ?: entity.createdAt,
                            items = saleData.items,
                            totalPrice = actualTotalAmount,
                            paidAmountString = actualPaidAmount.toString(),
                            type = actualSaleType,
                            customer = saleData.customer,
                            paymentStatus = actualStatus
                        )

                        Resource.Success(resultSaleData)
                    } else {
                        println("❌ Sync failed: No data returned from API")
                        queueForSync(entity, "CREATE")
                        Resource.Error("Failed to sync sale: No data returned")
                    }
                } else {
                    println("❌ Sync failed: ${apiResponse?.message ?: "Unknown error"}")
                    queueForSync(entity, "CREATE")
                    Resource.Error(apiResponse?.message ?: "Failed to create sale")
                }
            } else {
                println("❌ Sync failed with code: ${response.code()}")
                println("❌ Error body: ${response.errorBody()?.string()}")
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

    // ==================== GET CUSTOMER SALES ====================

    suspend fun getCustomerSales(customerId: String): Resource<List<SaleData>> {
        val shopId = preferenceManager.getCurrentShopId()
        if (shopId.isEmpty()) {
            return Resource.Error("No shop selected")
        }

        return try {
            if (!NetworkUtils.isNetworkAvailable(preferenceManager.getContext())) {
                // Try to get from local DB
                val localSales = database.saleDao().getSalesByCustomer(customerId).first()
                val saleDataList = localSales.map { entity ->
                    SaleData(
                        id = entity.id,
                        invoiceNumber = entity.invoiceNumber,
                        customerId = entity.customerId,
                        customerName = entity.customerName,
                        totalAmount = entity.totalAmount,
                        paidAmount = entity.paidAmount,
                        change = entity.change,
                        saleType = entity.saleType,
                        status = entity.status,
                        paymentMethod = entity.paymentMethod ?: "cash",
                        createdAt = entity.createdAt,
                        items = null
                    )
                }
                return Resource.Success(saleDataList)
            }

            println("📤 Fetching customer sales for: $customerId")
            val response = apiService.getCustomerSales(shopId, customerId)

            if (response.isSuccessful) {
                val apiResponse = response.body()
                if (apiResponse?.success == true) {
                    val sales = apiResponse.data ?: emptyList()
                    println("✅ Received ${sales.size} customer sales")
                    Resource.Success(sales)
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

    // ==================== GET SALE DETAILS ====================

    suspend fun getSaleDetails(saleId: String): Resource<SaleData> {
        val shopId = preferenceManager.getCurrentShopId()
        if (shopId.isEmpty()) {
            return Resource.Error("No shop selected")
        }

        return try {
            if (!NetworkUtils.isNetworkAvailable(preferenceManager.getContext())) {
                // Try to get from local DB
                val localSale = database.saleDao().getSaleById(saleId)
                if (localSale != null) {
                    val saleData = SaleData(
                        id = localSale.id,
                        invoiceNumber = localSale.invoiceNumber,
                        customerId = localSale.customerId,
                        customerName = localSale.customerName,
                        totalAmount = localSale.totalAmount,
                        paidAmount = localSale.paidAmount,
                        change = localSale.change,
                        saleType = localSale.saleType,
                        status = localSale.status,
                        paymentMethod = localSale.paymentMethod ?: "cash",
                        createdAt = localSale.createdAt,
                        items = null
                    )
                    return Resource.Success(saleData)
                } else {
                    return Resource.Error("Sale not found")
                }
            }

            println("📤 Fetching sale details for: $saleId")
            val response = apiService.getSaleDetails(shopId, saleId)

            if (response.isSuccessful) {
                val apiResponse = response.body()
                if (apiResponse?.success == true) {
                    val saleData = apiResponse.data
                    if (saleData != null) {
                        println("✅ Sale details received")
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

    // ==================== GET CUSTOMER SALE HISTORY ====================

    suspend fun getCustomerSaleHistory(customerId: String): Resource<List<SaleData>> {
        val shopId = preferenceManager.getCurrentShopId()
        if (shopId.isEmpty()) {
            return Resource.Error("No shop selected")
        }

        return try {
            if (!NetworkUtils.isNetworkAvailable(preferenceManager.getContext())) {
                // Try to get from local DB
                val localSales = database.saleDao().getSalesByCustomer(customerId).first()
                val saleDataList = localSales.map { entity ->
                    SaleData(
                        id = entity.id,
                        invoiceNumber = entity.invoiceNumber,
                        customerId = entity.customerId,
                        customerName = entity.customerName,
                        totalAmount = entity.totalAmount,
                        paidAmount = entity.paidAmount,
                        change = entity.change,
                        saleType = entity.saleType,
                        status = entity.status,
                        paymentMethod = entity.paymentMethod ?: "cash",
                        createdAt = entity.createdAt,
                        items = null
                    )
                }
                return Resource.Success(saleDataList)
            }

            println("📤 Fetching customer sale history for: $customerId")
            val response = apiService.getCustomerSaleHistory(shopId, customerId)

            if (response.isSuccessful) {
                val apiResponse = response.body()
                if (apiResponse?.success == true) {
                    val sales = apiResponse.data ?: emptyList()
                    println("✅ Received ${sales.size} customer sale history")
                    Resource.Success(sales)
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

    // ==================== RECORD PAYMENT ====================

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

                        // Update local sale status if needed
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

    // ==================== SYNC HELPERS ====================

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

    // ==================== UTILITY METHODS ====================

    private fun generateInvoiceNumber(): String {
        val dateFormat = SimpleDateFormat("yyyyMMdd", Locale.getDefault())
        val date = dateFormat.format(Date())
        val random = (1000..9999).random()
        return "INV-$date-$random"
    }

    fun calculateChange(tendered: Double, total: Double): Double {
        return (tendered - total).coerceAtLeast(0.0)
    }

    fun calculateSubtotal(items: List<CartItem>): Double {
        return items.sumOf { it.subtotal }
    }

    fun calculateTotalTax(items: List<CartItem>): Double {
        return items.sumOf { item ->
            val price = item.customPrice ?: item.product.price
            val taxRate = item.product.taxRate ?: 0.0
            price * item.quantity * taxRate / 100
        }
    }

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