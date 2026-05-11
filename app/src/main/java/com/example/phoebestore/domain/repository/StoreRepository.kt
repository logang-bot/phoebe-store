package com.example.phoebestore.domain.repository

import com.example.phoebestore.domain.model.Store
import kotlinx.coroutines.flow.Flow

interface StoreRepository {
    suspend fun create(store: Store): String
    suspend fun update(store: Store)
    suspend fun getById(id: String): Store?
    fun getAll(): Flow<List<Store>>
    suspend fun delete(id: String)
    suspend fun markAccessed(id: String)
}
