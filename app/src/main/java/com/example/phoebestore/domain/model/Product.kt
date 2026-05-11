package com.example.phoebestore.domain.model

data class Product(
    val id: String = "",
    val storeId: String,
    val name: String,
    val description: String = "",
    val price: Double,
    val costPrice: Double = 0.0,
    val stock: Int = 0,
    val imageUrl: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val deviceId: String = ""
)
