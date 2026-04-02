package com.example.phoebestore.ui.screen.product

import android.content.res.Configuration
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.phoebestore.R
import com.example.phoebestore.domain.model.Product
import com.example.phoebestore.ui.theme.PhoebeStoreTheme

@Composable
fun UpdateStockDialog(
    product: Product,
    stockInput: String,
    isSaving: Boolean,
    onStockInputChange: (String) -> Unit,
    onIncrement: () -> Unit,
    onDecrement: () -> Unit,
    onSave: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = { if (!isSaving) onDismiss() },
        title = {
            Text(
                text = product.name,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = stringResource(R.string.update_stock_dialog_current_stock, product.stock),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                OutlinedTextField(
                    value = stockInput,
                    onValueChange = onStockInputChange,
                    label = { Text(stringResource(R.string.update_stock_dialog_new_stock_label)) },
                    enabled = !isSaving,
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
                val stock = stockInput.toIntOrNull() ?: 0
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilledTonalIconButton(
                        onClick = onDecrement,
                        enabled = !isSaving && stock > 0,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(painterResource(R.drawable.ic_remove), contentDescription = null)
                    }
                    FilledTonalIconButton(
                        onClick = onIncrement,
                        enabled = !isSaving,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(painterResource(R.drawable.ic_add), contentDescription = null)
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onSave,
                enabled = !isSaving && stockInput.isNotEmpty()
            ) {
                AnimatedContent(
                    targetState = isSaving,
                    transitionSpec = {
                        (fadeIn() + scaleIn(initialScale = 0.8f)) togetherWith
                                (fadeOut() + scaleOut(targetScale = 0.8f))
                    },
                    label = "SaveButtonContent"
                ) { loading ->
                    if (loading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp,
                            color = LocalContentColor.current
                        )
                    } else {
                        Text(stringResource(R.string.update_stock_dialog_save))
                    }
                }
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                enabled = !isSaving
            ) {
                Text(stringResource(R.string.update_stock_dialog_cancel))
            }
        }
    )
}

private val previewProduct = Product(id = 1L, storeId = 1L, name = "Summer Dress", price = 29.99, stock = 12)

@Preview(showBackground = true)
@Composable
private fun UpdateStockDialogLightPreview() {
    PhoebeStoreTheme {
        UpdateStockDialog(
            product = previewProduct,
            stockInput = "12",
            isSaving = false,
            onStockInputChange = {},
            onIncrement = {},
            onDecrement = {},
            onSave = {},
            onDismiss = {}
        )
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun UpdateStockDialogDarkPreview() {
    PhoebeStoreTheme {
        UpdateStockDialog(
            product = previewProduct,
            stockInput = "12",
            isSaving = false,
            onStockInputChange = {},
            onIncrement = {},
            onDecrement = {},
            onSave = {},
            onDismiss = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun UpdateStockDialogSavingPreview() {
    PhoebeStoreTheme {
        UpdateStockDialog(
            product = previewProduct,
            stockInput = "15",
            isSaving = true,
            onStockInputChange = {},
            onIncrement = {},
            onDecrement = {},
            onSave = {},
            onDismiss = {}
        )
    }
}
