package com.example.phoebestore.data.mapper

import com.example.phoebestore.data.local.entity.ProductEntity
import com.example.phoebestore.data.remote.dto.ProductDto
import com.example.phoebestore.domain.model.Product

fun ProductEntity.toDomain(): Product = Product(
    id = id,
    storeId = storeId,
    name = name,
    description = description,
    price = price,
    costPrice = costPrice,
    stock = stock,
    imageUrl = imageUrl,
    createdAt = createdAt
)

fun ProductDto.toDomain(): Product = Product(
    id = id,
    storeId = storeId,
    name = name,
    description = description,
    price = price,
    costPrice = costPrice,
    stock = stock,
    imageUrl = imageUrl,
    createdAt = createdAt
)

fun Product.toEntity(): ProductEntity = ProductEntity(
    id = id,
    storeId = storeId,
    name = name,
    description = description,
    price = price,
    costPrice = costPrice,
    stock = stock,
    imageUrl = imageUrl,
    createdAt = createdAt
)
