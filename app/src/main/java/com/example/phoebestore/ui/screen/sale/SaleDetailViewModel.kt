package com.example.phoebestore.ui.screen.sale

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.phoebestore.domain.repository.SaleRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class SaleDetailViewModel @Inject constructor(
    private val saleRepository: SaleRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val saleId: Long = checkNotNull(savedStateHandle["saleId"])

    private val dateFormat = SimpleDateFormat("MMM dd, yyyy - h:mm a", Locale.getDefault())

    private val _uiState = MutableStateFlow(SaleDetailUiState())
    val uiState: StateFlow<SaleDetailUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val sale = saleRepository.getById(saleId)
            _uiState.value = if (sale != null) {
                val profit = sale.quantity * (sale.unitPrice - sale.unitCost)
                SaleDetailUiState(
                    sale = sale,
                    formattedQuantity = "${sale.quantity}",
                    formattedUnitPrice = "%.2f".format(sale.unitPrice),
                    formattedUnitCost = "%.2f".format(sale.unitCost),
                    formattedProfit = "%.2f".format(profit),
                    formattedTotal = "%.2f".format(sale.totalAmount),
                    formattedDate = dateFormat.format(Date(sale.soldAt)),
                    showUnitCost = sale.unitCost > 0.0,
                    showNotes = sale.notes.isNotBlank()
                )
            } else {
                SaleDetailUiState()
            }
        }
    }
}
