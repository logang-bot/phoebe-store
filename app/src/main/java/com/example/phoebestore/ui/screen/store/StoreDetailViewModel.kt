package com.example.phoebestore.ui.screen.store

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.phoebestore.domain.repository.ProductRepository
import com.example.phoebestore.domain.repository.SaleRepository
import com.example.phoebestore.domain.repository.StoreRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class StoreDetailViewModel @Inject constructor(
    private val storeRepository: StoreRepository,
    private val saleRepository: SaleRepository,
    private val productRepository: ProductRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val storeId: Long = checkNotNull(savedStateHandle["storeId"])

    private val _uiState = MutableStateFlow(StoreDetailUiState())
    val uiState: StateFlow<StoreDetailUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            storeRepository.markAccessed(storeId)
            combine(
                storeRepository.getAll().map { list -> list.find { it.id == storeId } },
                saleRepository.getByStore(storeId),
                productRepository.getByStore(storeId)
            ) { store, sales, products ->
                val lowestStock = products.filter { it.stock <= 5 }.sortedBy { it.stock }.take(3)
                StoreDetailUiState(
                    store = store,
                    totalSales = sales.size,
                    formattedRevenue = "%.2f".format(sales.sumOf { it.totalAmount }),
                    formattedProfit = "%.2f".format(sales.sumOf { it.quantity * (it.unitPrice - it.unitCost) }),
                    totalStock = products.sumOf { it.stock },
                    lowStockAlerts = lowestStock.takeIf { it.isNotEmpty() }
                                         ?.joinToString(", ") { it.name }
                )
            }.collect { _uiState.value = it }
        }
    }

    fun deleteStore() {
        viewModelScope.launch {
            storeRepository.delete(storeId)
            _uiState.value = _uiState.value.copy(deleted = true)
        }
    }
}
