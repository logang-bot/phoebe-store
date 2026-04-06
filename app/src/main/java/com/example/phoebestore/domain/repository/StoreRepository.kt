package com.example.phoebestore.domain.repository

import com.example.phoebestore.domain.model.Store
import kotlinx.coroutines.flow.Flow

interface StoreRepository {
    suspend fun create(store: Store): Long
    suspend fun update(store: Store)
    suspend fun getById(id: Long): Store?
    fun getAll(): Flow<List<Store>>
    suspend fun delete(id: Long)
    suspend fun markAccessed(id: Long)
}
