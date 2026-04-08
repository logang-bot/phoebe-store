package com.example.phoebestore.domain.repository

import com.example.phoebestore.domain.model.Sale
import kotlinx.coroutines.flow.Flow

interface SaleRepository {
    suspend fun create(sale: Sale): Long
    suspend fun update(sale: Sale)
    suspend fun getById(id: Long): Sale?
    fun getByStore(storeId: Long): Flow<List<Sale>>
    fun getOnCreditByStore(storeId: Long): Flow<List<Sale>>
    suspend fun delete(id: Long)
}
