package com.devbrian.osebo.data.repository

import com.devbrian.osebo.data.ApiService
import com.devbrian.osebo.data.PreferenceManager
import com.devbrian.osebo.data.remote.api.TransferDto
import com.devbrian.osebo.data.remote.dto.request.CreateMultiStockTransferRequest
import com.devbrian.osebo.data.remote.dto.request.CreateStockTransferRequest
import com.devbrian.osebo.data.remote.dto.request.TransferApprovalRequest
import com.devbrian.osebo.data.remote.dto.request.TransferItemRequest
import com.devbrian.osebo.models.Product
import com.devbrian.osebo.utils.Resource

class TransferRepository(
    private val apiService: ApiService,
    private val preferenceManager: PreferenceManager
) {

    suspend fun getTransfersForItem(shopId: String, stockItemId: String): Resource<List<TransferDto>> {
        return try {
            val response = apiService.getStockTransfers(shopId, stockItemId)
            val body = response.body()
            if (response.isSuccessful && body?.success == true) {
                Resource.Success(body.data ?: emptyList())
            } else {
                Resource.Error(body?.message ?: "Failed to load transfers")
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to load transfers")
        }
    }

    suspend fun createSingleTransfer(
        sourceShopId: String,
        targetShopId: String,
        stockItemId: String,
        quantity: Double
    ): Resource<TransferDto> {
        return try {
            val response = apiService.createStockTransfer(
                shopId = sourceShopId,
                stockItemId = stockItemId,
                request = CreateStockTransferRequest(
                    sourceShop = sourceShopId,
                    targetShop = targetShopId,
                    quantity = quantity
                )
            )
            val body = response.body()
            if (response.isSuccessful && body?.success == true && body.data != null) {
                Resource.Success(body.data)
            } else {
                Resource.Error(body?.message ?: "Failed to create transfer")
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to create transfer")
        }
    }

    suspend fun createMultiTransfer(
        sourceShopId: String,
        targetShopId: String,
        items: List<TransferItemRequest>
    ): Resource<TransferDto> {
        if (items.isEmpty()) {
            return Resource.Error("No items to transfer")
        }
        return try {
            val response = apiService.createMultiStockTransfer(
                shopId = sourceShopId,
                request = CreateMultiStockTransferRequest(
                    sourceShop = sourceShopId,
                    targetShop = targetShopId,
                    items = items
                )
            )
            val body = response.body()
            if (response.isSuccessful && body?.success == true && body.data != null) {
                Resource.Success(body.data)
            } else {
                Resource.Error(body?.message ?: "Failed to create transfer")
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to create transfer")
        }
    }

    suspend fun setTransferApproval(
        shopId: String,
        stockItemId: String,
        transferId: String,
        approved: Boolean
    ): Resource<TransferDto> {
        return try {
            val status = if (approved) TransferApprovalRequest.APPROVED else TransferApprovalRequest.REJECTED
            val response = apiService.updateStockTransferApproval(
                shopId = shopId,
                stockItemId = stockItemId,
                transferId = transferId,
                request = TransferApprovalRequest(status)
            )
            val body = response.body()
            if (response.isSuccessful && body?.success == true && body.data != null) {
                Resource.Success(body.data)
            } else {
                Resource.Error(body?.message ?: "Failed to update transfer")
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to update transfer")
        }
    }

    suspend fun findItemByBarcode(shopId: String, barcode: String): Resource<Product> {
        return try {
            val token = "Bearer ${preferenceManager.getAuthToken()}"
            val response = apiService.searchProductByBarcode(token, shopId, barcode)
            val body = response.body()
            if (response.isSuccessful && body?.success == true && body.data != null) {
                Resource.Success(body.data)
            } else {
                Resource.Error(body?.message ?: "Item not found for barcode $barcode")
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Item not found for barcode $barcode")
        }
    }

    suspend fun findItemsByName(shopId: String, query: String): Resource<List<Product>> {
        return try {
            val token = "Bearer ${preferenceManager.getAuthToken()}"
            val response = apiService.searchProducts(token, shopId, query)
            val body = response.body()
            if (response.isSuccessful && body?.success == true) {
                Resource.Success(body.data ?: emptyList())
            } else {
                Resource.Error(body?.message ?: "No items found for \"$query\"")
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "No items found for \"$query\"")
        }
    }
}
