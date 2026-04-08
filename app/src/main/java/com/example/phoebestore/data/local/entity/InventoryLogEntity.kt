package com.example.phoebestore.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "inventory_logs",
    foreignKeys = [
        ForeignKey(
            entity = StoreEntity::class,
            parentColumns = ["id"],
            childColumns = ["storeId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("storeId")]
)
data class InventoryLogEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val storeId: Long,
    val productId: Long,
    val productName: String,
    val previousStock: Int,
    val newStock: Int,
    val loggedAt: Long = System.currentTimeMillis()
)
