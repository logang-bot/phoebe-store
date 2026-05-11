package com.example.phoebestore.domain.repository

import com.example.phoebestore.domain.model.Product
import kotlinx.coroutines.flow.Flow

interface ProductRepository {
    suspend fun create(product: Product): String
    suspend fun update(product: Product)
    suspend fun getById(id: String): Product?
    fun getByStore(storeId: String): Flow<List<Product>>
    suspend fun delete(id: String)
}
