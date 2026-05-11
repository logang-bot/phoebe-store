package com.example.phoebestore.ui.screen.sale

import android.content.res.Configuration
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.phoebestore.R
import com.example.phoebestore.domain.model.Currency
import com.example.phoebestore.domain.model.Product
import com.example.phoebestore.ui.common.LoadingButton
import com.example.phoebestore.ui.theme.PhoebeStoreTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecordSaleScreen(
    storeId: String,
    onSaleRecorded: () -> Unit,
    onNavigateBack: () -> Unit,
    viewModel: RecordSaleViewModel = hiltViewModel()
) {
    val formState by viewModel.formState.collectAsStateWithLifecycle()

    formState.saleResult?.let { result ->
        SaleResultDialog(
            result = result,
            onDismiss = {
                viewModel.clearSaleResult()
                if (result is SaleResult.Success) onSaleRecorded()
            }
        )
    }

    if (formState.showConfirmDialog) {
        SaleConfirmDialog(
            currencyName = formState.currency.name,
            productName = formState.productName,
            quantity = formState.quantity,
            formattedUnitPrice = formState.formattedUnitPrice,
            formattedUnitCost = formState.formattedUnitCost,
            formattedTotalAmount = formState.formattedTotalAmount,
            formattedSoldAt = formState.formattedSoldAt,
            notes = formState.notes,
            isOnCredit = formState.isOnCredit,
            creditPersonName = formState.creditPersonName,
            onConfirm = viewModel::confirmSave,
            onDismiss = viewModel::onDismissConfirmDialog
        )
    }

    RecordSaleScreenContent(
        formState = formState,
        onNavigateBack = onNavigateBack,
        onProductSelected = viewModel::onProductSelected,
        onCustomProductSelected = viewModel::onCustomProductSelected,
        onSearchSelected = viewModel::onSearchSelected,
        onSearchQueryChange = viewModel::onSearchQueryChange,
        onSearchConfirmed = viewModel::onSearchConfirmed,
        onProductNameChange = viewModel::onProductNameChange,
        onQuantityChange = viewModel::onQuantityChange,
        onQuantityIncrement = viewModel::onQuantityIncrement,
        onQuantityDecrement = viewModel::onQuantityDecrement,
        onUnitPriceChange = viewModel::onUnitPriceChange,
        onUnitCostChange = viewModel::onUnitCostChange,
        onUnitPriceFocusLost = viewModel::onUnitPriceFocusLost,
        onUnitCostFocusLost = viewModel::onUnitCostFocusLost,
        onSoldAtChange = viewModel::onSoldAtChange,
        onNotesChange = viewModel::onNotesChange,
        onOnCreditChange = viewModel::onOnCreditChange,
        onCreditPersonNameChange = viewModel::onCreditPersonNameChange,
        onSave = viewModel::onSaveClicked
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RecordSaleScreenContent(
    formState: RecordSaleFormState,
    onNavigateBack: () -> Unit,
    onProductSelected: (Product?) -> Unit,
    onCustomProductSelected: () -> Unit,
    onSearchSelected: () -> Unit,
    onSearchQueryChange: (String) -> Unit,
    onSearchConfirmed: () -> Unit,
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
    onCreditPersonNameChange: (String) -> Unit,
    onSave: () -> Unit
) {
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(formState.isSearchExpanded) {
        if (formState.isSearchExpanded) focusRequester.requestFocus()
    }

    Scaffold(
        topBar = {
            AnimatedContent(
                targetState = formState.isSearchExpanded,
                transitionSpec = {
                    (slideInVertically { -it } + fadeIn()) togetherWith (slideOutVertically { -it } + fadeOut())
                },
                label = "TopBarContent"
            ) { isExpanded ->
                if (isExpanded) {
                    SearchTopBar(
                        query = formState.searchQuery,
                        onQueryChange = onSearchQueryChange,
                        onSearchConfirmed = onSearchConfirmed,
                        focusRequester = focusRequester
                    )
                } else {
                    TopAppBar(
                        title = {
                            Text(
                                text = stringResource(R.string.record_sale_title),
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.SemiBold
                            )
                        },
                        navigationIcon = {
                            IconButton(onClick = onNavigateBack) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = stringResource(R.string.navigate_back)
                                )
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainer
                        )
                    )
                }
            }
        },
        containerColor = MaterialTheme.colorScheme.surfaceContainer,
        bottomBar = {
            if (!formState.isSearchExpanded) {
                Box(modifier = Modifier.navigationBarsPadding().padding(horizontal = 16.dp, vertical = 12.dp)) {
                    LoadingButton(
                        text = stringResource(R.string.record_sale_save),
                        onClick = onSave,
                        enabled = formState.canSave,
                        isLoading = formState.isSaving,
                        modifier = Modifier.fillMaxWidth(),
                        icon = {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                modifier = Modifier.size(ButtonDefaults.IconSize)
                            )
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        AnimatedContent(
            targetState = formState.isSearchExpanded,
            transitionSpec = { fadeIn() togetherWith fadeOut() },
            label = "MainContent",
            modifier = Modifier.padding(innerPadding)
        ) { isExpanded ->
            if (isExpanded) {
                SearchResultsContent(
                    filteredProducts = formState.filteredProducts,
                    searchQuery = formState.searchQuery,
                    onProductSelected = onProductSelected
                )
            } else {
                SaleFormContent(
                    formState = formState,
                    onProductSelected = onProductSelected,
                    onCustomProductSelected = onCustomProductSelected,
                    onSearchSelected = onSearchSelected,
                    onProductNameChange = onProductNameChange,
                    onQuantityChange = onQuantityChange,
                    onQuantityIncrement = onQuantityIncrement,
                    onQuantityDecrement = onQuantityDecrement,
                    onUnitPriceChange = onUnitPriceChange,
                    onUnitCostChange = onUnitCostChange,
                    onUnitPriceFocusLost = onUnitPriceFocusLost,
                    onUnitCostFocusLost = onUnitCostFocusLost,
                    onSoldAtChange = onSoldAtChange,
                    onNotesChange = onNotesChange,
                    onOnCreditChange = onOnCreditChange,
                    onCreditPersonNameChange = onCreditPersonNameChange
                )
            }
        }
    }
}

private val previewProducts = listOf(
    Product(id = "1", storeId = "1", name = "Coffee", price = 5.00, costPrice = 2.00, stock = 10),
    Product(id = "2", storeId = "2", name = "Tea", price = 3.50, costPrice = 1.00, stock = 5),
)

@Preview(showBackground = true)
@Composable
private fun RecordSaleScreenLightPreview() {
    PhoebeStoreTheme {
        RecordSaleScreenContent(
            onNavigateBack = {},

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
            onSearchQueryChange = {},
            onSearchConfirmed = {},
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
            onCreditPersonNameChange = {},
            onSave = {}
        )
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun RecordSaleScreenDarkPreview() {
    PhoebeStoreTheme {
        RecordSaleScreenContent(
            onNavigateBack = {},

            formState = RecordSaleFormState(
                products = previewProducts,
                selectedProduct = previewProducts.first(),
                unitPrice = "5.00",
                unitCost = "2.00",
                quantity = "3",
                totalAmount = 15.00,
                currency = Currency.BOB,
                formattedTotalAmount = "15.00",
                formattedSoldAt = "Mar 28, 2026 - 3:45 PM",
                formattedUnitPrice = "5.00",
                formattedUnitCost = "2.00"
            ),
            onProductSelected = {},
            onCustomProductSelected = {},
            onSearchSelected = {},
            onSearchQueryChange = {},
            onSearchConfirmed = {},
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
            onCreditPersonNameChange = {},
            onSave = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun RecordSaleExceedsStockLightPreview() {
    PhoebeStoreTheme {
        RecordSaleScreenContent(
            onNavigateBack = {},
            formState = RecordSaleFormState(
                products = previewProducts,
                selectedProduct = previewProducts.first(),
                unitPrice = "5.00",
                unitCost = "2.00",
                quantity = "15",
                totalAmount = 75.00,
                currency = Currency.USD,
                formattedTotalAmount = "75.00",
                formattedSoldAt = "Mar 28, 2026 - 3:45 PM",
                formattedUnitPrice = "5.00",
                formattedUnitCost = "2.00",
                quantityExceedsStock = true,
                canSave = true
            ),
            onProductSelected = {},
            onCustomProductSelected = {},
            onSearchSelected = {},
            onSearchQueryChange = {},
            onSearchConfirmed = {},
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
            onCreditPersonNameChange = {},
            onSave = {}
        )
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun RecordSaleExceedsStockDarkPreview() {
    PhoebeStoreTheme {
        RecordSaleScreenContent(
            onNavigateBack = {},
            formState = RecordSaleFormState(
                products = previewProducts,
                selectedProduct = previewProducts.first(),
                unitPrice = "5.00",
                unitCost = "2.00",
                quantity = "15",
                totalAmount = 75.00,
                currency = Currency.USD,
                formattedTotalAmount = "75.00",
                formattedSoldAt = "Mar 28, 2026 - 3:45 PM",
                formattedUnitPrice = "5.00",
                formattedUnitCost = "2.00",
                quantityExceedsStock = true,
                canSave = true
            ),
            onProductSelected = {},
            onCustomProductSelected = {},
            onSearchSelected = {},
            onSearchQueryChange = {},
            onSearchConfirmed = {},
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
            onCreditPersonNameChange = {},
            onSave = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun RecordSaleSearchExpandedPreview() {
    PhoebeStoreTheme {
        RecordSaleScreenContent(
            onNavigateBack = {},

            formState = RecordSaleFormState(
                products = previewProducts,
                isSearchSelected = true,
                isSearchExpanded = true,
                searchQuery = "Cof",
                filteredProducts = previewProducts.filter { it.name.contains("Cof", ignoreCase = true) },
                currency = Currency.USD,
                formattedSoldAt = "Mar 28, 2026 - 3:45 PM"
            ),
            onProductSelected = {},
            onCustomProductSelected = {},
            onSearchSelected = {},
            onSearchQueryChange = {},
            onSearchConfirmed = {},
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
            onCreditPersonNameChange = {},
            onSave = {}
        )
    }
}
