package com.example.phoebestore.data.mapper

import com.example.phoebestore.data.local.entity.InventoryLogEntity
import com.example.phoebestore.domain.model.InventoryLog

fun InventoryLogEntity.toDomain(): InventoryLog =
    InventoryLog(id, storeId, productId, productName, previousStock, newStock, loggedAt)

fun InventoryLog.toEntity(): InventoryLogEntity =
    InventoryLogEntity(id, storeId, productId, productName, previousStock, newStock, loggedAt)
