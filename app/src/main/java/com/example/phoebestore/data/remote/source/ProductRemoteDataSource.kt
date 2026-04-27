package com.example.phoebestore.data.remote.source

import com.example.phoebestore.data.remote.dto.ProductDto

interface ProductRemoteDataSource {
    suspend fun getByStore(storeId: Long): List<ProductDto>
    suspend fun getById(id: Long): ProductDto?
    suspend fun insert(dto: ProductDto)
    suspend fun update(dto: ProductDto)
    suspend fun delete(id: Long)
}
