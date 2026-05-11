package com.example.phoebestore.ui.screen.product

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.phoebestore.domain.model.InventoryLog
import com.example.phoebestore.domain.model.Product
import com.example.phoebestore.domain.repository.InventoryLogRepository
import com.example.phoebestore.domain.repository.ProductRepository
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

private data class LogFilters(val from: Long, val to: Long, val selectedProduct: Product?)

@HiltViewModel
class InventoryHistoryViewModel @Inject constructor(
    private val inventoryLogRepository: InventoryLogRepository,
    private val productRepository: ProductRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val storeId: String = checkNotNull(savedStateHandle["storeId"])
    private val filterDateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
    private val logDateFormat = SimpleDateFormat("MMM dd, yyyy - h:mm a", Locale.getDefault())

    private val _fromDate = MutableStateFlow(startOfDay(System.currentTimeMillis()))
    private val _toDate = MutableStateFlow(endOfDay(System.currentTimeMillis()))
    private val _selectedProduct = MutableStateFlow<Product?>(null)

    private val _filters = combine(_fromDate, _toDate, _selectedProduct) { from, to, product ->
        LogFilters(from, to, product)
    }

    val uiState: StateFlow<InventoryHistoryUiState> = combine(
        inventoryLogRepository.getByStore(storeId),
        productRepository.getByStore(storeId),
        _filters
    ) { logs, products, filters ->
        buildUiState(logs, products, filters)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = InventoryHistoryUiState(
            fromDate = _fromDate.value,
            toDate = _toDate.value,
            formattedFromDate = filterDateFormat.format(Date(_fromDate.value)),
            formattedToDate = filterDateFormat.format(Date(_toDate.value)),
            isLoading = true
        )
    )

    private fun buildUiState(logs: List<InventoryLog>, products: List<Product>, filters: LogFilters): InventoryHistoryUiState {
        val filtered = logs.filter { it.loggedAt in filters.from..filters.to && (filters.selectedProduct == null || it.productId == filters.selectedProduct.id) }
        return InventoryHistoryUiState(
            logs = filtered.map { toDisplayItem(it) },
            products = products,
            selectedProduct = filters.selectedProduct,
            fromDate = filters.from,
            toDate = filters.to,
            formattedFromDate = filterDateFormat.format(Date(filters.from)),
            formattedToDate = filterDateFormat.format(Date(filters.to)),
            isLoading = false
        )
    }

    private fun toDisplayItem(log: InventoryLog): InventoryLogDisplayItem = InventoryLogDisplayItem(
        id = log.id,
        productName = log.productName,
        formattedDate = logDateFormat.format(Date(log.loggedAt)),
        previousStock = log.previousStock,
        newStock = log.newStock,
        delta = log.newStock - log.previousStock
    )

    fun onFromDateChange(epochMillis: Long) {
        val newFrom = startOfDay(epochMillis)
        _fromDate.value = newFrom
        if (newFrom > _toDate.value) _toDate.value = endOfDay(epochMillis)
    }

    fun onToDateChange(epochMillis: Long) { _toDate.value = endOfDay(epochMillis) }

    fun onProductSelected(product: Product?) { _selectedProduct.value = product }

    fun resetFilters() {
        val now = System.currentTimeMillis()
        _fromDate.value = startOfDay(now)
        _toDate.value = endOfDay(now)
        _selectedProduct.value = null
    }

    private fun startOfDay(epochMillis: Long): Long = Calendar.getInstance().apply {
        timeInMillis = epochMillis
        set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
    }.timeInMillis

    private fun endOfDay(epochMillis: Long): Long = Calendar.getInstance().apply {
        timeInMillis = epochMillis
        set(Calendar.HOUR_OF_DAY, 23); set(Calendar.MINUTE, 59); set(Calendar.SECOND, 59); set(Calendar.MILLISECOND, 999)
    }.timeInMillis
}
