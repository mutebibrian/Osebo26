package com.devbrian.osebo.data.local.dao


import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.devbrian.osebo.data.local.entity.CustomerEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CustomerDao {

    @Query("SELECT * FROM customers WHERE shopId = :shopId ORDER BY name ASC")
    fun getAllCustomers(shopId: String): Flow<List<CustomerEntity>>

    @Query("SELECT * FROM customers WHERE shopId = :shopId AND isDefault = 1")
    fun getDefaultCustomers(shopId: String): Flow<List<CustomerEntity>>

    @Query("SELECT * FROM customers WHERE shopId = :shopId AND status = 'active' ORDER BY totalSpent DESC LIMIT 10")
    fun getTopCustomers(shopId: String): Flow<List<CustomerEntity>>

    @Query("SELECT * FROM customers WHERE id = :customerId")
    suspend fun getCustomerById(customerId: String): CustomerEntity?

    @Query("SELECT * FROM customers WHERE shopId = :shopId AND (name LIKE '%' || :query || '%' OR phone LIKE '%' || :query || '%' OR email LIKE '%' || :query || '%')")
    fun searchCustomers(shopId: String, query: String): Flow<List<CustomerEntity>>

    @Query("SELECT COUNT(*) FROM customers WHERE shopId = :shopId")
    suspend fun getCustomerCount(shopId: String): Int

    @Query("SELECT SUM(totalSpent) FROM customers WHERE shopId = :shopId")
    suspend fun getTotalCustomerSales(shopId: String): Double?

    @Query("SELECT COUNT(*) FROM customers WHERE shopId = :shopId AND createdAt LIKE '%' || :month || '%'")
    suspend fun getNewCustomersCount(shopId: String, month: String): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCustomer(customer: CustomerEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllCustomers(customers: List<CustomerEntity>)

    @Update
    suspend fun updateCustomer(customer: CustomerEntity)

    @Delete
    suspend fun deleteCustomer(customer: CustomerEntity)

    @Query("DELETE FROM customers WHERE id = :customerId")
    suspend fun deleteCustomerById(customerId: String)

    @Query("SELECT * FROM customers WHERE isPendingSync = 1")
    suspend fun getPendingSyncCustomers(): List<CustomerEntity>

    @Query("UPDATE customers SET isPendingSync = 0, syncAction = NULL WHERE id IN (:customerIds)")
    suspend fun markAsSynced(customerIds: List<String>)

    @Query("DELETE FROM customers WHERE shopId = :shopId")
    suspend fun clearCustomers(shopId: String)

    @Transaction
    suspend fun syncCustomers(customers: List<CustomerEntity>, shopId: String) {
        clearCustomers(shopId)
        insertAllCustomers(customers)
    }
}


