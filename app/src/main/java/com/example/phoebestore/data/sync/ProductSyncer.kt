package com.example.phoebestore.data.sync

import com.example.phoebestore.data.local.dao.ProductDao
import com.example.phoebestore.data.mapper.toDomain
import com.example.phoebestore.data.mapper.toDto
import com.example.phoebestore.data.mapper.toEntity
import com.example.phoebestore.data.remote.source.ProductRemoteDataSource
import com.example.phoebestore.data.remote.storage.ImageUploader
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProductSyncer @Inject constructor(
    private val productDao: ProductDao,
    private val productRemote: ProductRemoteDataSource,
    private val imageUploader: ImageUploader,
    private val deviceIdProvider: DeviceIdProvider
) : EntitySyncer {

    override suspend fun syncWrite(entityId: Long) {
        val entity = productDao.getById(entityId) ?: return
        val p = entity.toDomain()
        val deviceId = deviceIdProvider.id
        val image = imageUploader.resolveUrl(p.imageUrl, "product-images", "products/$deviceId/${p.id}.jpg")
        if (image != p.imageUrl)
            productDao.update(p.copy(imageUrl = image).toEntity())
        productRemote.insert(p.copy(imageUrl = image, deviceId = deviceId).toDto())
    }

    override suspend fun syncDelete(entityId: Long) {
        productRemote.delete(entityId)
    }
}
