package com.example.phoebestore.data.mapper

import com.example.phoebestore.data.local.entity.StoreEntity
import com.example.phoebestore.data.remote.dto.StoreDto
import com.example.phoebestore.domain.model.Store

fun StoreEntity.toDomain(): Store = Store(
    id = id,
    name = name,
    description = description,
    currency = currency,
    logoUrl = logoUrl,
    createdAt = createdAt
)

fun StoreDto.toDomain(): Store = Store(
    id = id,
    name = name,
    description = description,
    currency = currency,
    logoUrl = logoUrl,
    createdAt = createdAt
)

fun Store.toEntity(): StoreEntity = StoreEntity(
    id = id,
    name = name,
    description = description,
    currency = currency,
    logoUrl = logoUrl,
    createdAt = createdAt
)
