package com.devbrian.osebo.workers

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.devbrian.osebo.data.ApiClient
import com.devbrian.osebo.data.PreferenceManager
import com.devbrian.osebo.data.local.AppDatabase
import com.devbrian.osebo.data.local.entity.ProductEntity
import com.devbrian.osebo.data.local.entity.SaleEntity
import com.devbrian.osebo.data.local.entity.SyncQueueEntity
import com.devbrian.osebo.data.remote.dto.request.SaleItemRequest
import com.devbrian.osebo.data.remote.dto.request.SaleRequest
import com.devbrian.osebo.models.CartItem
import com.devbrian.osebo.utils.NetworkUtils
import com.google.gson.Gson
import kotlinx.coroutines.delay

class SyncWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    private val database = AppDatabase.getInstance(applicationContext)
    private val gson = Gson()
    private val preferenceManager = PreferenceManager.getInstance(applicationContext)
    private val apiService = ApiClient.createWithAuth(applicationContext)

    override suspend fun doWork(): Result {
        
        if (!NetworkUtils.isNetworkAvailable(applicationContext)) {
            return Result.retry()
        }

        val pendingItems = database.syncQueueDao().getPendingSyncItemsBatch()

        if (pendingItems.isEmpty()) {
            return Result.success()
        }

        var successCount = 0
        for (item in pendingItems) {
            try {
                val success = processSyncItem(item)
                if (success) {
                    database.syncQueueDao().markAsCompleted(item.id)
                    successCount++
                } else {
                    database.syncQueueDao().markAsFailed(item.id, System.currentTimeMillis())
                }
            } catch (e: Exception) {
                database.syncQueueDao().markAsFailed(item.id, System.currentTimeMillis())
            }

            delay(1000)
        }

        return if (successCount > 0) Result.success() else Result.retry()
    }

    private suspend fun processSyncItem(item: SyncQueueEntity): Boolean {
        return when (item.entityType) {
            "PRODUCT" -> processProductSync(item)
            "SALE" -> processSaleSync(item)
            else -> false
        }
    }

    // Mirrors SalesRepository.syncCreateSale's request-building and
    // success-handling exactly, so a sale queued here (no network when it
    // was made) uploads the same way a sale made with network already
    // does. Was previously missing entirely - every queued sale fell into
    // processSyncItem's `else -> false` branch and got marked FAILED,
    // never actually reaching the server.
    private suspend fun processSaleSync(item: SyncQueueEntity): Boolean {
        return try {
            val entity = gson.fromJson(item.data, SaleEntity::class.java)
            val cartItems = gson.fromJson(entity.items, Array<CartItem>::class.java).toList()

            val saleItems = cartItems.map { cartItem ->
                val unitPrice = cartItem.customPrice ?: cartItem.product.price
                val totalBeforeDiscount = unitPrice * cartItem.quantity
                val discountAmount = totalBeforeDiscount * (cartItem.discount / 100)
                SaleItemRequest(
                    stockItemId = cartItem.product.id,
                    quantity = cartItem.quantity,
                    price = unitPrice,
                    discount = cartItem.discount,
                    isCustomPrice = cartItem.isCustomPrice,
                    amount = totalBeforeDiscount - discountAmount,
                )
            }

            val request = SaleRequest(
                customerId = entity.customerId,
                paidAmount = entity.paidAmount.toString(),
                saleType = entity.saleType,
                stockItems = saleItems,
                notes = entity.notes,
            )

            val response = apiService.createSale(entity.shopId, request)
            val apiData = response.takeIf { it.isSuccessful }?.body()
                ?.takeIf { it.success }?.data
                ?: return false

            val updatedEntity = entity.copy(
                id = apiData.id,
                invoiceNumber = apiData.invoiceNumber ?: entity.invoiceNumber,
                createdAt = apiData.createdAt ?: entity.createdAt,
                status = "COMPLETED",
                isPendingSync = false,
                syncAction = null,
            )
            database.saleDao().deleteSaleById(entity.id)
            database.saleDao().insertSale(updatedEntity)
            true
        } catch (e: Exception) {
            false
        }
    }

    private suspend fun processProductSync(item: SyncQueueEntity): Boolean {
        val entity = gson.fromJson(item.data, ProductEntity::class.java)
        val shopId = entity.shopId

        return try {
            when (item.action) {
                "CREATE" -> {
                    
                    
                    true
                }
                "UPDATE" -> {
                    
                    true
                }
                "DELETE" -> {
                    
                    true
                }
                else -> false
            }
        } catch (e: Exception) {
            false
        }
    }
}


