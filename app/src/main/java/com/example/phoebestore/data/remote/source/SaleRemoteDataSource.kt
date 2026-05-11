package com.example.phoebestore.data.remote.source

import com.example.phoebestore.data.remote.dto.SaleDto

interface SaleRemoteDataSource {
    suspend fun getByStore(storeId: String): List<SaleDto>
    suspend fun getById(id: String): SaleDto?
    suspend fun insert(dto: SaleDto)
    suspend fun update(dto: SaleDto)
    suspend fun delete(id: String)
}
