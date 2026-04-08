package com.example.phoebestore.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "sales",
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
data class SaleEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val storeId: Long,
    val productId: Long? = null,
    val productName: String,
    val quantity: Int,
    val unitPrice: Double,
    val unitCost: Double = 0.0,
    val totalAmount: Double,
    val saleType: String = "STANDARD",
    val profitOutcome: String = "NORMAL_PROFIT",
    val notes: String = "",
    val onCredit: Boolean = false,
    val creditPersonName: String = "",
    val soldAt: Long = System.currentTimeMillis(),
    val createdAt: Long = System.currentTimeMillis()
)
