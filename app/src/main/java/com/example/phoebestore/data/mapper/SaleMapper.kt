package com.example.phoebestore.data.mapper

import com.example.phoebestore.data.local.entity.SaleEntity
import com.example.phoebestore.data.remote.dto.SaleDto
import com.example.phoebestore.domain.model.ProfitOutcome
import com.example.phoebestore.domain.model.Sale
import com.example.phoebestore.domain.model.SaleType

fun SaleEntity.toDomain(): Sale = Sale(
    id = id,
    storeId = storeId,
    productId = productId,
    productName = productName,
    quantity = quantity,
    unitPrice = unitPrice,
    unitCost = unitCost,
    totalAmount = totalAmount,
    saleType = runCatching { SaleType.valueOf(saleType) }.getOrDefault(SaleType.STANDARD),
    profitOutcome = runCatching { ProfitOutcome.valueOf(profitOutcome) }.getOrDefault(ProfitOutcome.NORMAL_PROFIT),
    notes = notes,
    onCredit = onCredit,
    creditPersonName = creditPersonName,
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
    saleType = runCatching { SaleType.valueOf(saleType) }.getOrDefault(SaleType.STANDARD),
    profitOutcome = runCatching { ProfitOutcome.valueOf(profitOutcome) }.getOrDefault(ProfitOutcome.NORMAL_PROFIT),
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
    saleType = saleType.name,
    profitOutcome = profitOutcome.name,
    notes = notes,
    onCredit = onCredit,
    creditPersonName = creditPersonName,
    soldAt = soldAt,
    createdAt = createdAt
)
