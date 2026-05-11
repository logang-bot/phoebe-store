package com.example.phoebestore.data.sync

import com.example.phoebestore.data.local.dao.SaleDao
import com.example.phoebestore.data.mapper.toDomain
import com.example.phoebestore.data.mapper.toDto
import com.example.phoebestore.data.remote.source.SaleRemoteDataSource
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SaleSyncer @Inject constructor(
    private val saleDao: SaleDao,
    private val saleRemote: SaleRemoteDataSource,
    private val deviceIdProvider: DeviceIdProvider
) : EntitySyncer {

    override suspend fun syncWrite(entityId: String) {
        val entity = saleDao.getById(entityId) ?: return
        saleRemote.insert(entity.toDomain().copy(deviceId = deviceIdProvider.id).toDto())
    }

    override suspend fun syncDelete(entityId: String) {
        saleRemote.delete(entityId)
    }
}
