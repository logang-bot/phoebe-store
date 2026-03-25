package com.example.phoebestore.domain.model

data class Store(
    val id: Long = 0,
    val name: String,
    val description: String = "",
    val currency: String = "USD",
    val logoUrl: String = "",
    val createdAt: Long = System.currentTimeMillis()
)
