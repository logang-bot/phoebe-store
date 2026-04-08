package com.example.phoebestore.ui.screen.sale

import com.example.phoebestore.domain.model.ProfitOutcome

data class DailyRevenueItem(
    val dayLabel: String,
    val fraction: Float
)

data class InventoryBarItem(
    val productName: String,
    val soldUnits: Int,
    val fraction: Float,
    val currentStock: Int
)

data class ProfitOutcomeBreakdownItem(
    val outcome: ProfitOutcome,
    val count: Int,
    val fraction: Float
)

data class SalesReportUiState(
    val isLoading: Boolean = true,
    val formattedFromDate: String = "",
    val formattedToDate: String = "",
    val filteredProductName: String? = null,
    val inventoryItems: List<InventoryBarItem> = emptyList(),
    val dailyRevenue: List<DailyRevenueItem> = emptyList(),
    val profitOutcomeBreakdown: List<ProfitOutcomeBreakdownItem> = emptyList(),
    val formattedTotalRevenue: String = "",
    val formattedTotalProfit: String = "",
    val creditSalesCount: Int = 0,
    val formattedCreditRevenue: String = "",
    val formattedCreditProfit: String = "",
    val hasData: Boolean = false
)
