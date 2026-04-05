package com.example.phoebestore.ui.screen.sale

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.phoebestore.R
import com.example.phoebestore.domain.model.Sale
import com.example.phoebestore.ui.common.ThemedCard
import com.example.phoebestore.ui.theme.PhoebeStoreTheme

@Composable
fun SaleDetailScreen(
    onNavigateBack: () -> Unit,
    viewModel: SaleDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    SaleDetailScreenContent(
        uiState = uiState,
        onNavigateBack = onNavigateBack
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SaleDetailScreenContent(
    uiState: SaleDetailUiState,
    onNavigateBack: () -> Unit
) {
    Scaffold(
        containerColor = MaterialTheme.colorScheme.surfaceContainer,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.sale_detail_title),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.sale_detail_back)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainer
                )
            )
        }
    ) { innerPadding ->
        if (uiState.sale != null) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(innerPadding)
                    .padding(horizontal = 16.dp, vertical = 16.dp)
            ) {
                SaleDetailCard(uiState = uiState)
            }
        }
    }
}

@Composable
private fun SaleDetailCard(uiState: SaleDetailUiState) {
    val sale = uiState.sale ?: return
    val rows = buildList {
        add(stringResource(R.string.sale_detail_product) to sale.productName)
        add(stringResource(R.string.sale_detail_quantity) to uiState.formattedQuantity)
        add(stringResource(R.string.sale_detail_unit_price) to uiState.formattedUnitPrice)
        if (uiState.showUnitCost) {
            add(stringResource(R.string.sale_detail_unit_cost) to uiState.formattedUnitCost)
            add(stringResource(R.string.sale_detail_profit) to uiState.formattedProfit)
        }
        add(stringResource(R.string.sale_detail_total) to uiState.formattedTotal)
        add(stringResource(R.string.sale_detail_date) to uiState.formattedDate)
        if (uiState.showNotes) {
            add(stringResource(R.string.sale_detail_notes) to sale.notes)
        }
    }

    ThemedCard(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            rows.forEachIndexed { index, (label, value) ->
                if (index > 0) {
                    Spacer(modifier = Modifier.height(12.dp))
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                    Spacer(modifier = Modifier.height(12.dp))
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = value,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}

private val previewSale = Sale(
    id = 1L,
    storeId = 1L,
    productName = "Summer Dress",
    quantity = 2,
    unitPrice = 29.99,
    unitCost = 15.00,
    totalAmount = 59.98,
    notes = "Customer paid cash"
)

private val previewUiState = SaleDetailUiState(
    sale = previewSale,
    formattedQuantity = "2",
    formattedUnitPrice = "29.99",
    formattedUnitCost = "15.00",
    formattedProfit = "29.98",
    formattedTotal = "59.98",
    formattedDate = "Apr 03, 2026 - 3:00 PM",
    showUnitCost = true,
    showNotes = true
)

@Preview(showBackground = true)
@Composable
private fun SaleDetailScreenLightPreview() {
    PhoebeStoreTheme {
        SaleDetailScreenContent(
            uiState = previewUiState,
            onNavigateBack = {}
        )
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun SaleDetailScreenDarkPreview() {
    PhoebeStoreTheme {
        SaleDetailScreenContent(
            uiState = previewUiState,
            onNavigateBack = {}
        )
    }
}
