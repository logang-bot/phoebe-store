package com.example.phoebestore.data.sync

import android.util.Log
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RemoteErrorHandler @Inject constructor() {

    private val _errors = MutableSharedFlow<String>(extraBufferCapacity = 8)
    val errors: SharedFlow<String> = _errors.asSharedFlow()

    fun log(operation: String, error: Throwable) {
        Log.e(TAG, "[$operation] ${error.message}", error)
    }

    fun notify(operation: String, error: Throwable, userMessage: String) {
        Log.e(TAG, "[$operation] ${error.message}", error)
        _errors.tryEmit(userMessage)
    }

    companion object {
        private const val TAG = "RemoteSync"
    }
}
