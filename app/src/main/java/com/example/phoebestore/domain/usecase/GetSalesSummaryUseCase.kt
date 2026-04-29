package com.example.phoebestore.domain.usecase

import com.example.phoebestore.domain.model.ProfitOutcome
import com.example.phoebestore.domain.model.Product
import com.example.phoebestore.domain.repository.ProductRepository
import com.example.phoebestore.domain.repository.SaleRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import java.util.Calendar
import javax.inject.Inject

class GetSalesSummaryUseCase @Inject constructor(
    private val saleRepository: SaleRepository,
    private val productRepository: ProductRepository
) {
    data class Summary(
        val products: List<Product>,
        val saleCount: Int,
        val totalRevenue: Double,
        val totalProfit: Double,
        val creditCount: Int,
        val creditRevenue: Double,
        val creditProfit: Double,
        val revenueByDay: Map<String, Double>,
        val unitsSoldByProduct: List<Pair<Product, Int>>,
        val profitOutcomeBreakdown: Map<ProfitOutcome, Int>
    )

    operator fun invoke(
        storeId: Long,
        fromDate: Long,
        toDate: Long,
        productId: Long?
    ): Flow<Summary> = combine(
        saleRepository.getByStore(storeId),
        productRepository.getByStore(storeId)
    ) { sales, products ->
        val filtered = sales.filter { sale ->
            sale.soldAt in fromDate..toDate &&
                (productId == null || sale.productId == productId)
        }
        val productMap = products.associateBy { it.id }
        val creditSales = filtered.filter { it.onCredit }

        Summary(
            products = products,
            saleCount = filtered.size,
            totalRevenue = filtered.sumOf { it.totalAmount },
            totalProfit = filtered.sumOf { (it.unitPrice - it.unitCost) * it.quantity },
            creditCount = creditSales.size,
            creditRevenue = creditSales.sumOf { it.totalAmount },
            creditProfit = creditSales.sumOf { (it.unitPrice - it.unitCost) * it.quantity },
            revenueByDay = filtered
                .groupBy { dayKey(it.soldAt) }
                .mapValues { (_, s) -> s.sumOf { it.totalAmount } },
            unitsSoldByProduct = filtered
                .filter { it.productId != null }
                .groupBy { it.productId!! }
                .mapNotNull { (pid, salesList) ->
                    val product = productMap[pid] ?: return@mapNotNull null
                    product to salesList.sumOf { it.quantity }
                }
                .sortedByDescending { it.second },
            profitOutcomeBreakdown = filtered
                .groupBy { it.profitOutcome }
                .mapValues { (_, s) -> s.size }
        )
    }

    private fun dayKey(epochMillis: Long): String {
        val cal = Calendar.getInstance()
        cal.timeInMillis = epochMillis
        return "%04d-%02d-%02d".format(
            cal.get(Calendar.YEAR),
            cal.get(Calendar.MONTH) + 1,
            cal.get(Calendar.DAY_OF_MONTH)
        )
    }
}
