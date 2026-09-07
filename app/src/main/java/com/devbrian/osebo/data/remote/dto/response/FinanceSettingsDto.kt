package com.devbrian.osebo.data.remote.dto.response

data class FinanceSettingsDto(
    val currency: String,
    val fiscalYearStart: String,
    val fiscalYearEnd: String,
    val taxRate: Double,
    val invoicePrefix: String,
    val enableAutomaticTax: Boolean,
    val enableReceipts: Boolean,
    val defaultPaymentMethod: String
)

data class UpdateFinanceSettingsRequest(
    val currency: String? = null,
    val taxRate: Double? = null,
    val invoicePrefix: String? = null,
    val enableAutomaticTax: Boolean? = null,
    val enableReceipts: Boolean? = null,
    val defaultPaymentMethod: String? = null
)
