package com.example.phoebestore.ui.screen.sale

import android.content.res.Configuration
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.example.phoebestore.R
import com.example.phoebestore.domain.model.Product
import com.example.phoebestore.ui.theme.PhoebeStoreTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductDropdown(
    products: List<Product>,
    selectedProduct: Product?,
    onProductSelected: (Product?) -> Unit,
    modifier: Modifier = Modifier,
    isCustomSelected: Boolean = false,
    isSearchSelected: Boolean = false,
    showCustomOption: Boolean = true,
    showSearchOption: Boolean = true,
    allProductsLabel: String? = null,
    onCustomSelected: () -> Unit = {},
    onSearchSelected: () -> Unit = {}
) {
    var expanded by remember { mutableStateOf(false) }

    val displayText = when {
        isSearchSelected -> stringResource(R.string.record_sale_search_product)
        isCustomSelected -> stringResource(R.string.record_sale_custom_product)
        selectedProduct != null -> selectedProduct.name
        allProductsLabel != null -> allProductsLabel
        else -> ""
    }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it },
        modifier = modifier
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
            if (showSearchOption) {
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.record_sale_search_product)) },
                    onClick = {
                        onSearchSelected()
                        expanded = false
                    }
                )
            }
            if (showCustomOption) {
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.record_sale_custom_product)) },
                    onClick = {
                        onCustomSelected()
                        expanded = false
                    }
                )
            }
            if (allProductsLabel != null) {
                DropdownMenuItem(
                    text = { Text(allProductsLabel) },
                    onClick = {
                        onProductSelected(null)
                        expanded = false
                    }
                )
            }
            products.forEach { product ->
                DropdownMenuItem(
                    text = { Text(product.name) },
                    onClick = {
                        onProductSelected(product)
                        expanded = false
                    }
                )
            }
        }
    }
}

private val previewProducts = listOf(
    Product(id = 1L, storeId = 1L, name = "Coffee", price = 5.00, costPrice = 2.00),
    Product(id = 2L, storeId = 1L, name = "Tea", price = 3.50, costPrice = 1.00),
)

@Preview(showBackground = true)
@Composable
private fun ProductDropdownLightPreview() {
    PhoebeStoreTheme {
        ProductDropdown(
            products = previewProducts,
            selectedProduct = previewProducts.first(),
            isCustomSelected = false,
            isSearchSelected = false,
            onProductSelected = {},
            onCustomSelected = {},
            onSearchSelected = {}
        )
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun ProductDropdownDarkPreview() {
    PhoebeStoreTheme {
        ProductDropdown(
            products = previewProducts,
            selectedProduct = previewProducts.first(),
            isCustomSelected = false,
            isSearchSelected = false,
            onProductSelected = {},
            onCustomSelected = {},
            onSearchSelected = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ProductDropdownFilterPreview() {
    PhoebeStoreTheme {
        ProductDropdown(
            products = previewProducts,
            selectedProduct = null,
            onProductSelected = {},
            allProductsLabel = "All products",
            showCustomOption = false,
            showSearchOption = false
        )
    }
}
