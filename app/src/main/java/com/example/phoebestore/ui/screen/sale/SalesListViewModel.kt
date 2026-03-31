package com.example.phoebestore.ui.screen.sale

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.phoebestore.domain.repository.SaleRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class SalesListViewModel @Inject constructor(
    private val saleRepository: SaleRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val storeId: Long = checkNotNull(savedStateHandle["storeId"])

    private val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())

    private val _fromDate = MutableStateFlow(startOfDay(System.currentTimeMillis()))
    private val _toDate = MutableStateFlow(endOfDay(System.currentTimeMillis()))

    val uiState: StateFlow<SalesListUiState> = combine(
        saleRepository.getByStore(storeId),
        _fromDate,
        _toDate
    ) { sales, from, to ->
        SalesListUiState(
            sales = sales.filter { it.soldAt in from..to },
            fromDate = from,
            toDate = to,
            formattedFromDate = dateFormat.format(Date(from)),
            formattedToDate = dateFormat.format(Date(to))
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = SalesListUiState(
            fromDate = _fromDate.value,
            toDate = _toDate.value,
            formattedFromDate = dateFormat.format(Date(_fromDate.value)),
            formattedToDate = dateFormat.format(Date(_toDate.value))
        )
    )

    fun onFromDateChange(epochMillis: Long) {
        val newFrom = startOfDay(epochMillis)
        _fromDate.value = newFrom
        if (newFrom > _toDate.value) {
            _toDate.value = endOfDay(epochMillis)
        }
    }

    fun onToDateChange(epochMillis: Long) {
        val newTo = endOfDay(epochMillis)
        _toDate.value = newTo
        if (newTo < _fromDate.value) {
            _fromDate.value = startOfDay(epochMillis)
        }
    }

    private fun startOfDay(epochMillis: Long): Long = Calendar.getInstance().apply {
        timeInMillis = epochMillis
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }.timeInMillis

    private fun endOfDay(epochMillis: Long): Long = Calendar.getInstance().apply {
        timeInMillis = epochMillis
        set(Calendar.HOUR_OF_DAY, 23)
        set(Calendar.MINUTE, 59)
        set(Calendar.SECOND, 59)
        set(Calendar.MILLISECOND, 999)
    }.timeInMillis
}
