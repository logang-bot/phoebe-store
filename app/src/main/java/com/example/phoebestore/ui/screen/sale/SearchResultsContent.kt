package com.example.phoebestore.ui.screen.sale

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.phoebestore.R
import com.example.phoebestore.domain.model.Product
import com.example.phoebestore.ui.theme.PhoebeStoreTheme

@Composable
internal fun SearchResultsContent(
    filteredProducts: List<Product>,
    searchQuery: String,
    onProductSelected: (Product?) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        if (filteredProducts.isEmpty()) {
            Text(
                text = if (searchQuery.isBlank()) ""
                else stringResource(R.string.record_sale_search_no_results),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.fillMaxWidth()
            )
        } else {
            filteredProducts.forEach { product ->
                TextButton(
                    onClick = { onProductSelected(product) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = product.name,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "%.2f".format(product.price),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

private val previewProducts = listOf(
    Product(id = 1L, storeId = 1L, name = "Coffee", price = 5.00, costPrice = 2.00, stock = 10),
    Product(id = 2L, storeId = 1L, name = "Cappuccino", price = 6.50, costPrice = 2.50, stock = 8),
)

@Preview(showBackground = true)
@Composable
private fun SearchResultsContentPreview() {
    PhoebeStoreTheme {
        SearchResultsContent(
            filteredProducts = previewProducts,
            searchQuery = "Cof",
            onProductSelected = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun SearchResultsEmptyPreview() {
    PhoebeStoreTheme {
        SearchResultsContent(
            filteredProducts = emptyList(),
            searchQuery = "xyz",
            onProductSelected = {}
        )
    }
}
