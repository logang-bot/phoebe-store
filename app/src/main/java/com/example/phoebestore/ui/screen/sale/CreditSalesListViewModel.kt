package com.example.phoebestore.ui.screen.sale

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.phoebestore.domain.model.Sale
import com.example.phoebestore.domain.repository.SaleRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class CreditSalesListViewModel @Inject constructor(
    private val saleRepository: SaleRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val storeId: Long = checkNotNull(savedStateHandle["storeId"])
    private val filterDateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
    private val saleItemDateFormat = SimpleDateFormat("MMM dd, yyyy - h:mm a", Locale.getDefault())

    private val _fromDate = MutableStateFlow(startOfYear())
    private val _toDate = MutableStateFlow(endOfDay(System.currentTimeMillis()))

    val uiState: StateFlow<CreditSalesListUiState> = combine(
        saleRepository.getOnCreditByStore(storeId),
        _fromDate,
        _toDate
    ) { sales, from, to ->
        buildUiState(sales, from, to)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = CreditSalesListUiState(
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
    }

    fun onToDateChange(epochMillis: Long) {
        _toDate.value = endOfDay(epochMillis)
    }

    fun resetFilters() {
        _fromDate.value = startOfYear()
        _toDate.value = endOfDay(System.currentTimeMillis())
    }

    fun markAsDone(saleId: Long) {
        viewModelScope.launch {
            val sale = saleRepository.getById(saleId) ?: return@launch
            saleRepository.update(sale.copy(onCredit = false))
        }
    }

    private fun buildUiState(sales: List<Sale>, from: Long, to: Long) = CreditSalesListUiState(
        sales = sales.filter { it.soldAt in from..to }.map { it.toDisplayItem() },
        fromDate = from,
        toDate = to,
        formattedFromDate = filterDateFormat.format(Date(from)),
        formattedToDate = filterDateFormat.format(Date(to)),
        isLoading = false
    )

    private fun Sale.toDisplayItem() = CreditSaleDisplayItem(
        id = id,
        productName = productName,
        creditPersonName = creditPersonName,
        formattedDate = saleItemDateFormat.format(Date(soldAt)),
        formattedTotal = "%.2f".format(totalAmount),
        quantity = quantity
    )

    private fun startOfYear(): Long = Calendar.getInstance().apply {
        set(Calendar.MONTH, Calendar.JANUARY)
        set(Calendar.DAY_OF_MONTH, 1)
        set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
    }.timeInMillis

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
}
