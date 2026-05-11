package com.example.phoebestore.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "pending_sync_ops")
data class SyncOperationEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val entityType: String,
    val entityId: String,
    val operation: String,
    val createdAt: Long = System.currentTimeMillis()
) {
    companion object {
        const val TYPE_STORE = "STORE"
        const val TYPE_PRODUCT = "PRODUCT"
        const val TYPE_SALE = "SALE"

        const val OP_CREATE = "CREATE"
        const val OP_UPDATE = "UPDATE"
        const val OP_DELETE = "DELETE"
    }
}
