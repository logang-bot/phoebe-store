package com.example.phoebestore.data.repository.impl

import com.example.phoebestore.data.local.dao.StoreDao
import com.example.phoebestore.data.local.entity.SyncOperationEntity
import com.example.phoebestore.data.mapper.toDomain
import com.example.phoebestore.data.mapper.toEntity
import com.example.phoebestore.data.sync.SyncScheduler
import com.example.phoebestore.domain.model.Store
import com.example.phoebestore.domain.repository.StoreRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.UUID
import javax.inject.Inject

class StoreRepositoryImpl @Inject constructor(
    private val dao: StoreDao,
    private val syncScheduler: SyncScheduler
) : StoreRepository {

    override suspend fun create(store: Store): String {
        val id = UUID.randomUUID().toString()
        dao.insert(store.copy(id = id).toEntity())
        syncScheduler.enqueue(SyncOperationEntity.TYPE_STORE, id, SyncOperationEntity.OP_CREATE)
        return id
    }

    override suspend fun update(store: Store) {
        dao.update(store.toEntity())
        syncScheduler.enqueue(SyncOperationEntity.TYPE_STORE, store.id, SyncOperationEntity.OP_UPDATE)
    }

    override suspend fun getById(id: String): Store? =
        dao.getById(id)?.toDomain()

    override fun getAll(): Flow<List<Store>> =
        dao.getAll().map { list -> list.map { it.toDomain() } }

    override suspend fun delete(id: String) {
        dao.deleteById(id)
        syncScheduler.enqueue(SyncOperationEntity.TYPE_STORE, id, SyncOperationEntity.OP_DELETE)
    }

    override suspend fun markAccessed(id: String) =
        dao.updateLastAccessed(id, System.currentTimeMillis())
}
