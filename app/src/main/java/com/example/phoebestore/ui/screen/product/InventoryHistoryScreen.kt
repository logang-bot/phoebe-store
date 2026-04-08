package com.example.phoebestore.ui.screen.product

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
fun InventoryHistoryScreen(
    onNavigateBack: () -> Unit,
    viewModel: InventoryHistoryViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    InventoryHistoryScreenContent(
        uiState = uiState,
        onNavigateBack = onNavigateBack,
        onProductSelected = viewModel::onProductSelected,
        onFromDateChange = viewModel::onFromDateChange,
        onToDateChange = viewModel::onToDateChange,
        onResetFilters = viewModel::resetFilters
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun InventoryHistoryScreenContent(
    uiState: InventoryHistoryUiState,
    onNavigateBack: () -> Unit,
    onProductSelected: (Product?) -> Unit,
    onFromDateChange: (Long) -> Unit,
    onToDateChange: (Long) -> Unit,
    onResetFilters: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.inventory_history_title),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, stringResource(R.string.navigate_back))
                    }
                },
                actions = {
                    IconButton(onClick = { showMenu = true }) {
                        Icon(Icons.Default.MoreVert, stringResource(R.string.more_options))
                    }
                    DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.sales_list_menu_reset_filters)) },
                            onClick = { showMenu = false; onResetFilters() }
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
            )
        },
        containerColor = MaterialTheme.colorScheme.surfaceContainer
    ) { innerPadding ->
        Column(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            if (uiState.products.isNotEmpty()) {
                ProductDropdown(
                    products = uiState.products,
                    selectedProduct = uiState.selectedProduct,
                    onProductSelected = onProductSelected,
                    allProductsLabel = stringResource(R.string.sales_list_filter_all_products),
                    showCustomOption = false,
                    showSearchOption = false,
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
                )
                Spacer(Modifier.height(12.dp))
            }
            DateRangeFilter(
                fromDate = uiState.fromDate,
                toDate = uiState.toDate,
                formattedFromDate = uiState.formattedFromDate,
                formattedToDate = uiState.formattedToDate,
                onFromDateChange = onFromDateChange,
                onToDateChange = onToDateChange,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
            )
            Spacer(Modifier.height(12.dp))
            when {
                uiState.isLoading -> Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
                uiState.logs.isEmpty() -> EmptyHistoryContent(modifier = Modifier.weight(1f).fillMaxWidth())
                else -> LazyColumn(
                    modifier = Modifier.weight(1f).padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(uiState.logs, key = { it.id }) { item ->
                        InventoryLogItem(item = item, modifier = Modifier.animateItem())
                    }
                }
            }
        }
    }
}

@Composable
private fun InventoryLogItem(item: InventoryLogDisplayItem, modifier: Modifier = Modifier) {
    val deltaColor = if (item.delta >= 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
    val deltaText = if (item.delta >= 0) "+${item.delta}" else "${item.delta}"
    ThemedCard(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(item.productName, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Spacer(Modifier.height(2.dp))
                Text(item.formattedDate, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Column(horizontalAlignment = Alignment.End) {
                Text("${item.previousStock} → ${item.newStock}", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
                Spacer(Modifier.height(2.dp))
                Text(deltaText, style = MaterialTheme.typography.bodySmall, color = deltaColor, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

@Composable
private fun EmptyHistoryContent(modifier: Modifier = Modifier) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(stringResource(R.string.inventory_history_empty_title), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(4.dp))
            Text(stringResource(R.string.inventory_history_empty_subtitle), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center, modifier = Modifier.padding(horizontal = 32.dp))
        }
    }
}

private val previewLogs = listOf(
    InventoryLogDisplayItem(1L, "Summer Dress", "Apr 07, 2026 - 10:00 AM", 12, 20, 8),
    InventoryLogDisplayItem(2L, "Leather Bag", "Apr 06, 2026 - 2:30 PM", 5, 3, -2),
    InventoryLogDisplayItem(3L, "Sun Hat", "Apr 05, 2026 - 9:00 AM", 0, 15, 15)
)

@Preview(showBackground = true)
@Composable
private fun InventoryHistoryLightPreview() {
    PhoebeStoreTheme {
        InventoryHistoryScreenContent(
            uiState = InventoryHistoryUiState(logs = previewLogs, isLoading = false, formattedFromDate = "Apr 01, 2026", formattedToDate = "Apr 07, 2026"),
            onNavigateBack = {}, onProductSelected = {}, onFromDateChange = {}, onToDateChange = {}, onResetFilters = {}
        )
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun InventoryHistoryDarkPreview() {
    PhoebeStoreTheme {
        InventoryHistoryScreenContent(
            uiState = InventoryHistoryUiState(logs = previewLogs, isLoading = false, formattedFromDate = "Apr 01, 2026", formattedToDate = "Apr 07, 2026"),
            onNavigateBack = {}, onProductSelected = {}, onFromDateChange = {}, onToDateChange = {}, onResetFilters = {}
        )
    }
}
