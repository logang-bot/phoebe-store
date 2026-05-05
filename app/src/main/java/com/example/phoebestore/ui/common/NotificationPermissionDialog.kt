package com.example.phoebestore.ui.common

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import com.example.phoebestore.R

@Composable
fun NotificationPermissionDialog(
    modifier: Modifier = Modifier,
    onSkip: () -> Unit,
    onGoToSettings: () -> Unit
) {
    AlertDialog(
        modifier = modifier,
        onDismissRequest = onSkip,
        title = { Text(stringResource(R.string.notification_permission_dialog_title)) },
        text = { Text(stringResource(R.string.notification_permission_dialog_body)) },
        confirmButton = {
            TextButton(onClick = onGoToSettings) {
                Text(stringResource(R.string.notification_permission_go_to_settings), fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onSkip) {
                Text(stringResource(R.string.notification_permission_skip))
            }
        }
    )
}
