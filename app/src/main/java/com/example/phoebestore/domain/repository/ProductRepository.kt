package com.example.phoebestore.domain.repository

import com.example.phoebestore.domain.model.Product
import kotlinx.coroutines.flow.Flow

interface ProductRepository {
    suspend fun create(product: Product): Long
    suspend fun update(product: Product)
    suspend fun getById(id: Long): Product?
    fun getByStore(storeId: Long): Flow<List<Product>>
    suspend fun delete(id: Long)
}
