package com.example.phoebestore.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.phoebestore.data.local.entity.SyncOperationEntity

@Dao
interface SyncOperationDao {

    @Insert
    suspend fun insert(op: SyncOperationEntity)

    @Query("SELECT * FROM pending_sync_ops ORDER BY createdAt ASC")
    suspend fun getAll(): List<SyncOperationEntity>

    @Query("DELETE FROM pending_sync_ops WHERE id = :id")
    suspend fun deleteById(id: Long)
}
