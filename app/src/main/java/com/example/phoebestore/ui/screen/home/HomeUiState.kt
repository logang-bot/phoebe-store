package com.example.phoebestore.ui.screen.home

import com.example.phoebestore.domain.model.Store

data class HomeUiState(
    val lastStore: Store? = null,
    val totalSales: Int = 0,
    val formattedRevenue: String = "0.00",
    val formattedProfit: String = "0.00",
    val totalStock: Int = 0,
    val lowStockAlerts: String? = null,
    val hasProducts: Boolean = false,
    val isInitialized: Boolean = false
)
