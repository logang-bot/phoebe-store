package com.example.phoebestore.ui.screen.sale

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.phoebestore.R
import com.example.phoebestore.ui.theme.PhoebeStoreTheme

@Composable
fun SaleConfirmDialog(
    currencyName: String,
    productName: String,
    quantity: String,
    formattedUnitPrice: String,
    formattedUnitCost: String,
    formattedTotalAmount: String,
    formattedSoldAt: String,
    notes: String,
    isOnCredit: Boolean = false,
    creditPersonName: String = "",
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = stringResource(R.string.record_sale_confirm_title),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

                ConfirmRow(
                    label = stringResource(R.string.record_sale_product_name_label),
                    value = productName
                )
                ConfirmRow(
                    label = stringResource(R.string.record_sale_quantity_label),
                    value = quantity
                )
                ConfirmRow(
                    label = stringResource(R.string.record_sale_unit_price_label),
                    value = "$currencyName $formattedUnitPrice"
                )
                if (formattedUnitCost != "0.00") {
                    ConfirmRow(
                        label = stringResource(R.string.record_sale_unit_cost_label),
                        value = "$currencyName $formattedUnitCost"
                    )
                }
                if (formattedTotalAmount.isNotEmpty()) {
                    ConfirmRow(
                        label = stringResource(R.string.record_sale_total_label),
                        value = "$currencyName $formattedTotalAmount"
                    )
                }
                ConfirmRow(
                    label = stringResource(R.string.record_sale_date_label),
                    value = formattedSoldAt
                )
                if (notes.isNotBlank()) {
                    ConfirmRow(
                        label = stringResource(R.string.record_sale_notes_label),
                        value = notes
                    )
                }
                if (isOnCredit) {
                    ConfirmRow(
                        label = stringResource(R.string.record_sale_on_credit_confirm_label),
                        value = creditPersonName
                    )
                }
            }
        },
        confirmButton = {
            Button(onClick = onConfirm) {
                Text(stringResource(R.string.record_sale_save))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.record_sale_date_cancel))
            }
        }
    )
}

@Composable
private fun ConfirmRow(label: String, value: String) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun SaleConfirmDialogLightPreview() {
    PhoebeStoreTheme {
        SaleConfirmDialog(
            currencyName = "USD",
            productName = "Coffee",
            quantity = "3",
            formattedUnitPrice = "5.00",
            formattedUnitCost = "2.00",
            formattedTotalAmount = "15.00",
            formattedSoldAt = "Mar 28, 2026 - 3:45 PM",
            notes = "",
            onConfirm = {},
            onDismiss = {}
        )
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun SaleConfirmDialogDarkPreview() {
    PhoebeStoreTheme {
        SaleConfirmDialog(
            currencyName = "USD",
            productName = "Coffee",
            quantity = "3",
            formattedUnitPrice = "5.00",
            formattedUnitCost = "2.00",
            formattedTotalAmount = "15.00",
            formattedSoldAt = "Mar 28, 2026 - 3:45 PM",
            notes = "Morning rush order",
            onConfirm = {},
            onDismiss = {}
        )
    }
}
