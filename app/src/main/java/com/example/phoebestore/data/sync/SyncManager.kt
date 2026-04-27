package com.example.phoebestore.data.sync

import android.content.Context
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
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SyncManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val storeRemote: StoreRemoteDataSource,
    private val productRemote: ProductRemoteDataSource,
    private val saleRemote: SaleRemoteDataSource,
    private val storeDao: StoreDao,
    private val productDao: ProductDao,
    private val saleDao: SaleDao,
    private val errorHandler: RemoteErrorHandler
) {
    private val prefs = context.getSharedPreferences("sync_prefs", Context.MODE_PRIVATE)

    private val _isSyncing = MutableStateFlow(false)
    val isSyncing: StateFlow<Boolean> = _isSyncing.asStateFlow()

    suspend fun runInitialSyncIfNeeded() {
        if (prefs.getBoolean(KEY_INITIAL_SYNC_DONE, false)) return

        _isSyncing.value = true
        try {
            val remoteStores = try {
                storeRemote.getAll()
            } catch (e: Exception) {
                errorHandler.notify(
                    "InitialSync", e,
                    "Could not connect to the server. Your data will sync when back online."
                )
                return
            }

            val succeeded = when {
                remoteStores.isNotEmpty() -> runCatching { pullAll(remoteStores) }
                    .onFailure {
                        errorHandler.notify(
                            "PullSync", it,
                            "Failed to restore your data. Check your connection and try again."
                        )
                    }
                    .isSuccess

                storeDao.getAll().first().isNotEmpty() -> runCatching { pushAll() }
                    .onFailure {
                        errorHandler.notify(
                            "PushSync", it,
                            "Failed to back up your local data. Check your connection and try again."
                        )
                    }
                    .isSuccess

                else -> true
            }

            if (succeeded) prefs.edit().putBoolean(KEY_INITIAL_SYNC_DONE, true).apply()
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
        val stores = storeDao.getAll().first()
        stores.forEach { storeRemote.insert(it.toDomain().toDto()) }
        stores.forEach { store ->
            productDao.getByStore(store.id).first().forEach { productRemote.insert(it.toDomain().toDto()) }
            saleDao.getByStore(store.id).first().forEach { saleRemote.insert(it.toDomain().toDto()) }
        }
    }

    companion object {
        private const val KEY_INITIAL_SYNC_DONE = "initial_sync_done"
    }
}
