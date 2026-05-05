package com.example.phoebestore.data.repository.impl

import com.example.phoebestore.data.local.dao.ProductDao
import com.example.phoebestore.data.local.entity.SyncOperationEntity
import com.example.phoebestore.data.mapper.toDomain
import com.example.phoebestore.data.mapper.toEntity
import com.example.phoebestore.data.sync.SyncScheduler
import com.example.phoebestore.domain.model.Product
import com.example.phoebestore.domain.repository.ProductRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class ProductRepositoryImpl @Inject constructor(
    private val dao: ProductDao,
    private val syncScheduler: SyncScheduler
) : ProductRepository {

    override suspend fun create(product: Product): Long {
        val id = dao.insert(product.toEntity())
        syncScheduler.enqueue(SyncOperationEntity.TYPE_PRODUCT, id, SyncOperationEntity.OP_CREATE)
        return id
    }

    override suspend fun update(product: Product) {
        dao.update(product.toEntity())
        syncScheduler.enqueue(SyncOperationEntity.TYPE_PRODUCT, product.id, SyncOperationEntity.OP_UPDATE)
    }

    override suspend fun getById(id: Long): Product? =
        dao.getById(id)?.toDomain()

    override fun getByStore(storeId: Long): Flow<List<Product>> =
        dao.getByStore(storeId).map { list -> list.map { it.toDomain() } }

    override suspend fun delete(id: Long) {
        dao.deleteById(id)
        syncScheduler.enqueue(SyncOperationEntity.TYPE_PRODUCT, id, SyncOperationEntity.OP_DELETE)
    }
}
