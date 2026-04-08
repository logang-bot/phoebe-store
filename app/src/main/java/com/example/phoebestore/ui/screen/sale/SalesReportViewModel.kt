package com.example.phoebestore.ui.screen.sale

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.phoebestore.domain.model.Product
import com.example.phoebestore.domain.model.Sale
import com.example.phoebestore.domain.repository.ProductRepository
import com.example.phoebestore.domain.repository.SaleRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import javax.inject.Inject

private data class ReportParams(
    val storeId: Long,
    val fromDate: Long,
    val toDate: Long,
    val productId: Long?
) {
    companion object {
        fun from(handle: SavedStateHandle) = ReportParams(
            storeId = checkNotNull(handle["storeId"]),
            fromDate = checkNotNull(handle["fromDate"]),
            toDate = checkNotNull(handle["toDate"]),
            productId = handle["productId"]
        )
    }
}

@HiltViewModel
class SalesReportViewModel @Inject constructor(
    private val saleRepository: SaleRepository,
    private val productRepository: ProductRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val params = ReportParams.from(savedStateHandle)
    private val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
    private val dayKeyFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    private val dayLabelFormat = SimpleDateFormat("MMM d", Locale.getDefault())

    val uiState: StateFlow<SalesReportUiState> = combine(
        saleRepository.getByStore(params.storeId),
        productRepository.getByStore(params.storeId)
    ) { sales, products ->
        buildUiState(sales, products)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = SalesReportUiState(isLoading = true)
    )

    private fun buildUiState(sales: List<Sale>, products: List<Product>): SalesReportUiState {
        val filtered = filterSales(sales)
        val credit = computeCreditTotals(filtered)
        return SalesReportUiState(
            isLoading = false,
            formattedFromDate = dateFormat.format(Date(params.fromDate)),
            formattedToDate = dateFormat.format(Date(params.toDate)),
            filteredProductName = findFilteredProductName(products),
            inventoryItems = buildInventoryItems(filtered, products),
            dailyRevenue = buildDailyRevenue(filtered),
            profitOutcomeBreakdown = buildProfitOutcomeBreakdown(filtered),
            formattedTotalRevenue = "%.2f".format(filtered.sumOf { it.totalAmount }),
            formattedTotalProfit = "%.2f".format(filtered.sumOf { (it.unitPrice - it.unitCost) * it.quantity }),
            creditSalesCount = credit.count,
            formattedCreditRevenue = "%.2f".format(credit.revenue),
            formattedCreditProfit = "%.2f".format(credit.profit),
            hasData = filtered.isNotEmpty()
        )
    }

    private data class CreditTotals(val count: Int, val revenue: Double, val profit: Double)

    private fun computeCreditTotals(sales: List<Sale>): CreditTotals {
        val credit = sales.filter { it.onCredit }
        return CreditTotals(
            count = credit.size,
            revenue = credit.sumOf { it.totalAmount },
            profit = credit.sumOf { (it.unitPrice - it.unitCost) * it.quantity }
        )
    }

    private fun filterSales(sales: List<Sale>): List<Sale> =
        sales.filter { sale ->
            sale.soldAt in params.fromDate..params.toDate &&
                (params.productId == null || sale.productId == params.productId)
        }

    private fun buildInventoryItems(sales: List<Sale>, products: List<Product>): List<InventoryBarItem> {
        val productMap = products.associateBy { it.id }
        val grouped = sales
            .filter { it.productId != null }
            .groupBy { it.productId!! }
            .mapNotNull { (pid, salesList) ->
                val product = productMap[pid] ?: return@mapNotNull null
                Pair(product, salesList.sumOf { it.quantity })
            }
            .sortedByDescending { it.second }
        val max = grouped.maxOfOrNull { it.second }?.toFloat() ?: 1f
        return grouped.map { (product, sold) ->
            InventoryBarItem(product.name, sold, sold / max, product.stock)
        }
    }

    private fun buildDailyRevenue(sales: List<Sale>): List<DailyRevenueItem> {
        val revenueByDay = sales.groupBy { dayKeyFormat.format(Date(it.soldAt)) }
            .mapValues { (_, s) -> s.sumOf { it.totalAmount } }
        val days = enumerateDays(revenueByDay)
        val max = days.maxOfOrNull { it.second }?.toFloat()?.takeIf { it > 0 } ?: 1f
        return days.map { (label, rev) -> DailyRevenueItem(label, rev.toFloat() / max) }
    }

    private fun enumerateDays(revenueByDay: Map<String, Double>): List<Pair<String, Double>> {
        val cal = dayCalendar(params.fromDate)
        val end = dayCalendar(params.toDate)
        val days = mutableListOf<Pair<String, Double>>()
        while (!cal.after(end)) {
            days.add(dayLabelFormat.format(cal.time) to (revenueByDay[dayKeyFormat.format(cal.time)] ?: 0.0))
            cal.add(Calendar.DAY_OF_MONTH, 1)
        }
        return days
    }

    private fun dayCalendar(millis: Long): Calendar = Calendar.getInstance().apply {
        timeInMillis = millis
        set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
    }

    private fun buildProfitOutcomeBreakdown(sales: List<Sale>): List<ProfitOutcomeBreakdownItem> {
        if (sales.isEmpty()) return emptyList()
        val grouped = sales.groupBy { it.profitOutcome }.mapValues { (_, s) -> s.size }
        val total = sales.size.toFloat()
        return grouped.entries
            .sortedByDescending { it.value }
            .map { (outcome, count) -> ProfitOutcomeBreakdownItem(outcome, count, count / total) }
    }

    private fun findFilteredProductName(products: List<Product>): String? =
        params.productId?.let { pid -> products.find { it.id == pid }?.name }
}
