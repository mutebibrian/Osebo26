package com.devbrian.osebo.data.repository

import com.devbrian.osebo.data.remote.ApiResult
import com.devbrian.osebo.data.remote.KtorOseboApiService
import com.devbrian.osebo.data.remote.api.SupplierDto
import com.devbrian.osebo.data.remote.dto.request.AddSupplierRequest
import com.devbrian.osebo.data.remote.dto.request.UpdateSupplierRequest
import com.devbrian.osebo.ui.screens.SupplierItemUi

/**
 * Mirrors SuppliersFragment on Android: list, create, update, delete (with
 * a confirmation step first — see SupplierFormDialog's onDelete and App()'s
 * supplierPendingDelete, matching Android's confirmDeleteSupplier).
 */
class SharedSupplierRepository(private val api: KtorOseboApiService) {
    suspend fun getSuppliers(shopId: String): ApiResult<List<SupplierDto>> =
        when (val result = api.getSuppliers(shopId)) {
            is ApiResult.Success -> ApiResult.Success(result.data.data.orEmpty(), result.httpCode)
            is ApiResult.Error -> result
            is ApiResult.NetworkError -> result
        }

    suspend fun createSupplier(shopId: String, request: AddSupplierRequest): ApiResult<SupplierDto> =
        when (val result = api.createSupplier(shopId, request)) {
            is ApiResult.Success -> result.data.data?.let { ApiResult.Success(it, result.httpCode) }
                ?: ApiResult.Error(result.httpCode, "No supplier returned")
            is ApiResult.Error -> result
            is ApiResult.NetworkError -> result
        }

    suspend fun updateSupplier(shopId: String, supplierId: String, request: UpdateSupplierRequest): ApiResult<SupplierDto> =
        when (val result = api.updateSupplier(shopId, supplierId, request)) {
            is ApiResult.Success -> result.data.data?.let { ApiResult.Success(it, result.httpCode) }
                ?: ApiResult.Error(result.httpCode, "No supplier returned")
            is ApiResult.Error -> result
            is ApiResult.NetworkError -> result
        }

    suspend fun deleteSupplier(shopId: String, supplierId: String): ApiResult<Unit> =
        when (val result = api.deleteSupplier(shopId, supplierId)) {
            is ApiResult.Success -> ApiResult.Success(Unit, result.httpCode)
            is ApiResult.Error -> result
            is ApiResult.NetworkError -> result
        }
}

fun SupplierDto.toSupplierItemUi(): SupplierItemUi {
    val initials = name.split(" ").filter { it.isNotBlank() }.take(2)
        .joinToString("") { it.first().uppercaseChar().toString() }.ifBlank { "SP" }
    return SupplierItemUi(
        id = id,
        name = name,
        initials = initials,
        contactPerson = contactPerson.orEmpty(),
        phone = phone.orEmpty(),
        email = email.orEmpty(),
        address = address.orEmpty(),
        products = numberOfProducts ?: 0,
        totalPurchases = totalPurchases ?: 0.0,
    )
}
