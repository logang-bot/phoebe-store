package com.example.phoebestore.ui.screen.sale

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.phoebestore.domain.model.Product
import com.example.phoebestore.domain.repository.ProductRepository
import com.example.phoebestore.domain.repository.SaleRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import javax.inject.Inject

private data class SaleFilters(
    val from: Long,
    val to: Long,
    val selectedProduct: Product?,
    val displayedCount: Int
)

@HiltViewModel
class SalesListViewModel @Inject constructor(
    private val saleRepository: SaleRepository,
    private val productRepository: ProductRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val storeId: Long = checkNotNull(savedStateHandle["storeId"])
    private val filterDateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
    private val saleItemDateFormat = SimpleDateFormat("MMM dd, yyyy - h:mm a", Locale.getDefault())

    private val _fromDate = MutableStateFlow(startOfDay(System.currentTimeMillis()))
    private val _toDate = MutableStateFlow(endOfDay(System.currentTimeMillis()))
    private val _selectedProduct = MutableStateFlow<Product?>(null)
    private val _displayedCount = MutableStateFlow(PAGE_SIZE)

    private val _filters = combine(_fromDate, _toDate, _selectedProduct, _displayedCount) { from, to, product, count ->
        SaleFilters(from, to, product, count)
    }

    val uiState: StateFlow<SalesListUiState> = combine(
        saleRepository.getByStore(storeId),
        productRepository.getByStore(storeId),
        _filters
    ) { sales, products, filters ->
        val filtered = sales.filter { sale ->
            sale.soldAt in filters.from..filters.to &&
                    (filters.selectedProduct == null || sale.productId == filters.selectedProduct.id)
        }
        val page = filtered.take(filters.displayedCount).map { sale ->
            SaleDisplayItem(
                id = sale.id,
                productName = sale.productName,
                formattedDate = saleItemDateFormat.format(Date(sale.soldAt)),
                formattedTotal = "%.2f".format(sale.totalAmount),
                formattedQuantity = "×${sale.quantity}",
                isOnCredit = sale.onCredit
            )
        }
        SalesListUiState(
            sales = page,
            products = products,
            selectedProduct = filters.selectedProduct,
            fromDate = filters.from,
            toDate = filters.to,
            formattedFromDate = filterDateFormat.format(Date(filters.from)),
            formattedToDate = filterDateFormat.format(Date(filters.to)),
            isLoading = false,
            hasMore = filtered.size > filters.displayedCount
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = SalesListUiState(
            fromDate = _fromDate.value,
            toDate = _toDate.value,
            formattedFromDate = filterDateFormat.format(Date(_fromDate.value)),
            formattedToDate = filterDateFormat.format(Date(_toDate.value)),
            isLoading = true
        )
    )

    fun onFromDateChange(epochMillis: Long) {
        val newFrom = startOfDay(epochMillis)
        _fromDate.value = newFrom
        if (newFrom > _toDate.value) _toDate.value = endOfDay(epochMillis)
        _displayedCount.value = PAGE_SIZE
    }

    fun onToDateChange(epochMillis: Long) {
        _toDate.value = endOfDay(epochMillis)
        _displayedCount.value = PAGE_SIZE
    }

    fun onProductSelected(product: Product?) {
        _selectedProduct.value = product
        _displayedCount.value = PAGE_SIZE
    }

    fun resetFilters() {
        val now = System.currentTimeMillis()
        _fromDate.value = startOfDay(now)
        _toDate.value = endOfDay(now)
        _selectedProduct.value = null
        _displayedCount.value = PAGE_SIZE
    }

    fun loadMore() {
        _displayedCount.update { it + PAGE_SIZE }
    }

    private fun startOfDay(epochMillis: Long): Long = Calendar.getInstance().apply {
        timeInMillis = epochMillis
        set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
    }.timeInMillis

    private fun endOfDay(epochMillis: Long): Long = Calendar.getInstance().apply {
        timeInMillis = epochMillis
        set(Calendar.HOUR_OF_DAY, 23); set(Calendar.MINUTE, 59)
        set(Calendar.SECOND, 59); set(Calendar.MILLISECOND, 999)
    }.timeInMillis

    companion object {
        private const val PAGE_SIZE = 15
    }
}
