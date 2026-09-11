package com.devbrian.osebo.data.local.dao


import androidx.room.*
import com.devbrian.osebo.data.local.entity.CategoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CategoryDao {

    @Query("SELECT * FROM categories WHERE shopId = :shopId ORDER BY name ASC")
    fun getCategoriesByShop(shopId: String): Flow<List<CategoryEntity>>

    @Query("SELECT * FROM categories WHERE remoteId = :remoteId")
    suspend fun getCategoryByRemoteId(remoteId: String): CategoryEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategory(category: CategoryEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllCategories(categories: List<CategoryEntity>)

    @Update
    suspend fun updateCategory(category: CategoryEntity)

    @Delete
    suspend fun deleteCategory(category: CategoryEntity)

    @Query("SELECT * FROM categories WHERE isPendingSync = 1")
    suspend fun getPendingSyncCategories(): List<CategoryEntity>
}


