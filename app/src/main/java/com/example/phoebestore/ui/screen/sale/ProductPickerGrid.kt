package com.example.phoebestore.ui.screen.sale

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.example.phoebestore.R
import com.example.phoebestore.domain.model.Product
import com.example.phoebestore.ui.common.ThemedCard
import com.example.phoebestore.ui.theme.PhoebeStoreTheme

private const val GRID_COLUMNS = 3
private val GRID_GAP = 8.dp

private sealed interface GridItem {
    data object Search : GridItem
    data object Custom : GridItem
    data class ProductItem(val product: Product) : GridItem
}

@Composable
internal fun ProductPickerGrid(
    products: List<Product>,
    selectedProduct: Product?,
    isCustomSelected: Boolean,
    isSearchSelected: Boolean,
    onProductSelected: (Product) -> Unit,
    onCustomSelected: () -> Unit,
    onSearchSelected: () -> Unit,
    modifier: Modifier = Modifier
) {
    val gridItems = remember(products) {
        buildList<GridItem> {
            add(GridItem.Search)
            add(GridItem.Custom)
            products.forEach { add(GridItem.ProductItem(it)) }
        }
    }

    // For a 3-column square-cell grid: height = width (3 cells + 2 gaps cancel out).
    // This fixes the height to exactly 3 visible rows.
    BoxWithConstraints(modifier = modifier.fillMaxWidth()) {
        LazyVerticalGrid(
            columns = GridCells.Fixed(GRID_COLUMNS),
            modifier = Modifier.fillMaxWidth().height(maxWidth),
            horizontalArrangement = Arrangement.spacedBy(GRID_GAP),
            verticalArrangement = Arrangement.spacedBy(GRID_GAP)
        ) {
            items(
                items = gridItems,
                key = { item ->
                    when (item) {
                        GridItem.Search -> "search"
                        GridItem.Custom -> "custom"
                        is GridItem.ProductItem -> item.product.id
                    }
                }
            ) { item ->
                when (item) {
                    GridItem.Search -> PickerActionCard(
                        label = stringResource(R.string.record_sale_search_product),
                        icon = Icons.Default.Search,
                        isSelected = isSearchSelected,
                        onClick = onSearchSelected,
                        modifier = Modifier.aspectRatio(1f)
                    )
                    GridItem.Custom -> PickerActionCard(
                        label = stringResource(R.string.record_sale_custom_product),
                        icon = Icons.Default.Edit,
                        isSelected = isCustomSelected,
                        onClick = onCustomSelected,
                        modifier = Modifier.aspectRatio(1f)
                    )
                    is GridItem.ProductItem -> PickerProductCard(
                        product = item.product,
                        isSelected = selectedProduct?.id == item.product.id,
                        onClick = { onProductSelected(item.product) },
                        modifier = Modifier.aspectRatio(1f)
                    )
                }
            }
        }
    }
}

@Composable
private fun PickerActionCard(
    label: String,
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer
    else MaterialTheme.colorScheme.surfaceContainerHigh
    val contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer
    else MaterialTheme.colorScheme.onSurfaceVariant

    ThemedCard(
        onClick = onClick,
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = containerColor)
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = contentColor, modifier = Modifier.size(28.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                color = contentColor,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}

@Composable
private fun PickerProductCard(
    product: Product,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer
    else MaterialTheme.colorScheme.surfaceContainerHigh

    ThemedCard(
        onClick = onClick,
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = containerColor)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            ) {
                if (product.imageUrl.isNotBlank()) {
                    AsyncImage(
                        model = product.imageUrl,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Icon(
                        painter = painterResource(R.drawable.ic_package_2),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(12.dp).fillMaxSize().align(Alignment.Center)
                    )
                }
                if (isSelected) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.4f))
                    )
                }
            }
            Box(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 6.dp, vertical = 5.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = product.name,
                    style = MaterialTheme.typography.labelMedium,
                    textAlign = TextAlign.Center,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer
                    else MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

private val previewProducts = listOf(
    Product(id = 1L, storeId = 1L, name = "Coffee", price = 5.00, costPrice = 2.00, stock = 10),
    Product(id = 2L, storeId = 1L, name = "Tea", price = 3.50, costPrice = 1.00, stock = 5),
    Product(id = 3L, storeId = 1L, name = "Cappuccino", price = 6.50, costPrice = 2.50, stock = 8),
    Product(id = 4L, storeId = 1L, name = "Lemonade", price = 4.00, costPrice = 1.50, stock = 15),
    Product(id = 5L, storeId = 1L, name = "Orange Juice", price = 3.00, costPrice = 1.00, stock = 20),
    Product(id = 6L, storeId = 1L, name = "Smoothie", price = 5.50, costPrice = 2.00, stock = 6),
    Product(id = 7L, storeId = 1L, name = "Hot Chocolate", price = 4.50, costPrice = 1.80, stock = 9),
)

@Preview(showBackground = true)
@Composable
private fun ProductPickerGridLightPreview() {
    PhoebeStoreTheme {
        ProductPickerGrid(
            products = previewProducts,
            selectedProduct = previewProducts[0],
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
private fun ProductPickerGridDarkPreview() {
    PhoebeStoreTheme {
        ProductPickerGrid(
            products = previewProducts,
            selectedProduct = null,
            isCustomSelected = true,
            isSearchSelected = false,
            onProductSelected = {},
            onCustomSelected = {},
            onSearchSelected = {}
        )
    }
}
