package com.example.phoebestore.ui.screen.sale

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.phoebestore.R
import com.example.phoebestore.ui.theme.PhoebeStoreTheme

@Composable
fun SalePriceRow(
    currencyName: String,
    unitPrice: String,
    unitCost: String,
    unitPriceError: Boolean,
    onUnitPriceChange: (String) -> Unit,
    onUnitCostChange: (String) -> Unit,
    onUnitPriceFocusLost: () -> Unit,
    onUnitCostFocusLost: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = modifier
    ) {
        OutlinedTextField(
            value = unitPrice,
            onValueChange = onUnitPriceChange,
            label = { Text(stringResource(R.string.record_sale_unit_price_label)) },
            prefix = { Text(currencyName) },
            isError = unitPriceError,
            supportingText = if (unitPriceError) {
                { Text(stringResource(R.string.record_sale_unit_price_error)) }
            } else null,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            singleLine = true,
            modifier = Modifier
                .weight(1f)
                .onFocusChanged { if (!it.isFocused) onUnitPriceFocusLost() }
        )
        OutlinedTextField(
            value = unitCost,
            onValueChange = onUnitCostChange,
            label = { Text(stringResource(R.string.record_sale_unit_cost_label)) },
            prefix = { Text(currencyName) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            singleLine = true,
            modifier = Modifier
                .weight(1f)
                .onFocusChanged { if (!it.isFocused) onUnitCostFocusLost() }
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun SalePriceRowLightPreview() {
    PhoebeStoreTheme {
        SalePriceRow(
            currencyName = "USD",
            unitPrice = "10.00",
            unitCost = "5.00",
            unitPriceError = false,
            onUnitPriceChange = {},
            onUnitCostChange = {},
            onUnitPriceFocusLost = {},
            onUnitCostFocusLost = {}
        )
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun SalePriceRowDarkPreview() {
    PhoebeStoreTheme {
        SalePriceRow(
            currencyName = "BOB",
            unitPrice = "10.00",
            unitCost = "",
            unitPriceError = true,
            onUnitPriceChange = {},
            onUnitCostChange = {},
            onUnitPriceFocusLost = {},
            onUnitCostFocusLost = {}
        )
    }
}
