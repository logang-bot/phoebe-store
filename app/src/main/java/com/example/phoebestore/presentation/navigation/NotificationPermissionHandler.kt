package com.example.phoebestore.presentation.navigation

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.phoebestore.ui.common.openAppSettings
import com.example.phoebestore.ui.common.NotificationPermissionDialog

@Composable
fun NotificationPermissionHandler(
    viewModel: NotificationPermissionViewModel = hiltViewModel()
) {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return

    val context = LocalContext.current
    val activity = context as Activity

    val triggerRequest by viewModel.triggerRequest.collectAsState()
    val showSettingsDialog by viewModel.showSettingsDialog.collectAsState()

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        val canShowRationale = activity.shouldShowRequestPermissionRationale(
            Manifest.permission.POST_NOTIFICATIONS
        )
        viewModel.onPermissionResult(isGranted, canShowRationale)
    }

    LaunchedEffect(Unit) {
        val isGranted = ContextCompat.checkSelfPermission(
            context, Manifest.permission.POST_NOTIFICATIONS
        ) == PackageManager.PERMISSION_GRANTED
        val canShowRationale = activity.shouldShowRequestPermissionRationale(
            Manifest.permission.POST_NOTIFICATIONS
        )
        viewModel.onStart(isGranted, canShowRationale)
    }

    LaunchedEffect(triggerRequest) {
        if (triggerRequest) permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
    }

    if (showSettingsDialog) {
        NotificationPermissionDialog(
            onSkip = viewModel::onSkip,
            onGoToSettings = {
                viewModel.onSettingsDismissed()
                activity.openAppSettings()
            }
        )
    }
}
