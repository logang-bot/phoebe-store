package com.example.phoebestore.domain.model

data class Sale(
    val id: Long = 0,
    val storeId: Long,
    val productId: Long? = null,
    val productName: String,
    val quantity: Int,
    val unitPrice: Double,
    val unitCost: Double = 0.0,
    val totalAmount: Double,
    val notes: String = "",
    val soldAt: Long = System.currentTimeMillis(),
    val createdAt: Long = System.currentTimeMillis()
)
