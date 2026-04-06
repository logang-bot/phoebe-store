package com.example.phoebestore.data.repository.impl

import com.example.phoebestore.data.local.dao.StoreDao
import com.example.phoebestore.data.mapper.toDomain
import com.example.phoebestore.data.mapper.toEntity
import com.example.phoebestore.domain.model.Store
import com.example.phoebestore.domain.repository.StoreRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class StoreRepositoryImpl @Inject constructor(
    private val dao: StoreDao
) : StoreRepository {

    override suspend fun create(store: Store): Long =
        dao.insert(store.toEntity())

    override suspend fun update(store: Store) =
        dao.update(store.toEntity())

    override suspend fun getById(id: Long): Store? =
        dao.getById(id)?.toDomain()

    override fun getAll(): Flow<List<Store>> =
        dao.getAll().map { list -> list.map { it.toDomain() } }

    override suspend fun delete(id: Long) =
        dao.deleteById(id)

    override suspend fun markAccessed(id: Long) =
        dao.updateLastAccessed(id, System.currentTimeMillis())
}
