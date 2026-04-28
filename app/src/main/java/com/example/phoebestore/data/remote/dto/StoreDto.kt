package com.example.phoebestore.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class StoreDto(
    val id: Long,
    val name: String,
    val description: String = "",
    val currency: String = "USD",
    @SerialName("logo_url") val logoUrl: String = "",
    @SerialName("photo_url") val photoUrl: String = "",
    @SerialName("created_at") val createdAt: Long,
    @SerialName("device_id") val deviceId: String = ""
)
