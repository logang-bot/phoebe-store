package com.example.phoebestore.ui.screen.sale

import android.content.res.Configuration
import java.util.Calendar
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.phoebestore.R
import com.example.phoebestore.domain.model.Product
import com.example.phoebestore.ui.common.DateRangeFilter
import com.example.phoebestore.ui.common.ProductDropdown
import com.example.phoebestore.ui.common.ThemedCard
import com.example.phoebestore.ui.theme.PhoebeStoreTheme

@Composable
fun SalesListScreen(
    storeId: Long,
    onNavigateBack: () -> Unit,
    onNavigateToSaleDetail: (saleId: Long) -> Unit,
    onNavigateToReport: (fromDate: Long, toDate: Long, productId: Long?) -> Unit,
    viewModel: SalesListViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    SalesListScreenContent(
        sales = uiState.sales,
        products = uiState.products,
        selectedProduct = uiState.selectedProduct,
        fromDate = uiState.fromDate,
        toDate = uiState.toDate,
        formattedFromDate = uiState.formattedFromDate,
        formattedToDate = uiState.formattedToDate,
        isLoading = uiState.isLoading,
        hasMore = uiState.hasMore,
        onNavigateBack = onNavigateBack,
        onProductSelected = viewModel::onProductSelected,
        onFromDateChange = viewModel::onFromDateChange,
        onToDateChange = viewModel::onToDateChange,
        onLoadMore = viewModel::loadMore,
        onResetFilters = viewModel::resetFilters,
        onNavigateToSaleDetail = onNavigateToSaleDetail,
        onNavigateToReport = onNavigateToReport
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SalesListScreenContent(
    sales: List<SaleDisplayItem>,
    products: List<Product>,
    selectedProduct: Product?,
    fromDate: Long,
    toDate: Long,
    formattedFromDate: String,
    formattedToDate: String,
    isLoading: Boolean,
    hasMore: Boolean,
    onNavigateBack: () -> Unit,
    onProductSelected: (Product?) -> Unit,
    onFromDateChange: (Long) -> Unit,
    onToDateChange: (Long) -> Unit,
    onLoadMore: () -> Unit,
    onResetFilters: () -> Unit,
    onNavigateToSaleDetail: (saleId: Long) -> Unit,
    onNavigateToReport: (fromDate: Long, toDate: Long, productId: Long?) -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }
    val todayStart = remember {
        Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
    }
    val todayEnd = remember {
        Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
            set(Calendar.MILLISECOND, 999)
        }.timeInMillis
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.sales_list_title),
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
                actions = {
                    IconButton(onClick = { showMenu = true }) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = stringResource(R.string.more_options)
                        )
                    }
                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.sales_list_menu_reset_filters)) },
                            onClick = {
                                showMenu = false
                                onResetFilters()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.sales_list_menu_today_report)) },
                            onClick = {
                                showMenu = false
                                onNavigateToReport(todayStart, todayEnd, null)
                            }
                        )
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.sales_list_menu_filtered_report)) },
                            onClick = {
                                showMenu = false
                                onNavigateToReport(fromDate, toDate, selectedProduct?.id)
                            }
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

            if (products.isNotEmpty()) {
                ProductDropdown(
                    products = products,
                    selectedProduct = selectedProduct,
                    onProductSelected = onProductSelected,
                    allProductsLabel = stringResource(R.string.sales_list_filter_all_products),
                    showCustomOption = false,
                    showSearchOption = false,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                )
                Spacer(modifier = Modifier.height(12.dp))
            }

            DateRangeFilter(
                fromDate = fromDate,
                toDate = toDate,
                formattedFromDate = formattedFromDate,
                formattedToDate = formattedToDate,
                onFromDateChange = onFromDateChange,
                onToDateChange = onToDateChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            when {
                isLoading -> {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }

                sales.isEmpty() -> {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = stringResource(R.string.sales_list_empty_title),
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = stringResource(R.string.sales_list_empty_subtitle),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(horizontal = 32.dp)
                            )
                        }
                    }
                }

                else -> {
                    LazyColumn(
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(sales, key = { it.id }) { item ->
                            SaleListItem(
                                item = item,
                                onClick = { onNavigateToSaleDetail(item.id) },
                                modifier = Modifier.animateItem()
                            )
                        }
                        item(key = "pagination_footer") {
                            if (hasMore) {
                                Button(
                                    onClick = onLoadMore,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 8.dp)
                                ) {
                                    Text(stringResource(R.string.sales_list_load_more))
                                }
                            } else {
                                Text(
                                    text = stringResource(R.string.sales_list_end_of_entries),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 16.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}


@Composable
private fun SaleListItem(
    item: SaleDisplayItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    ThemedCard(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = item.productName,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    if (item.isOnCredit) {
                        CreditBadge()
                    }
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = item.formattedDate,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = item.formattedTotal,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = item.formattedQuantity,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun CreditBadge() {
    Text(
        text = stringResource(R.string.sale_on_credit_badge),
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onSecondaryContainer,
        modifier = Modifier
            .background(
                color = MaterialTheme.colorScheme.secondaryContainer,
                shape = RoundedCornerShape(4.dp)
            )
            .padding(horizontal = 4.dp, vertical = 1.dp)
    )
}

private val previewSales = listOf(
    SaleDisplayItem(id = 1L, productName = "Summer Dress", formattedDate = "Apr 03, 2026 - 10:00 AM", formattedTotal = "59.98", formattedQuantity = "×2", isOnCredit = true),
    SaleDisplayItem(id = 2L, productName = "Leather Bag", formattedDate = "Apr 03, 2026 - 11:30 AM", formattedTotal = "89.99", formattedQuantity = "×1"),
    SaleDisplayItem(id = 3L, productName = "Sun Hat", formattedDate = "Apr 03, 2026 - 2:15 PM", formattedTotal = "44.97", formattedQuantity = "×3")
)

private val previewProducts = listOf(
    Product(id = 1L, storeId = 1L, name = "Summer Dress", price = 29.99),
    Product(id = 2L, storeId = 1L, name = "Leather Bag", price = 89.99),
    Product(id = 3L, storeId = 1L, name = "Sun Hat", price = 14.99)
)

@Preview(showBackground = true)
@Composable
private fun SalesListScreenLightPreview() {
    PhoebeStoreTheme {
        SalesListScreenContent(
            sales = previewSales,
            products = previewProducts,
            selectedProduct = null,
            fromDate = System.currentTimeMillis(),
            toDate = System.currentTimeMillis(),
            formattedFromDate = "Apr 03, 2026",
            formattedToDate = "Apr 03, 2026",
            isLoading = false,
            hasMore = true,
            onNavigateBack = {},
            onProductSelected = {},
            onFromDateChange = {},
            onToDateChange = {},
            onLoadMore = {},
            onResetFilters = {},
            onNavigateToSaleDetail = {},
            onNavigateToReport = { _, _, _ -> }
        )
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun SalesListScreenDarkPreview() {
    PhoebeStoreTheme {
        SalesListScreenContent(
            sales = previewSales,
            products = previewProducts,
            selectedProduct = null,
            fromDate = System.currentTimeMillis(),
            toDate = System.currentTimeMillis(),
            formattedFromDate = "Apr 03, 2026",
            formattedToDate = "Apr 03, 2026",
            isLoading = false,
            hasMore = false,
            onNavigateBack = {},
            onProductSelected = {},
            onFromDateChange = {},
            onToDateChange = {},
            onLoadMore = {},
            onResetFilters = {},
            onNavigateToSaleDetail = {},
            onNavigateToReport = { _, _, _ -> }
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun SalesListScreenLoadingPreview() {
    PhoebeStoreTheme {
        SalesListScreenContent(
            sales = emptyList(),
            products = emptyList(),
            selectedProduct = null,
            fromDate = System.currentTimeMillis(),
            toDate = System.currentTimeMillis(),
            formattedFromDate = "Apr 03, 2026",
            formattedToDate = "Apr 03, 2026",
            isLoading = true,
            hasMore = false,
            onNavigateBack = {},
            onProductSelected = {},
            onFromDateChange = {},
            onToDateChange = {},
            onLoadMore = {},
            onResetFilters = {},
            onNavigateToSaleDetail = {},
            onNavigateToReport = { _, _, _ -> }
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun SalesListScreenEmptyLightPreview() {
    PhoebeStoreTheme {
        SalesListScreenContent(
            sales = emptyList(),
            products = previewProducts,
            selectedProduct = null,
            fromDate = System.currentTimeMillis(),
            toDate = System.currentTimeMillis(),
            formattedFromDate = "Apr 03, 2026",
            formattedToDate = "Apr 03, 2026",
            isLoading = false,
            hasMore = false,
            onNavigateBack = {},
            onProductSelected = {},
            onFromDateChange = {},
            onToDateChange = {},
            onLoadMore = {},
            onResetFilters = {},
            onNavigateToSaleDetail = {},
            onNavigateToReport = { _, _, _ -> }
        )
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun SalesListScreenEmptyDarkPreview() {
    PhoebeStoreTheme {
        SalesListScreenContent(
            sales = emptyList(),
            products = previewProducts,
            selectedProduct = null,
            fromDate = System.currentTimeMillis(),
            toDate = System.currentTimeMillis(),
            formattedFromDate = "Apr 03, 2026",
            formattedToDate = "Apr 03, 2026",
            isLoading = false,
            hasMore = false,
            onNavigateBack = {},
            onProductSelected = {},
            onFromDateChange = {},
            onToDateChange = {},
            onLoadMore = {},
            onResetFilters = {},
            onNavigateToSaleDetail = {},
            onNavigateToReport = { _, _, _ -> }
        )
    }
}
