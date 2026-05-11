package com.example.phoebestore.ui.screen.product

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
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
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.phoebestore.R
import com.example.phoebestore.domain.model.Product
import com.example.phoebestore.ui.theme.PhoebeStoreTheme

@Composable
fun ProductListScreen(
    storeId: String,
    onNavigateBack: () -> Unit,
    onNavigateToCreateProduct: (storeId: String) -> Unit,
    onNavigateToEditProduct: (storeId: String, productId: String) -> Unit,
    viewModel: ProductListViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    uiState.stockDialogProduct?.let { product ->
        UpdateStockDialog(
            product = product,
            stockInput = uiState.stockDialogInput,
            isSaving = uiState.isSavingStock,
            onStockInputChange = viewModel::onStockInputChange,
            onIncrement = viewModel::onStockIncrement,
            onDecrement = viewModel::onStockDecrement,
            onSave = viewModel::onSaveStock,
            onDismiss = viewModel::onDismissStockDialog
        )
    }

    ProductListScreenContent(
        products = uiState.products,
        onNavigateBack = onNavigateBack,
        onNavigateToCreateProduct = { onNavigateToCreateProduct(storeId) },
        onNavigateToEditProduct = { productId -> onNavigateToEditProduct(storeId, productId) },
        onUpdateStockClick = viewModel::onUpdateStockClick
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ProductListScreenContent(
    products: List<Product>,
    onNavigateBack: () -> Unit,
    onNavigateToCreateProduct: () -> Unit,
    onNavigateToEditProduct: (productId: String) -> Unit,
    onUpdateStockClick: (Product) -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.product_list_title),
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
        },
        containerColor = MaterialTheme.colorScheme.surfaceContainer
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (products.isEmpty()) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = stringResource(R.string.product_list_empty_title),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = stringResource(R.string.product_list_empty_subtitle),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 32.dp)
                        )
                    }
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(products, key = { it.id }) { product ->
                        ProductCard(
                            product = product,
                            onClick = { onNavigateToEditProduct(product.id) },
                            onUpdateStockClick = { onUpdateStockClick(product) },
                            modifier = Modifier.animateItem()
                        )
                    }
                }
            }

            Button(
                onClick = onNavigateToCreateProduct,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null,
                    modifier = Modifier.size(ButtonDefaults.IconSize)
                )
                Spacer(modifier = Modifier.size(ButtonDefaults.IconSpacing))
                Text(stringResource(R.string.product_list_add_button))
            }
        }
    }
}

private val previewProducts = listOf(
    Product(id = "1", storeId = "1", name = "Summer Dress", price = 29.99, stock = 12),
    Product(id = "2", storeId = "2", name = "Leather Bag", price = 89.99, stock = 3),
    Product(id = "3", storeId = "3", name = "Sun Hat", price = 14.99, stock = 25)
)

@Preview(showBackground = true)
@Composable
private fun ProductListScreenLightPreview() {
    PhoebeStoreTheme {
        ProductListScreenContent(
            products = previewProducts,
            onNavigateBack = {},
            onNavigateToCreateProduct = {},
            onNavigateToEditProduct = {},
            onUpdateStockClick = {}
        )
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun ProductListScreenDarkPreview() {
    PhoebeStoreTheme {
        ProductListScreenContent(
            products = previewProducts,
            onNavigateBack = {},
            onNavigateToCreateProduct = {},
            onNavigateToEditProduct = {},
            onUpdateStockClick = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ProductListScreenEmptyLightPreview() {
    PhoebeStoreTheme {
        ProductListScreenContent(
            products = emptyList(),
            onNavigateToCreateProduct = {},
            onNavigateToEditProduct = {},
            onUpdateStockClick = {},
            onNavigateBack = {}
        )
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun ProductListScreenEmptyDarkPreview() {
    PhoebeStoreTheme {
        ProductListScreenContent(
            products = emptyList(),
            onNavigateToCreateProduct = {},
            onNavigateToEditProduct = {},
            onUpdateStockClick = {},
            onNavigateBack = {}
        )
    }
}
