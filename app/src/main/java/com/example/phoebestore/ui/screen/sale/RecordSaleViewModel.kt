package com.example.phoebestore.ui.screen.sale

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.phoebestore.domain.model.Product
import com.example.phoebestore.domain.model.Sale
import com.example.phoebestore.domain.repository.ProductRepository
import com.example.phoebestore.domain.repository.SaleRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class RecordSaleFormState(
    val products: List<Product> = emptyList(),
    val selectedProduct: Product? = null,
    val isCustomProduct: Boolean = false,
    val productName: String = "",
    val quantity: String = "1",
    val unitPrice: String = "",
    val unitCost: String = "",
    val notes: String = "",
    val soldAt: Long = System.currentTimeMillis(),
    val isSaving: Boolean = false,
    val isSuccess: Boolean = false,
    val productNameError: Boolean = false,
    val quantityError: Boolean = false,
    val unitPriceError: Boolean = false
)

@HiltViewModel
class RecordSaleViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val saleRepository: SaleRepository,
    private val productRepository: ProductRepository
) : ViewModel() {

    private val storeId: Long = checkNotNull(savedStateHandle["storeId"])

    private val _formState = MutableStateFlow(RecordSaleFormState())
    val formState: StateFlow<RecordSaleFormState> = _formState.asStateFlow()

    init {
        viewModelScope.launch {
            productRepository.getByStore(storeId).collect { products ->
                _formState.update { it.copy(products = products) }
            }
        }
    }

    fun onProductSelected(product: Product?) {
        _formState.update {
            it.copy(
                selectedProduct = product,
                isCustomProduct = product == null,
                productName = product?.name ?: "",
                unitPrice = product?.price?.toString() ?: "",
                unitCost = product?.costPrice?.toString() ?: "",
                productNameError = false,
                unitPriceError = false
            )
        }
    }

    fun onCustomProductSelected() {
        _formState.update {
            it.copy(
                selectedProduct = null,
                isCustomProduct = true,
                productName = "",
                unitPrice = "",
                unitCost = "",
                productNameError = false,
                unitPriceError = false
            )
        }
    }

    fun onProductNameChange(name: String) {
        _formState.update { it.copy(productName = name, productNameError = false) }
    }

    fun onQuantityChange(value: String) {
        _formState.update { it.copy(quantity = value, quantityError = false) }
    }

    fun onUnitPriceChange(value: String) {
        _formState.update { it.copy(unitPrice = value, unitPriceError = false) }
    }

    fun onUnitCostChange(value: String) {
        _formState.update { it.copy(unitCost = value) }
    }

    fun onNotesChange(value: String) {
        _formState.update { it.copy(notes = value) }
    }

    fun onSoldAtChange(epochMillis: Long) {
        _formState.update { it.copy(soldAt = epochMillis) }
    }

    fun save() {
        val state = _formState.value
        val productName = state.productName.trim()
        val quantity = state.quantity.toIntOrNull()
        val unitPrice = state.unitPrice.toDoubleOrNull()

        val nameInvalid = productName.isBlank()
        val quantityInvalid = quantity == null || quantity <= 0
        val priceInvalid = unitPrice == null || unitPrice <= 0.0

        if (nameInvalid || quantityInvalid || priceInvalid) {
            _formState.update {
                it.copy(
                    productNameError = nameInvalid,
                    quantityError = quantityInvalid,
                    unitPriceError = priceInvalid
                )
            }
            return
        }

        viewModelScope.launch {
            _formState.update { it.copy(isSaving = true) }
            saleRepository.create(
                Sale(
                    storeId = storeId,
                    productId = state.selectedProduct?.id,
                    productName = productName,
                    quantity = quantity!!,
                    unitPrice = unitPrice!!,
                    unitCost = state.unitCost.toDoubleOrNull() ?: 0.0,
                    totalAmount = quantity * unitPrice,
                    notes = state.notes.trim(),
                    soldAt = state.soldAt
                )
            )
            _formState.update { it.copy(isSaving = false, isSuccess = true) }
        }
    }
}
