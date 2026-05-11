package com.example.phoebestore.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.phoebestore.data.local.entity.InventoryLogEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface InventoryLogDao {

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(log: InventoryLogEntity)

    @Query("SELECT * FROM inventory_logs WHERE storeId = :storeId ORDER BY loggedAt DESC")
    fun getByStore(storeId: String): Flow<List<InventoryLogEntity>>
}
