package com.example.phoebestore.data.sync

import com.example.phoebestore.data.local.dao.ProductDao
import com.example.phoebestore.data.local.dao.SaleDao
import com.example.phoebestore.data.local.dao.StoreDao
import com.example.phoebestore.data.mapper.toDomain
import com.example.phoebestore.data.mapper.toDto
import com.example.phoebestore.data.mapper.toEntity
import com.example.phoebestore.data.remote.dto.StoreDto
import com.example.phoebestore.data.remote.source.ProductRemoteDataSource
import com.example.phoebestore.data.remote.source.SaleRemoteDataSource
import com.example.phoebestore.data.remote.source.StoreRemoteDataSource
import com.example.phoebestore.data.remote.storage.ImageUploader
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import java.io.File
import java.net.URI
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SyncManager @Inject constructor(
    private val storeRemote: StoreRemoteDataSource,
    private val productRemote: ProductRemoteDataSource,
    private val saleRemote: SaleRemoteDataSource,
    private val storeDao: StoreDao,
    private val productDao: ProductDao,
    private val saleDao: SaleDao,
    private val errorHandler: RemoteErrorHandler,
    private val imageUploader: ImageUploader,
    private val deviceIdProvider: DeviceIdProvider
) {
    private val _isSyncing = MutableStateFlow(false)
    val isSyncing: StateFlow<Boolean> = _isSyncing.asStateFlow()

    suspend fun runSync() {
        _isSyncing.value = true
        try {
            val remoteStores = try {
                storeRemote.getAll()
            } catch (e: Exception) {
                errorHandler.notify(
                    "Sync", e,
                    "Could not connect to the server. Your data will sync when back online."
                )
                return
            }

            when {
                remoteStores.isNotEmpty() -> runCatching { pullAll(remoteStores) }
                    .onFailure {
                        errorHandler.notify(
                            "PullSync", it,
                            "Failed to restore your data. Check your connection and try again."
                        )
                    }

                storeDao.getAll().first().isNotEmpty() -> runCatching { pushAll() }
                    .onFailure {
                        errorHandler.notify(
                            "PushSync", it,
                            "Failed to back up your local data. Check your connection and try again."
                        )
                    }
            }
        } finally {
            _isSyncing.value = false
        }
    }

    private suspend fun pullAll(stores: List<StoreDto>) {
        stores.forEach { storeDao.upsert(it.toDomain().toEntity()) }
        stores.forEach { store ->
            productRemote.getByStore(store.id).forEach { productDao.upsert(it.toDomain().toEntity()) }
            saleRemote.getByStore(store.id).forEach { saleDao.upsert(it.toDomain().toEntity()) }
        }
    }

    private suspend fun pushAll() {
        val deviceId = deviceIdProvider.id
        val stores = storeDao.getAll().first()
        stores.forEach { storeEntity ->
            val s = storeEntity.toDomain()
            val logo = imageUploader.resolveUrl(s.logoUrl, "store-images", "logos/$deviceId/${s.id}.jpg")
            val photo = imageUploader.resolveUrl(s.photoUrl, "store-images", "photos/$deviceId/${s.id}.jpg")
            if (logo != s.logoUrl || photo != s.photoUrl)
                storeDao.update(s.copy(logoUrl = logo, photoUrl = photo).toEntity())
            storeRemote.insert(s.copy(logoUrl = logo, photoUrl = photo, deviceId = deviceId).toDto())

            productDao.getByStore(s.id).first().forEach { productEntity ->
                val p = productEntity.toDomain()
                val image = imageUploader.resolveUrl(p.imageUrl, "product-images", "products/$deviceId/${p.id}.jpg")
                if (image != p.imageUrl) productDao.update(p.copy(imageUrl = image).toEntity())
                productRemote.insert(p.copy(imageUrl = image, deviceId = deviceId).toDto())
            }

            saleDao.getByStore(s.id).first().forEach { saleEntity ->
                saleRemote.insert(saleEntity.toDomain().copy(deviceId = deviceId).toDto())
            }
        }
    }

    suspend fun repairLocalImageUrls() {
        val deviceId = deviceIdProvider.id
        val stores = storeDao.getAll().first()
        if (stores.isEmpty()) return

        val hasLocalUrls = stores.any { e ->
            val s = e.toDomain()
            s.logoUrl.startsWith("file://") || s.photoUrl.startsWith("file://")
        } || stores.any { e ->
            productDao.getByStore(e.id).first().any { it.toDomain().imageUrl.startsWith("file://") }
        }
        if (!hasLocalUrls) return

        _isSyncing.value = true
        try {
            stores.forEach { storeEntity ->
                val s = storeEntity.toDomain()
                val logo  = repairUrl(s.logoUrl,  "store-images", "logos/$deviceId/${s.id}.jpg")
                val photo = repairUrl(s.photoUrl, "store-images", "photos/$deviceId/${s.id}.jpg")
                if (logo != s.logoUrl || photo != s.photoUrl) {
                    storeDao.update(s.copy(logoUrl = logo, photoUrl = photo).toEntity())
                    runCatching {
                        storeRemote.update(s.copy(logoUrl = logo, photoUrl = photo, deviceId = deviceId).toDto())
                    }.onFailure { errorHandler.log("StoreRepair", it) }
                }
                productDao.getByStore(s.id).first().forEach { pEntity ->
                    val p = pEntity.toDomain()
                    val img = repairUrl(p.imageUrl, "product-images", "products/$deviceId/${p.id}.jpg")
                    if (img != p.imageUrl) {
                        productDao.update(p.copy(imageUrl = img).toEntity())
                        runCatching {
                            productRemote.update(p.copy(imageUrl = img, deviceId = deviceId).toDto())
                        }.onFailure { errorHandler.log("ProductRepair", it) }
                    }
                }
            }
        } finally {
            _isSyncing.value = false
        }
    }

    private suspend fun repairUrl(url: String, bucket: String, remotePath: String): String {
        if (url.isBlank() || url.startsWith("https://")) return url
        val fileExists = runCatching {
            withContext(Dispatchers.IO) { File(URI.create(url)).exists() }
        }.getOrDefault(false)
        return if (!fileExists) "" else imageUploader.resolveUrl(url, bucket, remotePath)
    }

}
