package com.example.phoebestore.presentation.screens

import kotlinx.serialization.Serializable

@Serializable
data object HomeScreen

@Serializable
data object StoreListScreen

@Serializable
data class StoreDetailScreen(val storeId: Long)

@Serializable
data class CreateStoreScreen(val storeId: Long? = null)

@Serializable
data class CreateProductScreen(val storeId: Long, val productId: Long? = null)

@Serializable
data class RecordSaleScreen(val storeId: Long)

@Serializable
data class ProductListScreen(val storeId: Long)

@Serializable
data class SalesListScreen(val storeId: Long)

@Serializable
data class SaleDetailScreen(val saleId: Long)
