package com.example.phoebestore.domain.model

data class Store(
    val id: Long = 0,
    val name: String,
    val description: String = "",
    val currency: Currency = Currency.USD,
    val logoUrl: String = "",
    val photoUrl: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val lastAccessedAt: Long = 0
)
