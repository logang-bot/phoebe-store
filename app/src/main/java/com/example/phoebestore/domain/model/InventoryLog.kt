package com.example.phoebestore.domain.model

data class InventoryLog(
    val id: Long = 0,
    val storeId: Long,
    val productId: Long,
    val productName: String,
    val previousStock: Int,
    val newStock: Int,
    val loggedAt: Long = System.currentTimeMillis()
)
