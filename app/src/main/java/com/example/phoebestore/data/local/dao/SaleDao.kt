package com.example.phoebestore.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.phoebestore.data.local.entity.SaleEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SaleDao {

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(sale: SaleEntity): Long

    @Query("SELECT * FROM sales WHERE id = :id")
    suspend fun getById(id: Long): SaleEntity?

    @Query("SELECT * FROM sales WHERE storeId = :storeId ORDER BY soldAt DESC")
    fun getByStore(storeId: Long): Flow<List<SaleEntity>>

    @Query("DELETE FROM sales WHERE id = :id")
    suspend fun deleteById(id: Long)
}
