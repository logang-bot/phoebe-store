package com.example.phoebestore.data.sync

import com.example.phoebestore.data.local.dao.StoreDao
import com.example.phoebestore.data.mapper.toDomain
import com.example.phoebestore.data.mapper.toDto
import com.example.phoebestore.data.mapper.toEntity
import com.example.phoebestore.data.remote.source.StoreRemoteDataSource
import com.example.phoebestore.data.remote.storage.ImageUploader
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StoreSyncer @Inject constructor(
    private val storeDao: StoreDao,
    private val storeRemote: StoreRemoteDataSource,
    private val imageUploader: ImageUploader,
    private val deviceIdProvider: DeviceIdProvider
) : EntitySyncer {

    override suspend fun syncWrite(entityId: String) {
        val entity = storeDao.getById(entityId) ?: return
        val s = entity.toDomain()
        val deviceId = deviceIdProvider.id
        val logo = imageUploader.resolveUrl(s.logoUrl, "store-images", "logos/$deviceId/${s.id}.jpg")
        val photo = imageUploader.resolveUrl(s.photoUrl, "store-images", "photos/$deviceId/${s.id}.jpg")
        if (logo != s.logoUrl || photo != s.photoUrl)
            storeDao.update(s.copy(logoUrl = logo, photoUrl = photo).toEntity())
        storeRemote.insert(s.copy(logoUrl = logo, photoUrl = photo, deviceId = deviceId).toDto())
    }

    override suspend fun syncDelete(entityId: String) {
        storeRemote.delete(entityId)
    }
}
