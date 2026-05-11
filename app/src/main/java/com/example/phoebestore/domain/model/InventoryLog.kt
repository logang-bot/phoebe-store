package com.example.phoebestore.domain.model

data class InventoryLog(
    val id: Long = 0,
    val storeId: String,
    val productId: String,
    val productName: String,
    val previousStock: Int,
    val newStock: Int,
    val loggedAt: Long = System.currentTimeMillis()
)
