package com.example.phoebestore.data.repository.impl

import com.example.phoebestore.data.local.dao.SaleDao
import com.example.phoebestore.data.mapper.toDomain
import com.example.phoebestore.data.mapper.toEntity
import com.example.phoebestore.domain.model.Sale
import com.example.phoebestore.domain.repository.SaleRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class SaleRepositoryImpl @Inject constructor(
    private val dao: SaleDao
) : SaleRepository {

    override suspend fun create(sale: Sale): Long =
        dao.insert(sale.toEntity())

    override suspend fun getById(id: Long): Sale? =
        dao.getById(id)?.toDomain()

    override fun getByStore(storeId: Long): Flow<List<Sale>> =
        dao.getByStore(storeId).map { list -> list.map { it.toDomain() } }

    override suspend fun delete(id: Long) =
        dao.deleteById(id)
}
