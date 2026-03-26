package com.example.phoebestore.ui.screen.sale

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
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.phoebestore.R

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
                    onClick = viewModel::save,
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
                    onProductSelected = viewModel::onProductSelected,
                    onCustomSelected = viewModel::onCustomProductSelected,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(12.dp))
            }

            if (formState.products.isEmpty() || formState.isCustomProduct) {
                OutlinedTextField(
                    value = formState.productName,
                    onValueChange = viewModel::onProductNameChange,
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
                onValueChange = viewModel::onQuantityChange,
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
                onUnitPriceChange = viewModel::onUnitPriceChange,
                onUnitCostChange = viewModel::onUnitCostChange,
                modifier = Modifier.fillMaxWidth()
            )

            SaleTotalSection(
                totalAmount = formState.totalAmount,
                currencyName = formState.currency.name
            )

            SaleModificationInfo(
                visible = formState.selectedProduct != null &&
                        (formState.isPriceModified || formState.isCostModified),
                isPriceModified = formState.isPriceModified,
                isCostModified = formState.isCostModified,
                profitOutcome = formState.profitOutcome,
                currencyName = formState.currency.name,
                unitPrice = formState.unitPrice,
                unitCost = formState.unitCost,
                profitDelta = formState.profitDelta,
                currentProfit = formState.currentProfit
            )

            Spacer(modifier = Modifier.height(12.dp))

            DateField(
                epochMillis = formState.soldAt,
                onDateSelected = viewModel::onSoldAtChange,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = formState.notes,
                onValueChange = viewModel::onNotesChange,
                label = { Text(stringResource(R.string.record_sale_notes_label)) },
                minLines = 3,
                maxLines = 5,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
