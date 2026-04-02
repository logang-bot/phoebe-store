package com.example.phoebestore.ui.screen.product

import com.example.phoebestore.domain.model.Product

data class ProductListUiState(
    val products: List<Product> = emptyList(),
    val stockDialogProduct: Product? = null,
    val stockDialogInput: String = "",
    val isSavingStock: Boolean = false
)
