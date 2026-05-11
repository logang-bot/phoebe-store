package com.example.phoebestore.domain.repository

import com.example.phoebestore.domain.model.Sale
import kotlinx.coroutines.flow.Flow

interface SaleRepository {
    suspend fun create(sale: Sale): String
    suspend fun update(sale: Sale)
    suspend fun getById(id: String): Sale?
    fun getByStore(storeId: String): Flow<List<Sale>>
    fun getOnCreditByStore(storeId: String): Flow<List<Sale>>
    suspend fun delete(id: String)
}
