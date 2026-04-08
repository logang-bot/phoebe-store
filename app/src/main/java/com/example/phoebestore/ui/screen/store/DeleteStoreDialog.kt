package com.example.phoebestore.ui.screen.store

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.phoebestore.R

@Composable
internal fun DeleteStoreDialog(
    storeName: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    var typedName by rememberSaveable { mutableStateOf("") }
    val confirmed = typedName.trim() == storeName

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.store_detail_delete_dialog_title, storeName)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = stringResource(R.string.store_detail_delete_dialog_body),
                    style = MaterialTheme.typography.bodyMedium
                )
                OutlinedTextField(
                    value = typedName,
                    onValueChange = { typedName = it },
                    label = { Text(stringResource(R.string.store_detail_delete_dialog_name_label)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = onConfirm,
                enabled = confirmed,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = MaterialTheme.colorScheme.error,
                    disabledContentColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                )
            ) {
                Text(stringResource(R.string.store_detail_delete_dialog_confirm))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.store_detail_delete_dialog_cancel))
            }
        }
    )
}
