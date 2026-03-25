package com.example.phoebestore.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ProductDto(
    val id: Long,
    @SerialName("store_id") val storeId: Long,
    val name: String,
    val description: String = "",
    val price: Double,
    @SerialName("cost_price") val costPrice: Double = 0.0,
    val stock: Int = 0,
    @SerialName("image_url") val imageUrl: String = "",
    @SerialName("created_at") val createdAt: Long
)
