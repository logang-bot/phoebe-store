package com.example.phoebestore.domain.repository

import com.example.phoebestore.domain.model.InventoryLog
import kotlinx.coroutines.flow.Flow

interface InventoryLogRepository {
    suspend fun log(entry: InventoryLog)
    fun getByStore(storeId: Long): Flow<List<InventoryLog>>
}
