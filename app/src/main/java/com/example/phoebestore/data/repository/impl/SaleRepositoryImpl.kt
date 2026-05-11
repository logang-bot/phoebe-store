package com.example.phoebestore.data.repository.impl

import com.example.phoebestore.data.local.dao.SaleDao
import com.example.phoebestore.data.local.entity.SyncOperationEntity
import com.example.phoebestore.data.mapper.toDomain
import com.example.phoebestore.data.mapper.toEntity
import com.example.phoebestore.data.sync.SyncScheduler
import com.example.phoebestore.domain.model.Sale
import com.example.phoebestore.domain.repository.SaleRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.UUID
import javax.inject.Inject

class SaleRepositoryImpl @Inject constructor(
    private val dao: SaleDao,
    private val syncScheduler: SyncScheduler
) : SaleRepository {

    override suspend fun create(sale: Sale): String {
        val id = UUID.randomUUID().toString()
        dao.insert(sale.copy(id = id).toEntity())
        syncScheduler.enqueue(SyncOperationEntity.TYPE_SALE, id, SyncOperationEntity.OP_CREATE)
        return id
    }

    override suspend fun update(sale: Sale) {
        dao.update(sale.toEntity())
        syncScheduler.enqueue(SyncOperationEntity.TYPE_SALE, sale.id, SyncOperationEntity.OP_UPDATE)
    }

    override suspend fun getById(id: String): Sale? =
        dao.getById(id)?.toDomain()

    override fun getByStore(storeId: String): Flow<List<Sale>> =
        dao.getByStore(storeId).map { list -> list.map { it.toDomain() } }

    override fun getOnCreditByStore(storeId: String): Flow<List<Sale>> =
        dao.getOnCreditByStore(storeId).map { list -> list.map { it.toDomain() } }

    override suspend fun delete(id: String) {
        dao.deleteById(id)
        syncScheduler.enqueue(SyncOperationEntity.TYPE_SALE, id, SyncOperationEntity.OP_DELETE)
    }
}
