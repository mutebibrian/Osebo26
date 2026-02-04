package com.devbrian.osebo.data.mapper


import com.devbrian.osebo.data.remote.dto.response.AccountDto
import com.devbrian.osebo.domain.model.Account
import javax.inject.Inject

class AccountMapper @Inject constructor() {

    fun toDomain(dto: AccountDto): Account {
        return Account(
            id = dto.id,
            businessName = dto.businessName,
            businessType = dto.businessType,
            registrationNumber = dto.registrationNumber,
            taxId = dto.taxId,
            address = dto.address,
            status = dto.status,
            paymentMethod = dto.paymentMethod,
            billingCycle = dto.billingCycle,
            nextBillingDate = dto.nextBillingDate,
            twoFactorEnabled = dto.twoFactorEnabled,
            loginNotificationsEnabled = dto.loginNotificationsEnabled,
            createdAt = dto.createdAt,
            updatedAt = dto.updatedAt
        )
    }
}