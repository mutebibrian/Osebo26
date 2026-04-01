package com.devbrian.osebo.data.local.entity


import androidx.room.Entity
import androidx.room.PrimaryKey
import com.devbrian.osebo.data.remote.dto.response.ShopDto
import com.devbrian.osebo.models.Shop
import java.util.UUID

@Entity(tableName = "shops")
data class ShopEntity(
    @PrimaryKey
    val id: String,
    val uuid: String?,
    val name: String,
    val address: String?,
    val description: String?,
    val shopType: String?,
    val phone: String?,
    val email: String?,
    val registrationNumber: String?,
    val taxIdentificationNumber: String?,
    val logoUrl: String?,
    val totalRevenue: Double,
    val totalExpenses: Double,
    val profit: Double,
    val totalProducts: Int,
    val totalEmployees: Int,
    val subscriptionStatus: String,
    val subscriptionType: String?,
    val subscriptionExpiry: String?,
    val planId: String?,
    val isActive: Boolean,
    val status: String,
    val ownerId: String,
    val createdAt: String?,
    val updatedAt: String?,
    val lastSyncedAt: Long = System.currentTimeMillis()
) {
    fun toShop(): Shop {
        return Shop(
            id = this.id,
            uuid = this.uuid,
            name = this.name,
            address = this.address,
            description = this.description,
            shopType = this.shopType,
            phone = this.phone,
            email = this.email,
            registrationNumber = this.registrationNumber,
            taxIdentificationNumber = this.taxIdentificationNumber,
            logoUrl = this.logoUrl,
            totalRevenue = this.totalRevenue,
            totalExpenses = this.totalExpenses,
            profit = this.profit,
            totalProducts = this.totalProducts,
            totalEmployees = this.totalEmployees,
            subscriptionStatus = this.subscriptionStatus,
            subscriptionType = this.subscriptionType,
            subscriptionExpiry = this.subscriptionExpiry,
            planId = this.planId,
            isActive = this.isActive,
            status = this.status,
            ownerId = this.ownerId,
            createdAt = this.createdAt,
            updatedAt = this.updatedAt,
            subscription = null
        )
    }

    companion object {
        fun fromDto(dto: ShopDto, userId: String): ShopEntity {
            val uuid = try {
                UUID.fromString(dto.id)
                dto.id
            } catch (e: IllegalArgumentException) {
                null
            }

            // Use the helper properties from ShopDto
            val subscriptionStatus = dto.subscriptionStatusString
            val subscriptionExpiry = dto.subscriptionExpiryString
            val subscriptionType = dto.subscriptionTypeString

            return ShopEntity(
                id = dto.id,
                uuid = uuid,
                name = dto.name,
                address = dto.address,
                description = dto.description,
                shopType = dto.shopType ?: dto.shopTypeObject?.name,
                phone = dto.phone,
                email = dto.email,
                registrationNumber = dto.registrationNumber,
                taxIdentificationNumber = dto.taxIdentificationNumber,
                logoUrl = dto.logoUrl,
                totalRevenue = 0.0,
                totalExpenses = 0.0,
                profit = 0.0,
                totalProducts = 0,
                totalEmployees = (dto.employees?.managers ?: 0) + (dto.employees?.staff ?: 0),
                subscriptionStatus = subscriptionStatus,
                subscriptionType = subscriptionType,
                subscriptionExpiry = subscriptionExpiry,
                planId = dto.subscription?.packageDetails?.id,
                isActive = dto.isActive,
                status = dto.status ?: "active",
                ownerId = dto.ownerId ?: userId,
                createdAt = dto.createdAt,
                updatedAt = dto.updatedAt
            )
        }
    }
}