package com.example.phoebestore.ui.screen.sale

import com.example.phoebestore.domain.model.Product
import com.example.phoebestore.domain.model.Sale

data class SalesListUiState(
    val sales: List<Sale> = emptyList(),
    val products: List<Product> = emptyList(),
    val selectedProduct: Product? = null,
    val fromDate: Long = 0L,
    val toDate: Long = 0L,
    val formattedFromDate: String = "",
    val formattedToDate: String = "",
    val isLoading: Boolean = true,
    val hasMore: Boolean = false
)
