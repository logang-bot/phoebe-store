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
import com.example.phoebestore.domain.repository.StoreRepository
import com.example.phoebestore.domain.repository.UserSettingsRepository
import com.example.phoebestore.domain.usecase.RecordSaleUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import kotlin.math.abs

@HiltViewModel
class RecordSaleViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val recordSaleUseCase: RecordSaleUseCase,
    private val productRepository: ProductRepository,
    private val storeRepository: StoreRepository,
    private val userSettingsRepository: UserSettingsRepository
) : ViewModel() {

    private val storeId: Long = checkNotNull(savedStateHandle["storeId"])

    private val dateFormat = SimpleDateFormat("MMM dd, yyyy - h:mm a", Locale.getDefault())

    private val _formState = MutableStateFlow(
        RecordSaleFormState().withComputedDisplayFields()
    )
    val formState: StateFlow<RecordSaleFormState> = _formState.asStateFlow()

    private var searchJob: Job? = null

    init {
        viewModelScope.launch {
            storeRepository.markAccessed(storeId)
            userSettingsRepository.setLastAccessedStore(storeId)
            storeRepository.getById(storeId)?.let { store ->
                _formState.update { it.copy(currency = store.currency) }
            }
            productRepository.getByStore(storeId).collect { products ->
                _formState.update { it.copy(products = products) }
            }
        }
    }

    fun onProductSelected(product: Product?) {
        searchJob?.cancel()
        _formState.update { state ->
            val total = (state.quantity.toIntOrNull() ?: 0) * (product?.price ?: 0.0)
            val currentProfit = (product?.price ?: 0.0) - (product?.costPrice ?: 0.0)
            state.copy(
                selectedProduct = product,
                isCustomProduct = product == null,
                isSearchSelected = false,
                isSearchExpanded = false,
                searchQuery = "",
                filteredProducts = emptyList(),
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
            ).withComputedDisplayFields()
        }
    }

    fun onCustomProductSelected() {
        searchJob?.cancel()
        _formState.update {
            it.copy(
                selectedProduct = null,
                isCustomProduct = true,
                isSearchSelected = false,
                isSearchExpanded = false,
                searchQuery = "",
                filteredProducts = emptyList(),
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
            ).withComputedDisplayFields()
        }
    }

    fun onSearchSelected() {
        _formState.update {
            it.copy(
                selectedProduct = null,
                isCustomProduct = false,
                isSearchSelected = true,
                isSearchExpanded = true,
                searchQuery = "",
                filteredProducts = it.products,
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
            ).withComputedDisplayFields()
        }
    }

    fun onSearchQueryChange(query: String) {
        _formState.update { it.copy(searchQuery = query) }
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            delay(1_000)
            _formState.update { state ->
                val filtered = if (query.isBlank()) state.products
                else state.products.filter { it.name.contains(query, ignoreCase = true) }
                state.copy(filteredProducts = filtered)
            }
        }
    }

    fun onSearchConfirmed() {
        searchJob?.cancel()
        _formState.update { state ->
            state.copy(
                isSearchSelected = false,
                isSearchExpanded = false,
                filteredProducts = emptyList()
            )
        }
    }

    fun onQuantityIncrement() {
        _formState.update { state ->
            val newQty = (state.quantity.toIntOrNull() ?: 0) + 1
            val total = newQty * (state.unitPrice.toDoubleOrNull() ?: 0.0)
            state.copy(quantity = newQty.toString(), quantityError = false, totalAmount = total)
                .withComputedDisplayFields()
        }
    }

    fun onQuantityDecrement() {
        _formState.update { state ->
            val current = state.quantity.toIntOrNull() ?: 0
            if (current <= 1) return@update state
            val newQty = current - 1
            val total = newQty * (state.unitPrice.toDoubleOrNull() ?: 0.0)
            state.copy(quantity = newQty.toString(), quantityError = false, totalAmount = total)
                .withComputedDisplayFields()
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
                .withComputedDisplayFields()
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
            ).withComputedDisplayFields()
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
            ).withComputedDisplayFields()
        }
    }

    fun onUnitPriceFocusLost() {
        _formState.update { state ->
            if (state.unitPrice.isEmpty()) return@update state
            state.unitPrice.toDoubleOrNull()?.let { v ->
                state.copy(unitPrice = "%.2f".format(v)).withComputedDisplayFields()
            } ?: state
        }
    }

    fun onUnitCostFocusLost() {
        _formState.update { state ->
            if (state.unitCost.isEmpty()) return@update state
            state.unitCost.toDoubleOrNull()?.let { v ->
                state.copy(unitCost = "%.2f".format(v)).withComputedDisplayFields()
            } ?: state
        }
    }

    fun onNotesChange(value: String) {
        _formState.update { it.copy(notes = value) }
    }

    fun onOnCreditChange(value: Boolean) {
        _formState.update { it.copy(isOnCredit = value, creditPersonName = "", creditPersonNameError = false).withComputedDisplayFields() }
    }

    fun onCreditPersonNameChange(value: String) {
        _formState.update { it.copy(creditPersonName = value, creditPersonNameError = false).withComputedDisplayFields() }
    }

    fun onSoldAtChange(epochMillis: Long) {
        _formState.update { it.copy(soldAt = epochMillis).withComputedDisplayFields() }
    }

    fun onSaveClicked() {
        _formState.update { it.copy(showConfirmDialog = true) }
    }

    fun onDismissConfirmDialog() {
        _formState.update { it.copy(showConfirmDialog = false) }
    }

    fun confirmSave() {
        val state = _formState.value
        val productName = state.productName.trim()
        val quantity = state.quantity.toIntOrNull() ?: return
        val unitPrice = state.unitPrice.toDoubleOrNull() ?: return

        viewModelScope.launch {
            _formState.update { it.copy(showConfirmDialog = false, isSaving = true) }
            val saleType = if (state.isPriceModified || state.isCostModified) SaleType.MODIFIED else SaleType.STANDARD
            try {
                recordSaleUseCase(
                    sale = Sale(
                        storeId = storeId,
                        productId = state.selectedProduct?.id,
                        productName = productName,
                        quantity = quantity,
                        unitPrice = unitPrice,
                        unitCost = state.unitCost.toDoubleOrNull() ?: 0.0,
                        totalAmount = state.totalAmount,
                        saleType = saleType,
                        profitOutcome = state.profitOutcome,
                        notes = state.notes.trim(),
                        onCredit = state.isOnCredit,
                        creditPersonName = state.creditPersonName.trim(),
                        soldAt = state.soldAt
                    ),
                    selectedProduct = state.selectedProduct,
                    isCustomProduct = state.isCustomProduct
                )
                _formState.update {
                    it.copy(
                        isSaving = false,
                        saleResult = SaleResult.Success(
                            productName = productName,
                            quantity = quantity,
                            formattedTotal = "${state.currency.name} ${"%.2f".format(state.totalAmount)}"
                        )
                    )
                }
            } catch (e: Exception) {
                _formState.update { it.copy(isSaving = false, saleResult = SaleResult.Error) }
            }
        }
    }

    fun clearSaleResult() {
        _formState.update { it.copy(saleResult = null) }
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

    private fun RecordSaleFormState.withComputedDisplayFields(): RecordSaleFormState {
        val qty = quantity.toIntOrNull() ?: 0
        val exceedsStock = selectedProduct != null && qty > selectedProduct.stock
        return copy(
            formattedSoldAt = dateFormat.format(Date(soldAt)),
            formattedTotalAmount = if (totalAmount > 0.0) "%.2f".format(totalAmount) else "",
            formattedUnitPrice = "%.2f".format(unitPrice.toDoubleOrNull() ?: 0.0),
            formattedUnitCost = "%.2f".format(unitCost.toDoubleOrNull() ?: 0.0),
            formattedProfitDelta = "%.2f".format(abs(profitDelta)),
            formattedAbsCurrentProfit = "%.2f".format(abs(currentProfit)),
            showModificationInfo = selectedProduct != null && (isPriceModified || isCostModified),
            quantityExceedsStock = exceedsStock,
            canSave = (selectedProduct != null || productName.isNotBlank()) &&
                    (qty > 0) &&
                    (unitPrice.toDoubleOrNull()?.let { it > 0.0 } == true) &&
                    (!isOnCredit || creditPersonName.isNotBlank())
        )
    }

    private fun String.toDecimalInput(): String {
        val filtered = filter { it.isDigit() || it == '.' }
        val dotIndex = filtered.indexOf('.')
        return if (dotIndex == -1) filtered
        else filtered.substring(0, dotIndex + 1) + filtered.substring(dotIndex + 1).filter { it.isDigit() }.take(2)
    }

    private fun String.toIntegerInput(): String = filter { it.isDigit() }
}
