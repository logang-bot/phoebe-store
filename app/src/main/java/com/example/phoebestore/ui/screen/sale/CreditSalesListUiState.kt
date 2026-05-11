package com.example.phoebestore.ui.screen.sale

data class CreditSaleDisplayItem(
    val id: String,
    val productName: String,
    val creditPersonName: String,
    val formattedDate: String,
    val formattedTotal: String,
    val quantity: Int
)

data class CreditSalesListUiState(
    val sales: List<CreditSaleDisplayItem> = emptyList(),
    val fromDate: Long = 0L,
    val toDate: Long = 0L,
    val formattedFromDate: String = "",
    val formattedToDate: String = "",
    val isLoading: Boolean = true
)
