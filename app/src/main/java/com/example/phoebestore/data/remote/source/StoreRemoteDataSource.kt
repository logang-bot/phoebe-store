package com.example.phoebestore.data.remote.source

import com.example.phoebestore.data.remote.dto.StoreDto

interface StoreRemoteDataSource {
    suspend fun getAll(): List<StoreDto>
    suspend fun getById(id: String): StoreDto?
    suspend fun insert(dto: StoreDto)
    suspend fun update(dto: StoreDto)
    suspend fun delete(id: String)
}
