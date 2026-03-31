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
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import com.example.phoebestore.domain.model.Sale
import com.example.phoebestore.ui.common.ThemedCard
import com.example.phoebestore.ui.theme.PhoebeStoreTheme
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun SalesListScreen(
    storeId: Long,
    onNavigateToSaleDetail: (saleId: Long) -> Unit,
    viewModel: SalesListViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    SalesListScreenContent(
        sales = uiState.sales,
        fromDate = uiState.fromDate,
        toDate = uiState.toDate,
        formattedFromDate = uiState.formattedFromDate,
        formattedToDate = uiState.formattedToDate,
        onFromDateChange = viewModel::onFromDateChange,
        onToDateChange = viewModel::onToDateChange,
        onNavigateToSaleDetail = onNavigateToSaleDetail
    )
}

@Composable
private fun SalesListScreenContent(
    sales: List<Sale>,
    fromDate: Long,
    toDate: Long,
    formattedFromDate: String,
    formattedToDate: String,
    onFromDateChange: (Long) -> Unit,
    onToDateChange: (Long) -> Unit,
    onNavigateToSaleDetail: (saleId: Long) -> Unit
) {
    Scaffold(
        containerColor = MaterialTheme.colorScheme.surfaceContainer
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Text(
                text = stringResource(R.string.sales_list_title),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 24.dp)
            )

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

            if (sales.isEmpty()) {
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
            } else {
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
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = fromDate)
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
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = toDate)
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

@Preview(showBackground = true)
@Composable
private fun SalesListScreenLightPreview() {
    PhoebeStoreTheme {
        SalesListScreenContent(
            sales = previewSales,
            fromDate = System.currentTimeMillis(),
            toDate = System.currentTimeMillis(),
            formattedFromDate = "Mar 31, 2026",
            formattedToDate = "Mar 31, 2026",
            onFromDateChange = {},
            onToDateChange = {},
            onNavigateToSaleDetail = {}
        )
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun SalesListScreenDarkPreview() {
    PhoebeStoreTheme {
        SalesListScreenContent(
            sales = previewSales,
            fromDate = System.currentTimeMillis(),
            toDate = System.currentTimeMillis(),
            formattedFromDate = "Mar 31, 2026",
            formattedToDate = "Mar 31, 2026",
            onFromDateChange = {},
            onToDateChange = {},
            onNavigateToSaleDetail = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun SalesListScreenEmptyLightPreview() {
    PhoebeStoreTheme {
        SalesListScreenContent(
            sales = emptyList(),
            fromDate = System.currentTimeMillis(),
            toDate = System.currentTimeMillis(),
            formattedFromDate = "Mar 31, 2026",
            formattedToDate = "Mar 31, 2026",
            onFromDateChange = {},
            onToDateChange = {},
            onNavigateToSaleDetail = {}
        )
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun SalesListScreenEmptyDarkPreview() {
    PhoebeStoreTheme {
        SalesListScreenContent(
            sales = emptyList(),
            fromDate = System.currentTimeMillis(),
            toDate = System.currentTimeMillis(),
            formattedFromDate = "Mar 31, 2026",
            formattedToDate = "Mar 31, 2026",
            onFromDateChange = {},
            onToDateChange = {},
            onNavigateToSaleDetail = {}
        )
    }
}
