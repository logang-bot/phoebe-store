package com.example.phoebestore.data.mapper

import com.example.phoebestore.data.local.entity.StoreEntity
import com.example.phoebestore.data.remote.dto.StoreDto
import com.example.phoebestore.domain.model.Currency
import com.example.phoebestore.domain.model.Store

private fun String.toCurrency(): Currency =
    Currency.entries.firstOrNull { it.name == this } ?: Currency.USD

fun StoreEntity.toDomain(): Store = Store(
    id = id,
    name = name,
    description = description,
    currency = currency.toCurrency(),
    logoUrl = logoUrl,
    photoUrl = photoUrl,
    createdAt = createdAt,
    lastAccessedAt = lastAccessedAt
)

fun StoreDto.toDomain(): Store = Store(
    id = id,
    name = name,
    description = description,
    currency = currency.toCurrency(),
    logoUrl = logoUrl,
    photoUrl = photoUrl,
    createdAt = createdAt
)

fun Store.toDto(): StoreDto = StoreDto(
    id = id,
    name = name,
    description = description,
    currency = currency.name,
    logoUrl = logoUrl,
    photoUrl = photoUrl,
    createdAt = createdAt
)

fun Store.toEntity(): StoreEntity = StoreEntity(
    id = id,
    name = name,
    description = description,
    currency = currency.name,
    logoUrl = logoUrl,
    photoUrl = photoUrl,
    createdAt = createdAt,
    lastAccessedAt = lastAccessedAt
)
