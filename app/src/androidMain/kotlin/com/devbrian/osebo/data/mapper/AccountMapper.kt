package com.devbrian.osebo.data.mapper

import com.devbrian.osebo.data.remote.dto.response.AccountDto
import com.devbrian.osebo.domain.model.Account
import com.devbrian.osebo.models.ApiResponse

class AccountMapper() {

    fun toDomain(response: ApiResponse<AccountDto>): Account {
        val accountData = response.data ?: throw IllegalArgumentException("Account data is null in API response")

        return Account(
            id = accountData.id,
            businessName = accountData.businessName,
            businessType = accountData.businessType,
            registrationNumber = accountData.registrationNumber,
            taxId = accountData.taxId,
            address = accountData.address,
            status = accountData.status,
            paymentMethod = accountData.paymentMethod,
            billingCycle = accountData.billingCycle,
            nextBillingDate = accountData.nextBillingDate,
            twoFactorEnabled = accountData.twoFactorEnabled,
            loginNotificationsEnabled = accountData.loginNotificationsEnabled,
            createdAt = accountData.createdAt,
            updatedAt = accountData.updatedAt
        )
    }
}