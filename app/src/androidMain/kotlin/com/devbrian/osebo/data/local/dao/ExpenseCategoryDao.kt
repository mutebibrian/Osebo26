package com.devbrian.osebo.data.local.dao

import androidx.room.*
import com.devbrian.osebo.data.local.entity.ExpenseCategoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ExpenseCategoryDao {

    @Query("SELECT * FROM expense_categories WHERE shopId = :shopId ORDER BY name")
    fun getCategoriesByShop(shopId: String): Flow<List<ExpenseCategoryEntity>>

    @Query("SELECT * FROM expense_categories WHERE id = :id")
    suspend fun getCategoryById(id: String): ExpenseCategoryEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(category: ExpenseCategoryEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(categories: List<ExpenseCategoryEntity>)

    @Update
    suspend fun update(category: ExpenseCategoryEntity)

    @Query("DELETE FROM expense_categories WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("DELETE FROM expense_categories WHERE shopId = :shopId")
    suspend fun deleteAllByShop(shopId: String)
}