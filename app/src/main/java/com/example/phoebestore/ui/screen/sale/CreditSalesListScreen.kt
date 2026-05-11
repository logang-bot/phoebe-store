package com.example.phoebestore.ui.screen.sale

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.phoebestore.R
import com.example.phoebestore.ui.common.DateRangeFilter
import com.example.phoebestore.ui.theme.PhoebeStoreTheme

@Composable
fun CreditSalesListScreen(
    onNavigateBack: () -> Unit,
    viewModel: CreditSalesListViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    CreditSalesListScreenContent(
        uiState = uiState,
        onNavigateBack = onNavigateBack,
        onFromDateChange = viewModel::onFromDateChange,
        onToDateChange = viewModel::onToDateChange,
        onResetFilters = viewModel::resetFilters,
        onMarkAsDone = viewModel::markAsDone
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CreditSalesListScreenContent(
    uiState: CreditSalesListUiState,
    onNavigateBack: () -> Unit,
    onFromDateChange: (Long) -> Unit,
    onToDateChange: (Long) -> Unit,
    onResetFilters: () -> Unit,
    onMarkAsDone: (String) -> Unit
) {
    var pendingDoneItem by remember { mutableStateOf<CreditSaleDisplayItem?>(null) }

    pendingDoneItem?.let { item ->
        MarkAsDoneDialog(
            item = item,
            onConfirm = {
                onMarkAsDone(item.id)
                pendingDoneItem = null
            },
            onDismiss = { pendingDoneItem = null }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.credit_sales_list_title),
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
            DateRangeFilter(
                fromDate = uiState.fromDate,
                toDate = uiState.toDate,
                formattedFromDate = uiState.formattedFromDate,
                formattedToDate = uiState.formattedToDate,
                onFromDateChange = onFromDateChange,
                onToDateChange = onToDateChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))
            CreditSalesContent(
                uiState = uiState,
                onItemClick = { pendingDoneItem = it },
                onResetFilters = onResetFilters,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun CreditSalesContent(
    modifier: Modifier = Modifier,
    uiState: CreditSalesListUiState,
    onItemClick: (CreditSaleDisplayItem) -> Unit,
    onResetFilters: () -> Unit
) {
    when {
        uiState.isLoading -> Box(
            modifier = modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) { CircularProgressIndicator() }

        uiState.sales.isEmpty() -> EmptyCreditContent(
            modifier = modifier,
            onResetFilters = onResetFilters
        )

        else -> LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = modifier.padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(uiState.sales, key = { it.id }) { item ->
                CreditSaleGridItem(item = item, onClick = { onItemClick(item) })
            }
        }
    }
}

@Composable
private fun EmptyCreditContent(modifier: Modifier = Modifier, onResetFilters: () -> Unit) {
    Box(modifier = modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = stringResource(R.string.credit_sales_list_empty_title),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = stringResource(R.string.credit_sales_list_empty_subtitle),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 32.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Button(onClick = onResetFilters) {
                Text(stringResource(R.string.sales_list_menu_reset_filters))
            }
        }
    }
}

@Composable
private fun MarkAsDoneDialog(
    item: CreditSaleDisplayItem,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.credit_sale_mark_done_dialog_title)) },
        text = { Text(stringResource(R.string.credit_sale_mark_done_dialog_body, item.creditPersonName)) },
        confirmButton = {
            Button(onClick = onConfirm) { Text(stringResource(R.string.credit_sale_mark_done_confirm)) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.credit_sale_mark_done_cancel)) }
        }
    )
}

private val previewItems = listOf(
    CreditSaleDisplayItem("1", "Summer Dress", "Maria García", "Apr 03, 2026 - 10:00 AM", "59.98", 2),
    CreditSaleDisplayItem("2", "Leather Bag", "John Smith", "Apr 04, 2026 - 2:00 PM", "89.99", 1),
    CreditSaleDisplayItem("3", "Sun Hat", "Ana López", "Apr 05, 2026 - 11:30 AM", "44.97", 3)
)

@Preview(showBackground = true)
@Composable
private fun CreditSalesListLightPreview() {
    PhoebeStoreTheme {
        CreditSalesListScreenContent(
            uiState = CreditSalesListUiState(
                sales = previewItems,
                fromDate = System.currentTimeMillis(),
                toDate = System.currentTimeMillis(),
                formattedFromDate = "Jan 01, 2026",
                formattedToDate = "Apr 07, 2026",
                isLoading = false
            ),
            onNavigateBack = {},
            onFromDateChange = {},
            onToDateChange = {},
            onResetFilters = {},
            onMarkAsDone = {}
        )
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun CreditSalesListDarkPreview() {
    PhoebeStoreTheme {
        CreditSalesListScreenContent(
            uiState = CreditSalesListUiState(
                sales = previewItems,
                fromDate = System.currentTimeMillis(),
                toDate = System.currentTimeMillis(),
                formattedFromDate = "Jan 01, 2026",
                formattedToDate = "Apr 07, 2026",
                isLoading = false
            ),
            onNavigateBack = {},
            onFromDateChange = {},
            onToDateChange = {},
            onResetFilters = {},
            onMarkAsDone = {}
        )
    }
}
