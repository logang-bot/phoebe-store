package com.example.phoebestore.domain.usecase

import com.example.phoebestore.domain.model.Product
import com.example.phoebestore.domain.model.Sale
import com.example.phoebestore.domain.repository.ProductRepository
import com.example.phoebestore.domain.repository.SaleRepository
import javax.inject.Inject

class RecordSaleUseCase @Inject constructor(
    private val saleRepository: SaleRepository,
    private val productRepository: ProductRepository
) {
    suspend operator fun invoke(sale: Sale, selectedProduct: Product?) {
        saleRepository.create(sale)
        selectedProduct?.let { product ->
            productRepository.update(product.copy(stock = (product.stock - sale.quantity).coerceAtLeast(0)))
        }
    }
}
