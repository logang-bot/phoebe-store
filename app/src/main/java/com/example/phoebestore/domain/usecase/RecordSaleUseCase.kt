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
    suspend operator fun invoke(sale: Sale, selectedProduct: Product?, isCustomProduct: Boolean) {
        saleRepository.create(sale)
        if (selectedProduct != null) {
            productRepository.update(selectedProduct.copy(stock = (selectedProduct.stock - sale.quantity).coerceAtLeast(0)))
        } else if (isCustomProduct && sale.productName.isNotBlank()) {
            productRepository.create(
                Product(
                    storeId = sale.storeId,
                    name = sale.productName,
                    price = sale.unitPrice,
                    costPrice = sale.unitCost
                )
            )
        }
    }
}
