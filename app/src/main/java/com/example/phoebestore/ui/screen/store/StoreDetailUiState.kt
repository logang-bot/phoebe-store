package com.example.phoebestore.ui.screen.store

import com.example.phoebestore.domain.model.Store

data class StoreDetailUiState(
    val store: Store? = null,
    val totalSales: Int = 0,
    val formattedRevenue: String = "0.00",
    val formattedProfit: String = "0.00",
    val totalStock: Int = 0,
    val lowStockAlerts: String? = null,
    val deleted: Boolean = false
)
