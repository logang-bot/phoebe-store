package com.example.phoebestore.ui.screen.sale

import android.content.res.Configuration
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
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDatePickerState
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
import com.example.phoebestore.domain.model.Sale
import com.example.phoebestore.ui.common.ThemedCard
import com.example.phoebestore.ui.theme.PhoebeStoreTheme
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

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
    sales: List<Sale>,
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
                        items(sales, key = { it.id }) { sale ->
                            SaleListItem(
                                sale = sale,
                                onClick = { onNavigateToSaleDetail(sale.id) },
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DateRangeFilter(
    fromDate: Long,
    toDate: Long,
    formattedFromDate: String,
    formattedToDate: String,
    onFromDateChange: (Long) -> Unit,
    onToDateChange: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    var showFromPicker by remember { mutableStateOf(false) }
    var showToPicker by remember { mutableStateOf(false) }

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        OutlinedTextField(
            value = formattedFromDate,
            onValueChange = {},
            readOnly = true,
            label = { Text(stringResource(R.string.sales_list_filter_from)) },
            trailingIcon = {
                TextButton(onClick = { showFromPicker = true }) {
                    Text(
                        text = stringResource(R.string.record_sale_date_change),
                        style = MaterialTheme.typography.labelMedium
                    )
                }
            },
            modifier = Modifier.weight(1f),
            singleLine = true
        )
        OutlinedTextField(
            value = formattedToDate,
            onValueChange = {},
            readOnly = true,
            label = { Text(stringResource(R.string.sales_list_filter_to)) },
            trailingIcon = {
                TextButton(onClick = { showToPicker = true }) {
                    Text(
                        text = stringResource(R.string.record_sale_date_change),
                        style = MaterialTheme.typography.labelMedium
                    )
                }
            },
            modifier = Modifier.weight(1f),
            singleLine = true
        )
    }

    if (showFromPicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = remember(fromDate) { toUtcMidnight(fromDate) }
        )
        DatePickerDialog(
            onDismissRequest = { showFromPicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { onFromDateChange(it) }
                    showFromPicker = false
                }) {
                    Text(stringResource(R.string.sales_list_date_ok))
                }
            },
            dismissButton = {
                TextButton(onClick = { showFromPicker = false }) {
                    Text(stringResource(R.string.sales_list_date_cancel))
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    if (showToPicker) {
        val fromUtcMidnight = remember(fromDate) { toUtcMidnight(fromDate) }
        val toSelectableDates = remember(fromUtcMidnight) {
            object : SelectableDates {
                override fun isSelectableDate(utcTimeMillis: Long) = utcTimeMillis >= fromUtcMidnight
            }
        }
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = remember(toDate) { toUtcMidnight(toDate) },
            selectableDates = toSelectableDates
        )
        DatePickerDialog(
            onDismissRequest = { showToPicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { onToDateChange(it) }
                    showToPicker = false
                }) {
                    Text(stringResource(R.string.sales_list_date_ok))
                }
            },
            dismissButton = {
                TextButton(onClick = { showToPicker = false }) {
                    Text(stringResource(R.string.sales_list_date_cancel))
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

private val dateTimeFormat = SimpleDateFormat("MMM dd, yyyy - h:mm a", Locale.getDefault())

private fun toUtcMidnight(localEpochMillis: Long): Long {
    val local = Calendar.getInstance().apply { timeInMillis = localEpochMillis }
    return Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
        set(local.get(Calendar.YEAR), local.get(Calendar.MONTH), local.get(Calendar.DAY_OF_MONTH), 0, 0, 0)
        set(Calendar.MILLISECOND, 0)
    }.timeInMillis
}

@Composable
private fun SaleListItem(
    sale: Sale,
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
                Text(
                    text = sale.productName,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = dateTimeFormat.format(Date(sale.soldAt)),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "%.2f".format(sale.totalAmount),
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "×${sale.quantity}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

private val previewSales = listOf(
    Sale(id = 1L, storeId = 1L, productName = "Summer Dress", quantity = 2, unitPrice = 29.99, totalAmount = 59.98),
    Sale(id = 2L, storeId = 1L, productName = "Leather Bag", quantity = 1, unitPrice = 89.99, totalAmount = 89.99),
    Sale(id = 3L, storeId = 1L, productName = "Sun Hat", quantity = 3, unitPrice = 14.99, totalAmount = 44.97)
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
