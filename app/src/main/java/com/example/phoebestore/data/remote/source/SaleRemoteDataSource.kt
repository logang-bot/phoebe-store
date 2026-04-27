package com.example.phoebestore.data.remote.source

import com.example.phoebestore.data.remote.dto.SaleDto

interface SaleRemoteDataSource {
    suspend fun getByStore(storeId: Long): List<SaleDto>
    suspend fun getById(id: Long): SaleDto?
    suspend fun insert(dto: SaleDto)
    suspend fun update(dto: SaleDto)
    suspend fun delete(id: Long)
}
