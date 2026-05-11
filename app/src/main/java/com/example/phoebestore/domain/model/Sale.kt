package com.example.phoebestore.domain.model

data class Sale(
    val id: String = "",
    val storeId: String,
    val productId: String? = null,
    val productName: String,
    val quantity: Int,
    val unitPrice: Double,
    val unitCost: Double = 0.0,
    val totalAmount: Double,
    val saleType: SaleType = SaleType.STANDARD,
    val profitOutcome: ProfitOutcome = ProfitOutcome.NORMAL_PROFIT,
    val notes: String = "",
    val onCredit: Boolean = false,
    val creditPersonName: String = "",
    val soldAt: Long = System.currentTimeMillis(),
    val createdAt: Long = System.currentTimeMillis(),
    val deviceId: String = ""
)
