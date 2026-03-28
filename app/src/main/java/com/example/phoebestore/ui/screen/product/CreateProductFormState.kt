package com.example.phoebestore.ui.screen.product

import com.example.phoebestore.domain.model.Currency

data class CreateProductFormState(
    val name: String = "",
    val description: String = "",
    val price: String = "",
    val costPrice: String = "",
    val stock: String = "0",
    val imageUrl: String = "",
    val currency: Currency = Currency.USD,
    val isLoading: Boolean = false,
    val nameError: Boolean = false,
    val priceError: Boolean = false
)

sealed class CreateProductEvent {
    data object ProductSaved : CreateProductEvent()
}
