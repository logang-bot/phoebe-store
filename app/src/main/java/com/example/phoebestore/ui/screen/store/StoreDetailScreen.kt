package com.example.phoebestore.ui.screen.store

import android.content.res.Configuration
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.phoebestore.R
import com.example.phoebestore.domain.model.Currency
import com.example.phoebestore.domain.model.Store
import com.example.phoebestore.ui.theme.PhoebeStoreTheme

@Composable
fun StoreDetailScreen(
    storeId: Long,
    onNavigateToEditStore: (storeId: Long) -> Unit,
    onNavigateToProductList: (storeId: Long) -> Unit,
    onNavigateToSalesList: (storeId: Long) -> Unit,
    onNavigateToInventoryHistory: (storeId: Long) -> Unit = {},
    onNavigateToCreditSales: (storeId: Long) -> Unit = {},
    onNavigateToCreateSale: () -> Unit = {},
    onDeleteStore: () -> Unit = {},
    viewModel: StoreDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(uiState.deleted) {
        if (uiState.deleted) onDeleteStore()
    }

    StoreDetailScreenContent(
        store = uiState.store,
        totalSales = uiState.totalSales,
        formattedRevenue = uiState.formattedRevenue,
        formattedProfit = uiState.formattedProfit,
        totalStock = uiState.totalStock,
        lowStockAlerts = uiState.lowStockAlerts,
        onNavigateToEditStore = { onNavigateToEditStore(storeId) },
        onNavigateToProductList = { onNavigateToProductList(storeId) },
        onNavigateToSalesList = { onNavigateToSalesList(storeId) },
        onNavigateToInventoryHistory = { onNavigateToInventoryHistory(storeId) },
        onNavigateToCreditSales = { onNavigateToCreditSales(storeId) },
        onCreateSale = onNavigateToCreateSale,
        onDeleteStore = viewModel::deleteStore
    )
}

@Composable
private fun StoreDetailScreenContent(
    store: Store?,
    totalSales: Int,
    formattedRevenue: String,
    formattedProfit: String,
    totalStock: Int,
    lowStockAlerts: String?,
    onNavigateToEditStore: () -> Unit,
    onNavigateToProductList: () -> Unit,
    onNavigateToSalesList: () -> Unit,
    onNavigateToInventoryHistory: () -> Unit = {},
    onNavigateToCreditSales: () -> Unit = {},
    onCreateSale: () -> Unit = {},
    onDeleteStore: () -> Unit = {}
) {
    var showDeleteDialog by rememberSaveable { mutableStateOf(false) }

    if (showDeleteDialog && store != null) {
        DeleteStoreDialog(
            storeName = store.name,
            onConfirm = {
                showDeleteDialog = false
                onDeleteStore()
            },
            onDismiss = { showDeleteDialog = false }
        )
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.surfaceContainer,
        bottomBar = {
            Box(modifier = Modifier.navigationBarsPadding().padding(horizontal = 16.dp, vertical = 12.dp)) {
                Button(
                    onClick = onCreateSale,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                        modifier = Modifier.size(ButtonDefaults.IconSize)
                    )
                    Spacer(modifier = Modifier.size(ButtonDefaults.IconSpacing))
                    Text(stringResource(R.string.store_detail_create_sale_button))
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(innerPadding)
        ) {
            StoreDetailHeader(
                store = store,
                onNavigateToEditStore = onNavigateToEditStore
            )

            Column(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp)
            ) {
                if (store != null && store.description.isNotBlank()) {
                    Text(
                        text = store.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }

                if (store != null) {
                    Text(
                        text = stringResource(R.string.home_currency_format, store.currency.name),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }

                Button(
                    onClick = onNavigateToSalesList,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.secondary,
                        contentColor = MaterialTheme.colorScheme.onSecondary
                    )
                ) {
                    Icon(
                        painterResource(R.drawable.ic_orders),
                        contentDescription = null,
                        modifier = Modifier.size(ButtonDefaults.IconSize)
                    )
                    Spacer(modifier = Modifier.size(ButtonDefaults.IconSpacing))
                    Text(stringResource(R.string.store_detail_sales_button))
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedButton(
                    onClick = onNavigateToCreditSales,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        painterResource(R.drawable.ic_unpublished),
                        contentDescription = null,
                        modifier = Modifier.size(ButtonDefaults.IconSize)
                    )
                    Spacer(modifier = Modifier.size(ButtonDefaults.IconSpacing))
                    Text(stringResource(R.string.store_detail_credit_sales_button))
                }

                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = onNavigateToProductList,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.tertiary,
                        contentColor = MaterialTheme.colorScheme.onTertiary
                    )
                ) {
                    Icon(
                        painterResource(R.drawable.ic_package_2),
                        contentDescription = null,
                        modifier = Modifier.size(ButtonDefaults.IconSize)
                    )
                    Spacer(modifier = Modifier.size(ButtonDefaults.IconSpacing))
                    Text(stringResource(R.string.store_detail_products_button))
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedButton(
                    onClick = onNavigateToInventoryHistory,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        painterResource(R.drawable.ic_inventory),
                        contentDescription = null,
                        modifier = Modifier.size(ButtonDefaults.IconSize)
                    )
                    Spacer(modifier = Modifier.size(ButtonDefaults.IconSpacing))
                    Text(stringResource(R.string.store_detail_inventory_history_button))
                }

                Spacer(modifier = Modifier.height(16.dp))

                StoreDetailOverviewCard(
                    totalSales = totalSales,
                    formattedRevenue = formattedRevenue,
                    formattedProfit = formattedProfit,
                    totalStock = totalStock,
                    lowStockAlerts = lowStockAlerts
                )

                Spacer(modifier = Modifier.height(16.dp))

                StoreDetailAnalyticsCard()

                Spacer(modifier = Modifier.height(24.dp))

                OutlinedButton(
                    onClick = { showDeleteDialog = true },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    ),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.error)
                ) {
                    Text(stringResource(R.string.store_detail_delete_button))
                }

                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

private val previewStore = Store(
    id = 1L,
    name = "Phoebe's Boutique",
    description = "Fashion & Accessories",
    currency = Currency.USD
)

@Preview(showBackground = true)
@Composable
private fun StoreDetailScreenLightPreview() {
    PhoebeStoreTheme {
        StoreDetailScreenContent(
            store = previewStore,
            totalSales = 12,
            formattedRevenue = "350.00",
            formattedProfit = "120.00",
            totalStock = 84,
            lowStockAlerts = "Lipstick, Mascara, Blush",
            onNavigateToEditStore = {},
            onNavigateToProductList = {},
            onNavigateToSalesList = {}
        )
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun StoreDetailScreenDarkPreview() {
    PhoebeStoreTheme {
        StoreDetailScreenContent(
            store = previewStore,
            totalSales = 12,
            formattedRevenue = "350.00",
            formattedProfit = "120.00",
            totalStock = 84,
            lowStockAlerts = "Lipstick, Mascara, Blush",
            onNavigateToEditStore = {},
            onNavigateToProductList = {},
            onNavigateToSalesList = {}
        )
    }
}
