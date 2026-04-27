package com.example.phoebestore.data.repository.impl

import com.example.phoebestore.data.local.dao.ProductDao
import com.example.phoebestore.data.mapper.toDomain
import com.example.phoebestore.data.mapper.toDto
import com.example.phoebestore.data.mapper.toEntity
import com.example.phoebestore.data.remote.source.ProductRemoteDataSource
import com.example.phoebestore.data.sync.RemoteErrorHandler
import com.example.phoebestore.domain.model.Product
import com.example.phoebestore.domain.repository.ProductRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class ProductRepositoryImpl @Inject constructor(
    private val dao: ProductDao,
    private val remote: ProductRemoteDataSource,
    private val errorHandler: RemoteErrorHandler
) : ProductRepository {

    override suspend fun create(product: Product): Long {
        val id = dao.insert(product.toEntity())
        runCatching { remote.insert(product.copy(id = id).toDto()) }
            .onFailure { errorHandler.log("ProductCreate", it) }
        return id
    }

    override suspend fun update(product: Product) {
        dao.update(product.toEntity())
        runCatching { remote.update(product.toDto()) }
            .onFailure { errorHandler.log("ProductUpdate", it) }
    }

    override suspend fun getById(id: Long): Product? =
        dao.getById(id)?.toDomain()

    override fun getByStore(storeId: Long): Flow<List<Product>> =
        dao.getByStore(storeId).map { list -> list.map { it.toDomain() } }

    override suspend fun delete(id: Long) {
        dao.deleteById(id)
        runCatching { remote.delete(id) }
            .onFailure { errorHandler.log("ProductDelete", it) }
    }
}
