package com.example.phoebestore.ui.screen.sale

sealed class SaleResult {
    data class Success(
        val productName: String,
        val quantity: Int,
        val formattedTotal: String
    ) : SaleResult()
    data object Error : SaleResult()
}
