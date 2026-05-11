package com.example.phoebestore.domain.usecase

import com.example.phoebestore.domain.model.Product
import com.example.phoebestore.domain.model.Sale
import com.example.phoebestore.domain.repository.ProductRepository
import com.example.phoebestore.domain.repository.SaleRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import javax.inject.Inject

class GetSalesHistoryUseCase @Inject constructor(
    private val saleRepository: SaleRepository,
    private val productRepository: ProductRepository
) {
    data class Result(
        val sales: List<Sale>,
        val products: List<Product>,
        val hasMore: Boolean
    )

    operator fun invoke(
        storeId: String,
        fromDate: Long,
        toDate: Long,
        productId: String?,
        limit: Int
    ): Flow<Result> = combine(
        saleRepository.getByStore(storeId),
        productRepository.getByStore(storeId)
    ) { sales, products ->
        val filtered = sales.filter { sale ->
            sale.soldAt in fromDate..toDate &&
                (productId == null || sale.productId == productId)
        }
        Result(
            sales = filtered.take(limit),
            products = products,
            hasMore = filtered.size > limit
        )
    }
}
