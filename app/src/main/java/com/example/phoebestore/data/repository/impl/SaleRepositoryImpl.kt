package com.example.phoebestore.data.repository.impl

import com.example.phoebestore.data.local.dao.SaleDao
import com.example.phoebestore.data.mapper.toDomain
import com.example.phoebestore.data.mapper.toDto
import com.example.phoebestore.data.mapper.toEntity
import com.example.phoebestore.data.remote.source.SaleRemoteDataSource
import com.example.phoebestore.data.sync.DeviceIdProvider
import com.example.phoebestore.data.sync.RemoteErrorHandler
import com.example.phoebestore.domain.model.Sale
import com.example.phoebestore.domain.repository.SaleRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class SaleRepositoryImpl @Inject constructor(
    private val dao: SaleDao,
    private val remote: SaleRemoteDataSource,
    private val errorHandler: RemoteErrorHandler,
    private val deviceIdProvider: DeviceIdProvider
) : SaleRepository {

    override suspend fun create(sale: Sale): Long {
        val id = dao.insert(sale.toEntity())
        runCatching { remote.insert(sale.copy(id = id, deviceId = deviceIdProvider.id).toDto()) }
            .onFailure { errorHandler.log("SaleCreate", it) }
        return id
    }

    override suspend fun update(sale: Sale) {
        dao.update(sale.toEntity())
        runCatching { remote.update(sale.copy(deviceId = deviceIdProvider.id).toDto()) }
            .onFailure { errorHandler.log("SaleUpdate", it) }
    }

    override suspend fun getById(id: Long): Sale? =
        dao.getById(id)?.toDomain()

    override fun getByStore(storeId: Long): Flow<List<Sale>> =
        dao.getByStore(storeId).map { list -> list.map { it.toDomain() } }

    override fun getOnCreditByStore(storeId: Long): Flow<List<Sale>> =
        dao.getOnCreditByStore(storeId).map { list -> list.map { it.toDomain() } }

    override suspend fun delete(id: Long) {
        dao.deleteById(id)
        runCatching { remote.delete(id) }
            .onFailure { errorHandler.log("SaleDelete", it) }
    }
}
