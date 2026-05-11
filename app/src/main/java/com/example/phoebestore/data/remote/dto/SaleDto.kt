package com.example.phoebestore.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SaleDto(
    val id: String,
    @SerialName("store_id") val storeId: String,
    @SerialName("product_id") val productId: String? = null,
    @SerialName("product_name") val productName: String,
    val quantity: Int,
    @SerialName("unit_price") val unitPrice: Double,
    @SerialName("unit_cost") val unitCost: Double = 0.0,
    @SerialName("total_amount") val totalAmount: Double,
    @SerialName("sale_type") val saleType: String = "STANDARD",
    @SerialName("profit_outcome") val profitOutcome: String = "NORMAL_PROFIT",
    val notes: String = "",
    @SerialName("on_credit") val onCredit: Boolean = false,
    @SerialName("credit_person_name") val creditPersonName: String = "",
    @SerialName("sold_at") val soldAt: Long,
    @SerialName("created_at") val createdAt: Long,
    @SerialName("device_id") val deviceId: String = ""
)
