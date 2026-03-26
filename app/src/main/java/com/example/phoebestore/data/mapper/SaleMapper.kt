package com.example.phoebestore.data.mapper

import com.example.phoebestore.data.local.entity.SaleEntity
import com.example.phoebestore.data.remote.dto.SaleDto
import com.example.phoebestore.domain.model.Sale

fun SaleEntity.toDomain(): Sale = Sale(
    id = id,
    storeId = storeId,
    productId = productId,
    productName = productName,
    quantity = quantity,
    unitPrice = unitPrice,
    unitCost = unitCost,
    totalAmount = totalAmount,
    notes = notes,
    soldAt = soldAt,
    createdAt = createdAt
)

fun SaleDto.toDomain(): Sale = Sale(
    id = id,
    storeId = storeId,
    productId = productId,
    productName = productName,
    quantity = quantity,
    unitPrice = unitPrice,
    unitCost = unitCost,
    totalAmount = totalAmount,
    notes = notes,
    soldAt = soldAt,
    createdAt = createdAt
)

fun Sale.toEntity(): SaleEntity = SaleEntity(
    id = id,
    storeId = storeId,
    productId = productId,
    productName = productName,
    quantity = quantity,
    unitPrice = unitPrice,
    unitCost = unitCost,
    totalAmount = totalAmount,
    notes = notes,
    soldAt = soldAt,
    createdAt = createdAt
)
