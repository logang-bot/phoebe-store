package com.example.phoebestore.ui.screen.sale

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Checkbox
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.phoebestore.R
import com.example.phoebestore.domain.model.Currency
import com.example.phoebestore.ui.common.ProductDropdown
import com.example.phoebestore.domain.model.Product
import com.example.phoebestore.ui.theme.PhoebeStoreTheme

@Composable
internal fun SaleFormContent(
    formState: RecordSaleFormState,
    onProductSelected: (Product?) -> Unit,
    onCustomProductSelected: () -> Unit,
    onSearchSelected: () -> Unit,
    onProductNameChange: (String) -> Unit,
    onQuantityChange: (String) -> Unit,
    onQuantityIncrement: () -> Unit,
    onQuantityDecrement: () -> Unit,
    onUnitPriceChange: (String) -> Unit,
    onUnitCostChange: (String) -> Unit,
    onUnitPriceFocusLost: () -> Unit,
    onUnitCostFocusLost: () -> Unit,
    onSoldAtChange: (Long) -> Unit,
    onNotesChange: (String) -> Unit,
    onOnCreditChange: (Boolean) -> Unit,
    onCreditPersonNameChange: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 16.dp)
    ) {
        ProductDropdown(
            products = formState.products,
            selectedProduct = formState.selectedProduct,
            isCustomSelected = formState.isCustomProduct,
            isSearchSelected = formState.isSearchSelected,
            showSearchOption = formState.products.isNotEmpty(),
            onProductSelected = onProductSelected,
            onCustomSelected = onCustomProductSelected,
            onSearchSelected = onSearchSelected,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(12.dp))

        if (formState.isCustomProduct) {
            OutlinedTextField(
                value = formState.productName,
                onValueChange = onProductNameChange,
                label = { Text(stringResource(R.string.record_sale_product_name_label)) },
                isError = formState.productNameError,
                supportingText = if (formState.productNameError) {
                    { Text(stringResource(R.string.record_sale_product_required_error)) }
                } else null,
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(12.dp))
        }

        OutlinedTextField(
            value = formState.quantity,
            onValueChange = onQuantityChange,
            label = { Text(stringResource(R.string.record_sale_quantity_label)) },
            isError = formState.quantityError || formState.quantityExceedsStock,
            supportingText = when {
                formState.quantityError -> { { Text(stringResource(R.string.record_sale_quantity_error)) } }
                formState.quantityExceedsStock -> { { Text(stringResource(R.string.record_sale_quantity_exceeds_stock)) } }
                else -> null
            },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )

        val qty = formState.quantity.toIntOrNull() ?: 0
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 5.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilledTonalIconButton(
                onClick = onQuantityDecrement,
                enabled = qty > 1,
                modifier = Modifier.weight(1f)
            ) {
                Icon(painterResource(R.drawable.ic_remove), contentDescription = null)
            }
            FilledTonalIconButton(
                onClick = onQuantityIncrement,
                enabled = formState.selectedProduct == null || qty < formState.selectedProduct.stock,
                modifier = Modifier.weight(1f)
            ) {
                Icon(painterResource(R.drawable.ic_add), contentDescription = null)
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        SalePriceRow(
            currencyName = formState.currency.name,
            unitPrice = formState.unitPrice,
            unitCost = formState.unitCost,
            unitPriceError = formState.unitPriceError,
            onUnitPriceChange = onUnitPriceChange,
            onUnitCostChange = onUnitCostChange,
            onUnitPriceFocusLost = onUnitPriceFocusLost,
            onUnitCostFocusLost = onUnitCostFocusLost,
            modifier = Modifier.fillMaxWidth()
        )

        SaleTotalSection(
            formattedTotalAmount = formState.formattedTotalAmount,
            currencyName = formState.currency.name
        )

        SaleModificationInfo(
            visible = formState.showModificationInfo,
            isPriceModified = formState.isPriceModified,
            isCostModified = formState.isCostModified,
            profitOutcome = formState.profitOutcome,
            currencyName = formState.currency.name,
            formattedUnitPrice = formState.formattedUnitPrice,
            formattedUnitCost = formState.formattedUnitCost,
            formattedProfitDelta = formState.formattedProfitDelta,
            formattedAbsCurrentProfit = formState.formattedAbsCurrentProfit
        )

        Spacer(modifier = Modifier.height(12.dp))

        DateField(
            epochMillis = formState.soldAt,
            formattedDate = formState.formattedSoldAt,
            onDateSelected = onSoldAtChange,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = formState.notes,
            onValueChange = onNotesChange,
            label = { Text(stringResource(R.string.record_sale_notes_label)) },
            minLines = 3,
            maxLines = 5,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        OnCreditSection(
            isOnCredit = formState.isOnCredit,
            creditPersonName = formState.creditPersonName,
            creditPersonNameError = formState.creditPersonNameError,
            onOnCreditChange = onOnCreditChange,
            onCreditPersonNameChange = onCreditPersonNameChange
        )
    }
}

@Composable
private fun OnCreditSection(
    isOnCredit: Boolean,
    creditPersonName: String,
    creditPersonNameError: Boolean,
    onOnCreditChange: (Boolean) -> Unit,
    onCreditPersonNameChange: (String) -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        Checkbox(checked = isOnCredit, onCheckedChange = onOnCreditChange)
        Text(
            text = stringResource(R.string.record_sale_on_credit_label),
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.weight(1f)
        )
    }
    if (isOnCredit) {
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = creditPersonName,
            onValueChange = onCreditPersonNameChange,
            label = { Text(stringResource(R.string.record_sale_credit_person_label)) },
            isError = creditPersonNameError,
            supportingText = if (creditPersonNameError) {
                { Text(stringResource(R.string.record_sale_credit_person_required_error)) }
            } else null,
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

private val previewProducts = listOf(
    Product(id = 1L, storeId = 1L, name = "Coffee", price = 5.00, costPrice = 2.00, stock = 10),
    Product(id = 2L, storeId = 1L, name = "Tea", price = 3.50, costPrice = 1.00, stock = 5),
)

@Preview(showBackground = true)
@Composable
private fun SaleFormContentPreview() {
    PhoebeStoreTheme {
        SaleFormContent(
            formState = RecordSaleFormState(
                products = previewProducts,
                selectedProduct = previewProducts.first(),
                unitPrice = "5.00",
                unitCost = "2.00",
                quantity = "3",
                totalAmount = 15.00,
                currency = Currency.USD,
                formattedTotalAmount = "15.00",
                formattedSoldAt = "Mar 28, 2026 - 3:45 PM",
                formattedUnitPrice = "5.00",
                formattedUnitCost = "2.00"
            ),
            onProductSelected = {},
            onCustomProductSelected = {},
            onSearchSelected = {},
            onProductNameChange = {},
            onQuantityChange = {},
            onQuantityIncrement = {},
            onQuantityDecrement = {},
            onUnitPriceChange = {},
            onUnitCostChange = {},
            onUnitPriceFocusLost = {},
            onUnitCostFocusLost = {},
            onSoldAtChange = {},
            onNotesChange = {},
            onOnCreditChange = {},
            onCreditPersonNameChange = {}
        )
    }
}
