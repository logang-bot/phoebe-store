package com.example.phoebestore.ui.screen.sale

import com.example.phoebestore.domain.model.Sale

data class SalesListUiState(
    val sales: List<Sale> = emptyList(),
    val fromDate: Long = 0L,
    val toDate: Long = 0L,
    val formattedFromDate: String = "",
    val formattedToDate: String = ""
)
