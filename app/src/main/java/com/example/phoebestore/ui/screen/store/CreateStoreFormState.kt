package com.example.phoebestore.ui.screen.store

import com.example.phoebestore.domain.model.Currency

data class CreateStoreFormState(
    val name: String = "",
    val description: String = "",
    val currency: Currency = Currency.USD,
    val logoUrl: String = "",
    val photoUrl: String = "",
    val isLoading: Boolean = false,
    val nameError: Boolean = false
)

sealed class CreateStoreEvent {
    data object StoreSaved : CreateStoreEvent()
}
