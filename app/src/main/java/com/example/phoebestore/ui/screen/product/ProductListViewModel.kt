package com.example.phoebestore.ui.screen.product

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.phoebestore.domain.model.Product
import com.example.phoebestore.domain.repository.ProductRepository
import com.example.phoebestore.domain.usecase.RestockProductUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProductListViewModel @Inject constructor(
    private val productRepository: ProductRepository,
    private val restockProduct: RestockProductUseCase,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    val storeId: String = checkNotNull(savedStateHandle["storeId"])

    private val _uiState = MutableStateFlow(ProductListUiState())
    val uiState: StateFlow<ProductListUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            productRepository.getByStore(storeId).collect { products ->
                _uiState.update { it.copy(products = products) }
            }
        }
    }

    fun onUpdateStockClick(product: Product) {
        _uiState.update { it.copy(stockDialogProduct = product, stockDialogInput = product.stock.toString()) }
    }

    fun onDismissStockDialog() {
        _uiState.update { it.copy(stockDialogProduct = null, stockDialogInput = "", isSavingStock = false) }
    }

    fun onStockInputChange(value: String) {
        if (value.isEmpty() || value.all { it.isDigit() }) {
            _uiState.update { it.copy(stockDialogInput = value) }
        }
    }

    fun onStockIncrement() {
        val current = _uiState.value.stockDialogInput.toIntOrNull() ?: 0
        _uiState.update { it.copy(stockDialogInput = (current + 1).toString()) }
    }

    fun onStockDecrement() {
        val current = _uiState.value.stockDialogInput.toIntOrNull() ?: 0
        if (current > 0) _uiState.update { it.copy(stockDialogInput = (current - 1).toString()) }
    }

    fun onSaveStock() {
        val product = _uiState.value.stockDialogProduct ?: return
        val newStock = _uiState.value.stockDialogInput.toIntOrNull() ?: return
        if (product.stock == newStock) { onDismissStockDialog(); return }
        viewModelScope.launch {
            _uiState.update { it.copy(isSavingStock = true) }
            restockProduct(product, newStock)
            _uiState.update { it.copy(isSavingStock = false, stockDialogProduct = null, stockDialogInput = "") }
        }
    }
}
