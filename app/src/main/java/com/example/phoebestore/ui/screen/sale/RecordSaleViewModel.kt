package com.example.phoebestore.ui.screen.sale

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.phoebestore.domain.model.Currency
import com.example.phoebestore.domain.model.Product
import com.example.phoebestore.domain.model.ProfitOutcome
import com.example.phoebestore.domain.model.Sale
import com.example.phoebestore.domain.model.SaleType
import com.example.phoebestore.domain.repository.ProductRepository
import com.example.phoebestore.domain.repository.SaleRepository
import com.example.phoebestore.domain.repository.StoreRepository
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
    val currency: Currency = Currency.USD,
    val totalAmount: Double = 0.0,
    val isPriceModified: Boolean = false,
    val isCostModified: Boolean = false,
    val profitOutcome: ProfitOutcome = ProfitOutcome.NORMAL_PROFIT,
    val profitDelta: Double = 0.0,
    val currentProfit: Double = 0.0,
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
    private val productRepository: ProductRepository,
    private val storeRepository: StoreRepository
) : ViewModel() {

    private val storeId: Long = checkNotNull(savedStateHandle["storeId"])

    private val _formState = MutableStateFlow(RecordSaleFormState())
    val formState: StateFlow<RecordSaleFormState> = _formState.asStateFlow()

    init {
        viewModelScope.launch {
            storeRepository.getById(storeId)?.let { store ->
                _formState.update { it.copy(currency = store.currency) }
            }
            productRepository.getByStore(storeId).collect { products ->
                _formState.update { it.copy(products = products) }
            }
        }
    }

    fun onProductSelected(product: Product?) {
        _formState.update { state ->
            val total = (state.quantity.toIntOrNull() ?: 0) * (product?.price ?: 0.0)
            val currentProfit = (product?.price ?: 0.0) - (product?.costPrice ?: 0.0)
            state.copy(
                selectedProduct = product,
                isCustomProduct = product == null,
                productName = product?.name ?: "",
                unitPrice = product?.price?.toString() ?: "",
                unitCost = product?.costPrice?.toString() ?: "",
                totalAmount = total,
                isPriceModified = false,
                isCostModified = false,
                profitOutcome = ProfitOutcome.NORMAL_PROFIT,
                profitDelta = 0.0,
                currentProfit = currentProfit,
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
                totalAmount = 0.0,
                isPriceModified = false,
                isCostModified = false,
                profitOutcome = ProfitOutcome.NORMAL_PROFIT,
                profitDelta = 0.0,
                currentProfit = 0.0,
                productNameError = false,
                unitPriceError = false
            )
        }
    }

    fun onProductNameChange(name: String) {
        _formState.update { it.copy(productName = name, productNameError = false) }
    }

    fun onQuantityChange(value: String) {
        _formState.update { state ->
            val input = value.toIntegerInput()
            val total = (input.toIntOrNull() ?: 0) * (state.unitPrice.toDoubleOrNull() ?: 0.0)
            state.copy(quantity = input, quantityError = false, totalAmount = total)
        }
    }

    fun onUnitPriceChange(value: String) {
        _formState.update { state ->
            val input = value.toDecimalInput()
            val price = input.toDoubleOrNull() ?: 0.0
            val cost = state.unitCost.toDoubleOrNull() ?: 0.0
            val isPriceModified = state.selectedProduct != null && price != state.selectedProduct.price
            val outcome = computeProfitOutcome(state.selectedProduct, price, cost, isPriceModified, state.isCostModified)
            val currentProfit = price - cost
            val profitDelta = currentProfit - (state.selectedProduct?.let { it.price - it.costPrice } ?: 0.0)
            val total = (state.quantity.toIntOrNull() ?: 0) * price
            state.copy(
                unitPrice = input,
                unitPriceError = false,
                isPriceModified = isPriceModified,
                profitOutcome = outcome,
                currentProfit = currentProfit,
                profitDelta = profitDelta,
                totalAmount = total
            )
        }
    }

    fun onUnitCostChange(value: String) {
        _formState.update { state ->
            val input = value.toDecimalInput()
            val cost = input.toDoubleOrNull() ?: 0.0
            val price = state.unitPrice.toDoubleOrNull() ?: 0.0
            val isCostModified = state.selectedProduct != null && cost != state.selectedProduct.costPrice
            val outcome = computeProfitOutcome(state.selectedProduct, price, cost, state.isPriceModified, isCostModified)
            val currentProfit = price - cost
            val profitDelta = currentProfit - (state.selectedProduct?.let { it.price - it.costPrice } ?: 0.0)
            state.copy(
                unitCost = input,
                isCostModified = isCostModified,
                profitOutcome = outcome,
                currentProfit = currentProfit,
                profitDelta = profitDelta
            )
        }
    }

    private fun computeProfitOutcome(
        product: Product?,
        unitPrice: Double?,
        unitCost: Double?,
        isPriceModified: Boolean,
        isCostModified: Boolean
    ): ProfitOutcome {
        if (product == null || (!isPriceModified && !isCostModified)) return ProfitOutcome.NORMAL_PROFIT
        val standardProfit = product.price - product.costPrice
        val currentProfit = (unitPrice ?: 0.0) - (unitCost ?: 0.0)
        val delta = currentProfit - standardProfit
        return when {
            currentProfit <= 0.0 -> ProfitOutcome.LOSS
            delta > 0.0 -> ProfitOutcome.EXTRA_PROFIT
            delta < 0.0 -> ProfitOutcome.SMALLER_PROFIT
            else -> ProfitOutcome.NORMAL_PROFIT
        }
    }

    private fun String.toDecimalInput(): String {
        val filtered = filter { it.isDigit() || it == '.' }
        val dotIndex = filtered.indexOf('.')
        return if (dotIndex == -1) filtered
        else filtered.substring(0, dotIndex + 1) + filtered.substring(dotIndex + 1).filter { it.isDigit() }.take(2)
    }

    private fun String.toIntegerInput(): String = filter { it.isDigit() }

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
            val saleType = if (state.isPriceModified || state.isCostModified) SaleType.MODIFIED else SaleType.STANDARD
            saleRepository.create(
                Sale(
                    storeId = storeId,
                    productId = state.selectedProduct?.id,
                    productName = productName,
                    quantity = quantity!!,
                    unitPrice = unitPrice!!,
                    unitCost = state.unitCost.toDoubleOrNull() ?: 0.0,
                    totalAmount = state.totalAmount,
                    saleType = saleType,
                    profitOutcome = state.profitOutcome,
                    notes = state.notes.trim(),
                    soldAt = state.soldAt
                )
            )
            _formState.update { it.copy(isSaving = false, isSuccess = true) }
        }
    }
}
