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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
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
import com.example.phoebestore.domain.model.ProfitOutcome
import com.example.phoebestore.ui.common.ThemedCard
import com.example.phoebestore.ui.theme.PhoebeStoreTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SalesReportScreen(
    onNavigateBack: () -> Unit,
    viewModel: SalesReportViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.sales_report_title),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, stringResource(R.string.navigate_back))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
            )
        },
        containerColor = MaterialTheme.colorScheme.surfaceContainer
    ) { innerPadding ->
        when {
            uiState.isLoading -> LoadingContent(modifier = Modifier.fillMaxSize().padding(innerPadding))
            !uiState.hasData -> EmptyReportContent(modifier = Modifier.fillMaxSize().padding(innerPadding))
            else -> ReportContent(uiState = uiState, modifier = Modifier.padding(innerPadding))
        }
    }
}

@Composable
private fun ReportContent(uiState: SalesReportUiState, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        FilterOverviewCard(uiState = uiState, modifier = Modifier.fillMaxWidth())
        if (uiState.inventoryItems.isNotEmpty()) {
            InventoryChart(items = uiState.inventoryItems, modifier = Modifier.fillMaxWidth())
        }
        TotalsSection(uiState = uiState, modifier = Modifier.fillMaxWidth())
        if (uiState.dailyRevenue.isNotEmpty()) {
            DailyRevenueChart(items = uiState.dailyRevenue, modifier = Modifier.fillMaxWidth())
        }
        if (uiState.profitOutcomeBreakdown.isNotEmpty()) {
            ProfitOutcomeChart(items = uiState.profitOutcomeBreakdown, modifier = Modifier.fillMaxWidth())
        }
        Spacer(Modifier.height(8.dp))
    }
}

@Composable
private fun FilterOverviewCard(uiState: SalesReportUiState, modifier: Modifier = Modifier) {
    ThemedCard(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = stringResource(R.string.sales_report_period, uiState.formattedFromDate, uiState.formattedToDate),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = uiState.filteredProductName
                    ?.let { stringResource(R.string.sales_report_filter_product, it) }
                    ?: stringResource(R.string.sales_report_filter_all_products),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun TotalsSection(uiState: SalesReportUiState, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        RevenueAndProfitResume(
            uiState.formattedTotalRevenue,
            uiState.formattedTotalProfit,
            stringResource(R.string.sales_report_total_revenue_label),
            stringResource(R.string.sales_report_total_profit_label))

        if (uiState.creditSalesCount > 0) {
            Spacer(Modifier.height(12.dp))
            Text(
                text = stringResource(R.string.sales_report_credit_note_amount,
                    uiState.creditSalesCount),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.tertiary,
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Medium
            )
            Spacer(Modifier.height(12.dp))
            RevenueAndProfitResume(
                uiState.formattedCreditRevenue,
                uiState.formattedCreditProfit,
                stringResource(R.string.sales_report_total_credit_revenue_label),
                stringResource(R.string.sales_report_total_credit_profit_label)
            )
        }
    }
}

@Composable
private fun RevenueAndProfitResume(
    totalRevenue: String,
    totalProfit: String,
    revenueTitle: String = "",
    profitTitle: String = ""
) {
    Text(
        text = revenueTitle.ifEmpty { stringResource(R.string.revenue) },
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
    Text(
        text = totalRevenue,
        style = MaterialTheme.typography.displaySmall,
        fontWeight = FontWeight.Bold,
        textAlign = TextAlign.Center
    )
    Spacer(Modifier.height(8.dp))
    Text(
        text = profitTitle.ifEmpty { stringResource(R.string.profit) },
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
    Text(
        text = totalProfit,
        style = MaterialTheme.typography.headlineMedium,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.primary,
        textAlign = TextAlign.Center
    )
}

@Composable
private fun LoadingContent(modifier: Modifier = Modifier) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        CircularProgressIndicator()
    }
}

@Composable
private fun EmptyReportContent(modifier: Modifier = Modifier) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Text(
            text = stringResource(R.string.sales_report_no_data),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 32.dp)
        )
    }
}

private val previewUiState = SalesReportUiState(
    isLoading = false,
    formattedFromDate = "Apr 01, 2026",
    formattedToDate = "Apr 07, 2026",
    filteredProductName = null,
    inventoryItems = listOf(
        InventoryBarItem("Summer Dress", 42, 1f, 8),
        InventoryBarItem("Leather Bag", 27, 0.64f, 13),
        InventoryBarItem("Sun Hat", 15, 0.36f, 25)
    ),
    dailyRevenue = listOf(
        DailyRevenueItem("Apr 1", 0.6f), DailyRevenueItem("Apr 2", 1f),
        DailyRevenueItem("Apr 3", 0.4f), DailyRevenueItem("Apr 4", 0f),
        DailyRevenueItem("Apr 5", 0.8f), DailyRevenueItem("Apr 6", 0.5f),
        DailyRevenueItem("Apr 7", 0.7f)
    ),
    profitOutcomeBreakdown = listOf(
        ProfitOutcomeBreakdownItem(ProfitOutcome.NORMAL_PROFIT, 35, 0.72f),
        ProfitOutcomeBreakdownItem(ProfitOutcome.EXTRA_PROFIT, 7, 0.14f),
        ProfitOutcomeBreakdownItem(ProfitOutcome.SMALLER_PROFIT, 5, 0.10f),
        ProfitOutcomeBreakdownItem(ProfitOutcome.LOSS, 2, 0.04f)
    ),
    formattedTotalRevenue = "2,459.80",
    formattedTotalProfit = "987.40",
    creditSalesCount = 4,
    formattedCreditRevenue = "149.97",
    formattedCreditProfit = "59.97",
    hasData = true
)

@Preview(showBackground = true)
@Composable
private fun SalesReportScreenLightPreview() {
    PhoebeStoreTheme { ReportContent(uiState = previewUiState) }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun SalesReportScreenDarkPreview() {
    PhoebeStoreTheme { ReportContent(uiState = previewUiState) }
}
