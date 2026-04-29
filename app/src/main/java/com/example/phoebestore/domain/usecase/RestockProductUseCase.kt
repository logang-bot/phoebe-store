package com.example.phoebestore.domain.usecase

import com.example.phoebestore.domain.model.InventoryLog
import com.example.phoebestore.domain.model.Product
import com.example.phoebestore.domain.repository.InventoryLogRepository
import com.example.phoebestore.domain.repository.ProductRepository
import javax.inject.Inject

class RestockProductUseCase @Inject constructor(
    private val productRepository: ProductRepository,
    private val inventoryLogRepository: InventoryLogRepository
) {
    suspend operator fun invoke(product: Product, newStock: Int) {
        require(newStock >= 0) { "Stock cannot be negative" }
        if (product.stock == newStock) return
        productRepository.update(product.copy(stock = newStock))
        inventoryLogRepository.log(
            InventoryLog(
                storeId = product.storeId,
                productId = product.id,
                productName = product.name,
                previousStock = product.stock,
                newStock = newStock
            )
        )
    }
}
