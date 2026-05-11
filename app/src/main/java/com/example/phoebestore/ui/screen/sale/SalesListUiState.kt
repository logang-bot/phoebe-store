package com.example.phoebestore.ui.screen.sale

import com.example.phoebestore.domain.model.Product

data class SaleDisplayItem(
    val id: String,
    val productName: String,
    val formattedDate: String,
    val formattedTotal: String,
    val formattedQuantity: String,
    val isOnCredit: Boolean = false
)

data class SalesListUiState(
    val sales: List<SaleDisplayItem> = emptyList(),
    val products: List<Product> = emptyList(),
    val selectedProduct: Product? = null,
    val fromDate: Long = 0L,
    val toDate: Long = 0L,
    val formattedFromDate: String = "",
    val formattedToDate: String = "",
    val isLoading: Boolean = true,
    val hasMore: Boolean = false
)
