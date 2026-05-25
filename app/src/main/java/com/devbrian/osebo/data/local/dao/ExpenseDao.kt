package com.devbrian.osebo.data.local.dao

import androidx.room.*
import com.devbrian.osebo.data.local.entity.ExpenseEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ExpenseDao {

    @Query("SELECT * FROM expenses WHERE shopId = :shopId ORDER BY date DESC, createdAt DESC")
    fun getExpensesByShop(shopId: String): Flow<List<ExpenseEntity>>

    @Query("SELECT * FROM expenses WHERE id = :id")
    suspend fun getExpenseById(id: String): ExpenseEntity?

    @Query("SELECT * FROM expenses WHERE shopId = :shopId AND date BETWEEN :startDate AND :endDate ORDER BY date DESC")
    fun getExpensesByDateRange(shopId: String, startDate: String, endDate: String): Flow<List<ExpenseEntity>>

    @Query("SELECT SUM(amount) FROM expenses WHERE shopId = :shopId AND date BETWEEN :startDate AND :endDate")
    suspend fun getTotalExpensesByDateRange(shopId: String, startDate: String, endDate: String): Double?

    @Query("SELECT SUM(amount) FROM expenses WHERE shopId = :shopId AND substr(date, 1, 7) = :yearMonth")
    suspend fun getTotalExpensesByMonth(shopId: String, yearMonth: String): Double?

    @Query("SELECT * FROM expenses WHERE isPendingSync = 1")
    suspend fun getPendingSyncExpenses(): List<ExpenseEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(expense: ExpenseEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(expenses: List<ExpenseEntity>)

    @Update
    suspend fun update(expense: ExpenseEntity)

    @Query("DELETE FROM expenses WHERE id = :id")
    suspend fun deleteById(id: String)

    @Delete
    suspend fun delete(expense: ExpenseEntity)

    @Query("DELETE FROM expenses WHERE shopId = :shopId")
    suspend fun deleteAllByShop(shopId: String)
}