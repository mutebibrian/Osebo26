package com.devbrian.osebo.data.remote.dto.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * GET /api/v1/general-ledger — one row per sale/procurement/expense posting.
 * Mirrors the shape androidMain/data/models/FinancialStatement.kt's
 * LedgerTransaction/PaymentInfo already parse via Gson, just with explicit
 * kotlinx @SerialName annotations for Ktor.
 */
@Serializable
data class LedgerStatementDto(
    @SerialName("data") val data: List<LedgerTransactionDto>? = null,
    @SerialName("total") val total: Int = 0,
    @SerialName("page") val page: Int = 0,
    @SerialName("limit") val limit: Int = 0,
)

@Serializable
data class LedgerTransactionDto(
    @SerialName("id") val id: String,
    @SerialName("createdAt") val createdAt: String,
    @SerialName("updatedAt") val updatedAt: String,
    @SerialName("name") val name: String,
    @SerialName("description") val description: String,
    @SerialName("transactionId") val transactionId: String,
    @SerialName("transaction_type") val transactionType: String,
    @SerialName("payment") val payment: LedgerPaymentDto? = null,
)

@Serializable
data class LedgerPaymentDto(
    @SerialName("id") val id: String,
    @SerialName("method") val method: String? = null,
    @SerialName("reference") val reference: String? = null,
    @SerialName("amount") val amount: Double = 0.0,
    @SerialName("status") val status: String? = null,
    @SerialName("description") val description: String? = null,
    @SerialName("type") val type: String? = null,
)
