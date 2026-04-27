package com.example.phoebestore.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import androidx.room.Upsert
import com.example.phoebestore.data.local.entity.SaleEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SaleDao {

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(sale: SaleEntity): Long

    @Upsert
    suspend fun upsert(sale: SaleEntity)

    @Update
    suspend fun update(sale: SaleEntity)

    @Query("SELECT * FROM sales WHERE id = :id")
    suspend fun getById(id: Long): SaleEntity?

    @Query("SELECT * FROM sales WHERE storeId = :storeId ORDER BY soldAt DESC")
    fun getByStore(storeId: Long): Flow<List<SaleEntity>>

    @Query("SELECT * FROM sales WHERE storeId = :storeId AND onCredit = 1 ORDER BY soldAt DESC")
    fun getOnCreditByStore(storeId: Long): Flow<List<SaleEntity>>

    @Query("DELETE FROM sales WHERE id = :id")
    suspend fun deleteById(id: Long)
}
