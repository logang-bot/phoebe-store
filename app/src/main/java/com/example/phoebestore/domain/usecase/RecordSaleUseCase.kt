package com.example.phoebestore.domain.usecase

import com.example.phoebestore.domain.model.InventoryLog
import com.example.phoebestore.domain.model.Product
import com.example.phoebestore.domain.model.Sale
import com.example.phoebestore.domain.repository.InventoryLogRepository
import com.example.phoebestore.domain.repository.ProductRepository
import com.example.phoebestore.domain.repository.SaleRepository
import javax.inject.Inject

class RecordSaleUseCase @Inject constructor(
    private val saleRepository: SaleRepository,
    private val productRepository: ProductRepository,
    private val inventoryLogRepository: InventoryLogRepository
) {
    suspend operator fun invoke(sale: Sale, selectedProduct: Product?, isCustomProduct: Boolean) {
        saleRepository.create(sale)
        if (selectedProduct != null) {
            val newStock = (selectedProduct.stock - sale.quantity).coerceAtLeast(0)
            productRepository.update(selectedProduct.copy(stock = newStock))
            if (sale.quantity > selectedProduct.stock) {
                inventoryLogRepository.log(
                    InventoryLog(
                        storeId = sale.storeId,
                        productId = selectedProduct.id,
                        productName = selectedProduct.name,
                        previousStock = selectedProduct.stock,
                        newStock = newStock
                    )
                )
            }
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
