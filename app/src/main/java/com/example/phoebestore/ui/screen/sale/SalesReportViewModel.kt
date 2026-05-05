package com.example.phoebestore.ui.screen.sale

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.phoebestore.domain.usecase.GetSalesSummaryUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
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
    private val getSalesSummary: GetSalesSummaryUseCase,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val params = ReportParams.from(savedStateHandle)
    private val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
    private val dayLabelFormat = SimpleDateFormat("MMM d", Locale.getDefault())
    private val dayKeyFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    val uiState: StateFlow<SalesReportUiState> = getSalesSummary(
        storeId = params.storeId,
        fromDate = params.fromDate,
        toDate = params.toDate,
        productId = params.productId
    ).map { summary ->
        val outcomeTotal = summary.profitOutcomeBreakdown.values.sum().toFloat()
        val inventoryMax = summary.unitsSoldByProduct.maxOfOrNull { it.second }?.toFloat() ?: 1f
        SalesReportUiState(
            isLoading = false,
            formattedFromDate = dateFormat.format(Date(params.fromDate)),
            formattedToDate = dateFormat.format(Date(params.toDate)),
            filteredProductName = params.productId?.let { pid ->
                summary.products.find { it.id == pid }?.name
            },
            inventoryItems = summary.unitsSoldByProduct.map { (product, sold) ->
                InventoryBarItem(product.name, sold, sold / inventoryMax, product.stock)
            },
            dailyRevenue = buildDailyRevenue(summary.revenueByDay),
            profitOutcomeBreakdown = summary.profitOutcomeBreakdown.entries
                .sortedByDescending { it.value }
                .map { (outcome, count) ->
                    ProfitOutcomeBreakdownItem(outcome, count, count / outcomeTotal)
                },
            formattedTotalRevenue = "%.2f".format(summary.totalRevenue),
            formattedTotalProfit = "%.2f".format(summary.totalProfit),
            creditSalesCount = summary.creditCount,
            formattedCreditRevenue = "%.2f".format(summary.creditRevenue),
            formattedCreditProfit = "%.2f".format(summary.creditProfit),
            hasData = summary.saleCount > 0,
            zeroCostPriceProductCount = summary.zeroCostPriceProductCount
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = SalesReportUiState(isLoading = true)
    )

    private fun buildDailyRevenue(revenueByDay: Map<String, Double>): List<DailyRevenueItem> {
        val cal = dayCalendar(params.fromDate)
        val end = dayCalendar(params.toDate)
        val days = mutableListOf<Pair<String, Double>>()
        while (!cal.after(end)) {
            val key = dayKeyFormat.format(cal.time)
            days.add(dayLabelFormat.format(cal.time) to (revenueByDay[key] ?: 0.0))
            cal.add(Calendar.DAY_OF_MONTH, 1)
        }
        val max = days.maxOfOrNull { it.second }?.toFloat()?.takeIf { it > 0 } ?: 1f
        return days.map { (label, rev) -> DailyRevenueItem(label, rev.toFloat() / max) }
    }

    private fun dayCalendar(millis: Long): Calendar = Calendar.getInstance().apply {
        timeInMillis = millis
        set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
    }
}
