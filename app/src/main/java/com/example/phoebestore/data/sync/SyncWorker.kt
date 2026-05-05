package com.example.phoebestore.data.sync

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.example.phoebestore.data.local.dao.SyncOperationDao
import com.example.phoebestore.data.local.entity.SyncOperationEntity
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.util.concurrent.TimeUnit

@HiltWorker
class SyncWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val syncOpDao: SyncOperationDao,
    private val syncerRegistry: SyncerRegistry,
    private val syncNotifier: SyncNotifier,
    private val errorHandler: RemoteErrorHandler
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        val ops = syncOpDao.getAll()
        if (ops.isEmpty()) return Result.success()

        syncNotifier.notifyStarted()

        var anyFailed = false
        for (op in ops) {
            val success = runCatching { dispatch(op) }.isSuccess
            if (success) {
                syncOpDao.deleteById(op.id)
            } else {
                anyFailed = true
                errorHandler.log("SyncWorker", RuntimeException("Failed op: ${op.entityType}#${op.entityId} (${op.operation})"))
            }
        }

        return if (anyFailed) {
            syncNotifier.notifyFailure()
            Result.retry()
        } else {
            syncNotifier.notifySuccess()
            Result.success()
        }
    }

    private suspend fun dispatch(op: SyncOperationEntity) {
        val syncer = syncerRegistry.get(op.entityType) ?: return
        when (op.operation) {
            SyncOperationEntity.OP_CREATE,
            SyncOperationEntity.OP_UPDATE -> syncer.syncWrite(op.entityId)
            SyncOperationEntity.OP_DELETE -> syncer.syncDelete(op.entityId)
        }
    }

    companion object {
        private const val TAG = "SyncWorker"

        fun schedule(workManager: WorkManager) {
            val request = OneTimeWorkRequestBuilder<SyncWorker>()
                .setConstraints(
                    Constraints.Builder()
                        .setRequiredNetworkType(NetworkType.CONNECTED)
                        .build()
                )
                .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 30, TimeUnit.SECONDS)
                .addTag(TAG)
                .build()
            workManager.enqueueUniqueWork(TAG, ExistingWorkPolicy.KEEP, request)
        }
    }
}
