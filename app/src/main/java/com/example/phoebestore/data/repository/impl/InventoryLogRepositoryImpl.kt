package com.example.phoebestore.data.repository.impl

import com.example.phoebestore.data.local.dao.InventoryLogDao
import com.example.phoebestore.data.mapper.toDomain
import com.example.phoebestore.data.mapper.toEntity
import com.example.phoebestore.domain.model.InventoryLog
import com.example.phoebestore.domain.repository.InventoryLogRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class InventoryLogRepositoryImpl @Inject constructor(
    private val dao: InventoryLogDao
) : InventoryLogRepository {

    override suspend fun log(entry: InventoryLog) {
        dao.insert(entry.toEntity())
    }

    override fun getByStore(storeId: Long): Flow<List<InventoryLog>> =
        dao.getByStore(storeId).map { list -> list.map { it.toDomain() } }
}
