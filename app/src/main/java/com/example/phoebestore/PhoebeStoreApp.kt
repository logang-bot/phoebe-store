package com.example.phoebestore

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import androidx.work.WorkManager
import com.example.phoebestore.data.sync.SyncManager
import com.example.phoebestore.data.sync.SyncNotifier
import com.example.phoebestore.data.sync.SyncWorker
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltAndroidApp
class PhoebeStoreApp : Application(), Configuration.Provider {

    @Inject lateinit var syncManager: SyncManager
    @Inject lateinit var workerFactory: HiltWorkerFactory
    @Inject lateinit var workManager: WorkManager
    @Inject lateinit var syncNotifier: SyncNotifier

    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()

    override fun onCreate() {
        super.onCreate()
        syncNotifier.createChannel()
        SyncWorker.schedule(workManager)
        appScope.launch {
            syncManager.runSync()
            syncManager.repairLocalImageUrls()
        }
    }
}
