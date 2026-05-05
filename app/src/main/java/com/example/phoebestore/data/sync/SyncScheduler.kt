package com.example.phoebestore.data.sync

import androidx.work.WorkManager
import com.example.phoebestore.data.local.dao.SyncOperationDao
import com.example.phoebestore.data.local.entity.SyncOperationEntity
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SyncScheduler @Inject constructor(
    private val syncOpDao: SyncOperationDao,
    private val workManager: WorkManager
) {
    suspend fun enqueue(entityType: String, entityId: Long, operation: String) {
        syncOpDao.insert(SyncOperationEntity(entityType = entityType, entityId = entityId, operation = operation))
        SyncWorker.schedule(workManager)
    }
}
