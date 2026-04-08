package com.example.phoebestore.ui.screen.product

import com.example.phoebestore.domain.model.Product

data class InventoryLogDisplayItem(
    val id: Long,
    val productName: String,
    val formattedDate: String,
    val previousStock: Int,
    val newStock: Int,
    val delta: Int
)

data class InventoryHistoryUiState(
    val logs: List<InventoryLogDisplayItem> = emptyList(),
    val products: List<Product> = emptyList(),
    val selectedProduct: Product? = null,
    val fromDate: Long = 0L,
    val toDate: Long = 0L,
    val formattedFromDate: String = "",
    val formattedToDate: String = "",
    val isLoading: Boolean = true
)
