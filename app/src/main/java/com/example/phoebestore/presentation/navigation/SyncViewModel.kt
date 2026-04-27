package com.example.phoebestore.presentation.navigation

import androidx.lifecycle.ViewModel
import com.example.phoebestore.data.sync.RemoteErrorHandler
import com.example.phoebestore.data.sync.SyncManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

@HiltViewModel
class SyncViewModel @Inject constructor(
    errorHandler: RemoteErrorHandler,
    syncManager: SyncManager
) : ViewModel() {
    val syncError: SharedFlow<String> = errorHandler.errors
    val isSyncing: StateFlow<Boolean> = syncManager.isSyncing
}
