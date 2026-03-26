package com.example.phoebestore.ui.screen.sale

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.phoebestore.R
import com.example.phoebestore.domain.model.Product
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

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
            // Product selection
            if (formState.products.isNotEmpty()) {
                ProductDropdown(
                    products = formState.products,
                    selectedProduct = formState.selectedProduct,
                    isCustomSelected = formState.isCustomProduct,
                    onProductSelected = viewModel::onProductSelected,
                    onCustomSelected = viewModel::onCustomProductSelected
                )
                Spacer(modifier = Modifier.height(12.dp))
            }

            // Custom product name — shown when no products exist or "Custom" is selected
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

            // Quantity
            OutlinedTextField(
                value = formState.quantity,
                onValueChange = viewModel::onQuantityChange,
                label = { Text(stringResource(R.string.record_sale_quantity_label)) },
                isError = formState.quantityError,
                supportingText = if (formState.quantityError) {
                    { Text(stringResource(R.string.record_sale_quantity_error)) }
                } else null,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Unit price
            OutlinedTextField(
                value = formState.unitPrice,
                onValueChange = viewModel::onUnitPriceChange,
                label = { Text(stringResource(R.string.record_sale_unit_price_label)) },
                isError = formState.unitPriceError,
                supportingText = if (formState.unitPriceError) {
                    { Text(stringResource(R.string.record_sale_unit_price_error)) }
                } else null,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Unit cost (optional)
            OutlinedTextField(
                value = formState.unitCost,
                onValueChange = viewModel::onUnitCostChange,
                label = { Text(stringResource(R.string.record_sale_unit_cost_label)) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            // Total display
            val totalAmount = (formState.quantity.toIntOrNull() ?: 0) *
                    (formState.unitPrice.toDoubleOrNull() ?: 0.0)
            if (totalAmount > 0.0) {
                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "${stringResource(R.string.record_sale_total_label)}: %.2f".format(totalAmount),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(8.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Date of sale
            DateField(
                epochMillis = formState.soldAt,
                onDateSelected = viewModel::onSoldAtChange
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Notes (optional)
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ProductDropdown(
    products: List<Product>,
    selectedProduct: Product?,
    isCustomSelected: Boolean,
    onProductSelected: (Product?) -> Unit,
    onCustomSelected: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    val displayText = when {
        isCustomSelected -> stringResource(R.string.record_sale_custom_product)
        selectedProduct != null -> selectedProduct.name
        else -> ""
    }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it }
    ) {
        OutlinedTextField(
            value = displayText,
            onValueChange = {},
            readOnly = true,
            label = { Text(stringResource(R.string.record_sale_product_label)) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(MenuAnchorType.PrimaryNotEditable)
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            products.forEach { product ->
                DropdownMenuItem(
                    text = { Text(product.name) },
                    onClick = {
                        onProductSelected(product)
                        expanded = false
                    }
                )
            }
            DropdownMenuItem(
                text = { Text(stringResource(R.string.record_sale_custom_product)) },
                onClick = {
                    onCustomSelected()
                    expanded = false
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DateField(
    epochMillis: Long,
    onDateSelected: (Long) -> Unit
) {
    var showPicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState(initialSelectedDateMillis = epochMillis)
    val dateFormat = remember { SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()) }

    OutlinedTextField(
        value = dateFormat.format(Date(epochMillis)),
        onValueChange = {},
        readOnly = true,
        label = { Text(stringResource(R.string.record_sale_date_label)) },
        modifier = Modifier.fillMaxWidth(),
        // Make the entire field clickable via interaction source workaround
        trailingIcon = {
            TextButton(onClick = { showPicker = true }) {
                Text(
                    text = "Change",
                    style = MaterialTheme.typography.labelMedium
                )
            }
        }
    )

    if (showPicker) {
        DatePickerDialog(
            onDismissRequest = { showPicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { onDateSelected(it) }
                    showPicker = false
                }) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showPicker = false }) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}
