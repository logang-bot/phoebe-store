package com.example.phoebestore.ui.screen.sale

import com.example.phoebestore.domain.model.Sale

data class SaleDetailUiState(
    val sale: Sale? = null,
    val formattedQuantity: String = "",
    val formattedUnitPrice: String = "",
    val formattedUnitCost: String = "",
    val formattedProfit: String = "",
    val formattedTotal: String = "",
    val formattedDate: String = "",
    val showUnitCost: Boolean = false,
    val showNotes: Boolean = false
)
