package com.example.phoebestore.data.repository.impl

import com.example.phoebestore.data.local.dao.ProductDao
import com.example.phoebestore.data.mapper.toDomain
import com.example.phoebestore.data.mapper.toEntity
import com.example.phoebestore.domain.model.Product
import com.example.phoebestore.domain.repository.ProductRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class ProductRepositoryImpl @Inject constructor(
    private val dao: ProductDao
) : ProductRepository {

    override suspend fun create(product: Product): Long =
        dao.insert(product.toEntity())

    override suspend fun update(product: Product) =
        dao.update(product.toEntity())

    override suspend fun getById(id: Long): Product? =
        dao.getById(id)?.toDomain()

    override fun getByStore(storeId: Long): Flow<List<Product>> =
        dao.getByStore(storeId).map { list -> list.map { it.toDomain() } }

    override suspend fun delete(id: Long) =
        dao.deleteById(id)
}
