package com.example.phoebestore.presentation.navigation

import android.content.Context
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class NotificationPermissionViewModel @Inject constructor(
    @ApplicationContext context: Context
) : ViewModel() {

    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val _triggerRequest = MutableStateFlow(false)
    val triggerRequest: StateFlow<Boolean> = _triggerRequest.asStateFlow()

    private val _showSettingsDialog = MutableStateFlow(false)
    val showSettingsDialog: StateFlow<Boolean> = _showSettingsDialog.asStateFlow()

    fun onStart(isGranted: Boolean, canShowRationale: Boolean) {
        if (isGranted) return
        if (prefs.getBoolean(KEY_SKIPPED, false)) return

        val hasRequested = prefs.getBoolean(KEY_REQUESTED, false)
        if (!hasRequested || canShowRationale) {
            _triggerRequest.value = true
        } else {
            _showSettingsDialog.value = true
        }
    }

    fun onPermissionResult(isGranted: Boolean, canShowRationale: Boolean) {
        prefs.edit().putBoolean(KEY_REQUESTED, true).apply()
        _triggerRequest.value = false
        if (!isGranted && !canShowRationale) {
            _showSettingsDialog.value = true
        }
    }

    fun onSkip() {
        prefs.edit().putBoolean(KEY_SKIPPED, true).apply()
        _showSettingsDialog.value = false
    }

    fun onSettingsDismissed() {
        _showSettingsDialog.value = false
    }

    companion object {
        private const val PREFS_NAME = "notification_prefs"
        private const val KEY_REQUESTED = "notification_requested"
        private const val KEY_SKIPPED = "notification_skipped"
    }
}
