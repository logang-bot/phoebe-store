package com.example.phoebestore.ui.screen.sale

import android.content.res.Configuration
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.phoebestore.R
import com.example.phoebestore.domain.model.Currency
import com.example.phoebestore.domain.model.Product
import com.example.phoebestore.ui.theme.PhoebeStoreTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecordSaleScreen(
    storeId: Long,
    onSaleRecorded: () -> Unit,
    viewModel: RecordSaleViewModel = hiltViewModel()
) {
    val formState by viewModel.formState.collectAsStateWithLifecycle()

    LaunchedEffect(formState.isSuccess) {
        if (formState.isSuccess) onSaleRecorded()
    }

    RecordSaleScreenContent(
        formState = formState,
        onProductSelected = viewModel::onProductSelected,
        onCustomProductSelected = viewModel::onCustomProductSelected,
        onProductNameChange = viewModel::onProductNameChange,
        onQuantityChange = viewModel::onQuantityChange,
        onUnitPriceChange = viewModel::onUnitPriceChange,
        onUnitCostChange = viewModel::onUnitCostChange,
        onUnitPriceFocusLost = viewModel::onUnitPriceFocusLost,
        onUnitCostFocusLost = viewModel::onUnitCostFocusLost,
        onSoldAtChange = viewModel::onSoldAtChange,
        onNotesChange = viewModel::onNotesChange,
        onSave = viewModel::save
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RecordSaleScreenContent(
    formState: RecordSaleFormState,
    onProductSelected: (Product?) -> Unit,
    onCustomProductSelected: () -> Unit,
    onProductNameChange: (String) -> Unit,
    onQuantityChange: (String) -> Unit,
    onUnitPriceChange: (String) -> Unit,
    onUnitCostChange: (String) -> Unit,
    onUnitPriceFocusLost: () -> Unit,
    onUnitCostFocusLost: () -> Unit,
    onSoldAtChange: (Long) -> Unit,
    onNotesChange: (String) -> Unit,
    onSave: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.record_sale_title),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainer
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.surfaceContainer,
        bottomBar = {
            Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
                Button(
                    onClick = onSave,
                    enabled = !formState.isSaving,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(stringResource(R.string.record_sale_save))
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(innerPadding)
                .padding(horizontal = 16.dp, vertical = 16.dp)
        ) {
            if (formState.products.isNotEmpty()) {
                ProductDropdown(
                    products = formState.products,
                    selectedProduct = formState.selectedProduct,
                    isCustomSelected = formState.isCustomProduct,
                    onProductSelected = onProductSelected,
                    onCustomSelected = onCustomProductSelected,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(12.dp))
            }

            if (formState.products.isEmpty() || formState.isCustomProduct) {
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
                isError = formState.quantityError,
                supportingText = if (formState.quantityError) {
                    { Text(stringResource(R.string.record_sale_quantity_error)) }
                } else null,
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

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
        }
    }
}

private val previewProducts = listOf(
    Product(id = 1L, storeId = 1L, name = "Coffee", price = 5.00, costPrice = 2.00),
    Product(id = 2L, storeId = 1L, name = "Tea", price = 3.50, costPrice = 1.00),
)

@Preview(showBackground = true)
@Composable
private fun RecordSaleScreenLightPreview() {
    PhoebeStoreTheme {
        RecordSaleScreenContent(
            formState = RecordSaleFormState(
                products = previewProducts,
                selectedProduct = previewProducts.first(),
                unitPrice = "5.00",
                unitCost = "2.00",
                quantity = "3",
                totalAmount = 15.00,
                currency = Currency.USD,
                formattedTotalAmount = "15.00",
                formattedSoldAt = "Mar 28, 2026",
                formattedUnitPrice = "5.00",
                formattedUnitCost = "2.00"
            ),
            onProductSelected = {},
            onCustomProductSelected = {},
            onProductNameChange = {},
            onQuantityChange = {},
            onUnitPriceChange = {},
            onUnitCostChange = {},
            onUnitPriceFocusLost = {},
            onUnitCostFocusLost = {},
            onSoldAtChange = {},
            onNotesChange = {},
            onSave = {}
        )
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun RecordSaleScreenDarkPreview() {
    PhoebeStoreTheme {
        RecordSaleScreenContent(
            formState = RecordSaleFormState(
                products = previewProducts,
                selectedProduct = previewProducts.first(),
                unitPrice = "5.00",
                unitCost = "2.00",
                quantity = "3",
                totalAmount = 15.00,
                currency = Currency.BOB,
                formattedTotalAmount = "15.00",
                formattedSoldAt = "Mar 28, 2026",
                formattedUnitPrice = "5.00",
                formattedUnitCost = "2.00"
            ),
            onProductSelected = {},
            onCustomProductSelected = {},
            onProductNameChange = {},
            onQuantityChange = {},
            onUnitPriceChange = {},
            onUnitCostChange = {},
            onUnitPriceFocusLost = {},
            onUnitCostFocusLost = {},
            onSoldAtChange = {},
            onNotesChange = {},
            onSave = {}
        )
    }
}
