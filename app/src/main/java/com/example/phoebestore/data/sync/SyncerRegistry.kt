package com.example.phoebestore.data.sync

import com.example.phoebestore.data.local.entity.SyncOperationEntity
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SyncerRegistry @Inject constructor(
    private val storeSyncer: StoreSyncer,
    private val productSyncer: ProductSyncer,
    private val saleSyncer: SaleSyncer
) {
    fun get(entityType: String): EntitySyncer? = when (entityType) {
        SyncOperationEntity.TYPE_STORE   -> storeSyncer
        SyncOperationEntity.TYPE_PRODUCT -> productSyncer
        SyncOperationEntity.TYPE_SALE    -> saleSyncer
        else                             -> null
    }
}
