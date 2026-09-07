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
import retrofit2.HttpException
import java.text.SimpleDateFormat
import java.util.*

class SalesRepository(
    private val database: AppDatabase,
    private val apiService: ApiService,
    private val preferenceManager: PreferenceManager,
    private val gson: Gson
) {

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

    suspend fun getTotalSalesForShop(shopId: String, limit: Int = 10000): Double {
        return try {
            println("📊 Getting total sales for shop: $shopId")
            val entities = database.saleDao().getRecentSales(shopId, limit).first()
            val totalSales = entities.sumOf { entity -> entity.totalAmount }
            println("✅ Total sales for shop $shopId: $totalSales")
            totalSales
        } catch (e: Exception) {
            println("❌ Error getting total sales for shop $shopId: ${e.message}")
            e.printStackTrace()
            0.0
        }
    }

    suspend fun getRecentSalesForShop(shopId: String, limit: Int = 20): List<Sale> {
        return try {
            println("📊 Fetching recent sales for shop: $shopId")

            if (shopId.isEmpty()) {
                println("⚠️ Shop ID is empty, returning empty list")
                return emptyList()
            }

            val entities = database.saleDao().getRecentSales(shopId, limit).first()
            val sales = entities.map { entity ->
                try {
                    entity.toSale()
                } catch (e: Exception) {
                    println("❌ Error converting entity to sale: ${e.message}")
                    null
                }
            }.filterNotNull()
                .filter { it.amount > 0 }

            println("📊 Converted sales count for shop $shopId: ${sales.size}")
            sales
        } catch (e: Exception) {
            println("❌ Error getting recent sales for shop $shopId: ${e.message}")
            e.printStackTrace()
            emptyList()
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

    suspend fun createSale(
        customer: Customer?,
        cartItems: List<CartItem>,
        paidAmount: Double,
        saleType: String = "sale",
        paymentMethod: String = "cash",
        notes: String? = null
    ): Resource<SaleData> {
        val shopId = preferenceManager.getCurrentShopId()

        println("🔍 CREATE SALE - Shop ID from preferences: '$shopId'")
        println("🔍 CREATE SALE - Shop ID length: ${shopId.length}")
        println("🔍 CREATE SALE - Shop ID empty? ${shopId.isEmpty()}")

        if (shopId.isEmpty()) {
            println("❌ CREATE SALE - ERROR: No shop selected!")
            return Resource.Error("No shop selected. Please select a shop first.")
        }

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
            val unitPrice = item.customPrice ?: item.product.price
            val quantity = item.quantity
            val discountPercentage = item.discount
            val totalBeforeDiscount = unitPrice * quantity
            val discountAmount = totalBeforeDiscount * (discountPercentage / 100)
            val amount = totalBeforeDiscount - discountAmount

            SaleItemRequest(
                stockItemId = item.product.id,
                quantity = quantity,
                price = unitPrice,
                discount = discountPercentage,
                isCustomPrice = item.isCustomPrice,
                amount = amount
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
        println("   - Total Sale Amount: $totalAmount")
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
            // Return success with local data so user can proceed to receipt
            Resource.Success(
                SaleData(
                    id = tempId,
                    invoiceNumber = invoiceNumber,
                    totalAmount = totalAmount,
                    paidAmount = paidAmount,
                    change = (paidAmount - totalAmount).coerceAtLeast(0.0),
                    paymentMethod = paymentMethod,
                    createdAt = saleEntity.createdAt,
                    shop = getCurrentShopInfo()
                )
            )
        }
    }

    private suspend fun syncCreateSale(
        entity: SaleEntity,
        request: SaleRequest,
        originalPaymentMethod: String
    ): Resource<SaleData> {
        return try {
            val shopId = preferenceManager.getCurrentShopId()

            if (shopId.isEmpty()) {
                println("❌ SYNC CREATE SALE - ERROR: Shop ID is empty! Cannot sync sale")
                queueForSync(entity, "CREATE")
                // Return success with local data so user can proceed
                return Resource.Success(
                    SaleData(
                        id = entity.id,
                        invoiceNumber = entity.invoiceNumber,
                        totalAmount = entity.totalAmount,
                        paidAmount = entity.paidAmount,
                        change = entity.change,
                        paymentMethod = originalPaymentMethod,
                        createdAt = entity.createdAt,
                        shop = getCurrentShopInfo()
                    )
                )
            }

            println("📤 Syncing sale to server: ${entity.id}")
            val response = apiService.createSale(shopId, request)

            if (response.isSuccessful) {
                val apiResponse = response.body()
                if (apiResponse?.success == true) {
                    val apiData = apiResponse.data
                    if (apiData != null) {
                        println("✅ Sale synced successfully: ${apiData.id}")

                        val totalAmount = apiData.totalPrice ?: entity.totalAmount
                        val paidAmount = try {
                            apiData.paidAmountString?.toDouble() ?: entity.paidAmount
                        } catch (e: Exception) {
                            entity.paidAmount
                        }
                        val change = (paidAmount - totalAmount).coerceAtLeast(0.0)
                        val paymentMethod = try {
                            apiData.salePayments?.firstOrNull()?.payment?.method ?: originalPaymentMethod
                        } catch (e: Exception) {
                            originalPaymentMethod
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

                        database.saleDao().deleteSaleById(entity.id)
                        database.saleDao().insertSale(updatedEntity)

                        return Resource.Success(saleData)
                    }
                }
            }

            // Handle 401 Unauthorized - Token expired
            if (response.code() == 401) {
                println("⚠️ Token expired or invalid, queuing sale for later sync")
                queueForSync(entity, "CREATE")
                // Return success with local data so user can proceed to receipt
                return Resource.Success(
                    SaleData(
                        id = entity.id,
                        invoiceNumber = entity.invoiceNumber,
                        totalAmount = entity.totalAmount,
                        paidAmount = entity.paidAmount,
                        change = entity.change,
                        paymentMethod = originalPaymentMethod,
                        createdAt = entity.createdAt,
                        shop = getCurrentShopInfo()
                    )
                )
            }

            // For other errors, queue for sync and return error
            queueForSync(entity, "CREATE")
            Resource.Error("Network error: ${response.code()}")
        } catch (e: HttpException) {
            println("❌ HTTP error during sync: ${e.code()} - ${e.message}")

            // Handle 401 from exception
            if (e.code() == 401) {
                queueForSync(entity, "CREATE")
                return Resource.Success(
                    SaleData(
                        id = entity.id,
                        invoiceNumber = entity.invoiceNumber,
                        totalAmount = entity.totalAmount,
                        paidAmount = entity.paidAmount,
                        change = entity.change,
                        paymentMethod = originalPaymentMethod,
                        createdAt = entity.createdAt,
                        shop = getCurrentShopInfo()
                    )
                )
            }

            queueForSync(entity, "CREATE")
            Resource.Error(e.message ?: "Network error")
        } catch (e: Exception) {
            println("❌ Sync error: ${e.message}")
            queueForSync(entity, "CREATE")
            // Return success with local data so user can proceed
            Resource.Success(
                SaleData(
                    id = entity.id,
                    invoiceNumber = entity.invoiceNumber,
                    totalAmount = entity.totalAmount,
                    paidAmount = entity.paidAmount,
                    change = entity.change,
                    paymentMethod = originalPaymentMethod,
                    createdAt = entity.createdAt,
                    shop = getCurrentShopInfo()
                )
            )
        }
    }

    private fun getCurrentShopInfo(): ShopInfo? {
        return try {
            val shopId = preferenceManager.getCurrentShopId()
            val shopName = preferenceManager.getCurrentShopName()
            val shopAddress = preferenceManager.getShopLocation()
            val shopPhone = preferenceManager.getShopContact()
            val businessType = preferenceManager.getBusinessType()
            val shopEmail = preferenceManager.getShopEmail() ?: ""

            ShopInfo(
                id = shopId,
                name = shopName,
                address = shopAddress,
                phone = shopPhone,
                description = businessType,
                email = shopEmail
            )
        } catch (e: Exception) {
            println("❌ Error getting shop info: ${e.message}")
            null
        }
    }

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
                    val saleDataList = apiDataList.map { apiData ->
                        SaleData(
                            id = apiData.id,
                            invoiceNumber = apiData.invoiceNumber,
                            totalAmount = apiData.totalPrice ?: 0.0,
                            paidAmount = apiData.paidAmountString?.toDouble() ?: 0.0,
                            change = 0.0,
                            paymentMethod = apiData.salePayments?.firstOrNull()?.payment?.method ?: "cash",
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
                        val saleData = SaleData(
                            id = apiData.id,
                            invoiceNumber = apiData.invoiceNumber,
                            totalAmount = apiData.totalPrice ?: 0.0,
                            paidAmount = apiData.paidAmountString?.toDouble() ?: 0.0,
                            change = 0.0,
                            paymentMethod = apiData.salePayments?.firstOrNull()?.payment?.method ?: "cash",
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
                    val saleDataList = apiDataList.map { apiData ->
                        SaleData(
                            id = apiData.id,
                            invoiceNumber = apiData.invoiceNumber,
                            totalAmount = apiData.totalPrice ?: 0.0,
                            paidAmount = apiData.paidAmountString?.toDouble() ?: 0.0,
                            change = 0.0,
                            paymentMethod = apiData.salePayments?.firstOrNull()?.payment?.method ?: "cash",
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
                    Resource.Error(apiResponse?.message ?: "Failed to record payment")
                }
            } else {
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