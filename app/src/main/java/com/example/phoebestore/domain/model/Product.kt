package com.example.phoebestore.domain.model

data class Product(
    val id: Long = 0,
    val storeId: Long,
    val name: String,
    val description: String = "",
    val price: Double,
    val costPrice: Double = 0.0,
    val stock: Int = 0,
    val imageUrl: String = "",
    val createdAt: Long = System.currentTimeMillis()
)
