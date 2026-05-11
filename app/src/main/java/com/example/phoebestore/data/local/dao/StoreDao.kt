package com.example.phoebestore.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import androidx.room.Upsert
import com.example.phoebestore.data.local.entity.StoreEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface StoreDao {

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(store: StoreEntity)

    @Upsert
    suspend fun upsert(store: StoreEntity)

    @Update
    suspend fun update(store: StoreEntity)

    @Query("SELECT * FROM stores WHERE id = :id")
    suspend fun getById(id: String): StoreEntity?

    @Query("SELECT * FROM stores ORDER BY CASE WHEN lastAccessedAt > 0 THEN lastAccessedAt ELSE createdAt END DESC")
    fun getAll(): Flow<List<StoreEntity>>

    @Query("UPDATE stores SET lastAccessedAt = :timestamp WHERE id = :id")
    suspend fun updateLastAccessed(id: String, timestamp: Long)

    @Query("DELETE FROM stores WHERE id = :id")
    suspend fun deleteById(id: String)
}
