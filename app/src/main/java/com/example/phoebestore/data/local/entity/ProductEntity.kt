package com.example.phoebestore.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "products",
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
data class ProductEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val storeId: Long,
    val name: String,
    val description: String = "",
    val price: Double,
    val costPrice: Double = 0.0,
    val stock: Int = 0,
    val imageUrl: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val deviceId: String = ""
)
