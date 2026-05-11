package com.example.phoebestore.presentation.screens

import kotlinx.serialization.Serializable

@Serializable
data object HomeScreen

@Serializable
data object StoreListScreen

@Serializable
data class StoreDetailScreen(val storeId: String)

@Serializable
data class CreateStoreScreen(val storeId: String? = null)

@Serializable
data class CreateProductScreen(val storeId: String, val productId: String? = null)

@Serializable
data class RecordSaleScreen(val storeId: String)

@Serializable
data class ProductListScreen(val storeId: String)

@Serializable
data class SalesListScreen(val storeId: String)

@Serializable
data class InventoryHistoryScreen(val storeId: String)

@Serializable
data class SaleDetailScreen(val saleId: String)

@Serializable
data class SalesReportScreen(
    val storeId: String,
    val fromDate: Long,
    val toDate: Long,
    val productId: String? = null
)

@Serializable
data class CreditSalesListScreen(val storeId: String)
