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
    isCustomSelected: Boolean,
    onProductSelected: (Product?) -> Unit,
    onCustomSelected: () -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    val displayText = when {
        isCustomSelected -> stringResource(R.string.record_sale_custom_product)
        selectedProduct != null -> selectedProduct.name
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
            modifier = Modifier.fillMaxWidth().menuAnchor(MenuAnchorType.PrimaryNotEditable)
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
            onProductSelected = {},
            onCustomSelected = {}
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
            onProductSelected = {},
            onCustomSelected = {}
        )
    }
}
