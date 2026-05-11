package com.example.phoebestore.ui.screen.product

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.phoebestore.domain.model.InventoryLog
import com.example.phoebestore.domain.model.Product
import com.example.phoebestore.domain.repository.InventoryLogRepository
import com.example.phoebestore.domain.repository.ProductRepository
import com.example.phoebestore.domain.repository.StoreRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CreateProductViewModel @Inject constructor(
    private val productRepository: ProductRepository,
    private val storeRepository: StoreRepository,
    private val inventoryLogRepository: InventoryLogRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val storeId: String = checkNotNull(savedStateHandle["storeId"])
    private val productId: String? = savedStateHandle["productId"]
    private var originalStock: Int? = null

    private val _formState = MutableStateFlow(CreateProductFormState())
    val formState: StateFlow<CreateProductFormState> = _formState.asStateFlow()

    val visiblePermissionDialogQueue = mutableStateListOf<String>()

    private val _events = Channel<CreateProductEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    init {
        viewModelScope.launch {
            storeRepository.getById(storeId)?.let { store ->
                _formState.update { it.copy(currency = store.currency) }
            }
            productId?.let { id ->
                productRepository.getById(id)?.let { product ->
                    originalStock = product.stock
                    _formState.update {
                        it.copy(
                            name = product.name,
                            description = product.description,
                            price = product.price.toString(),
                            costPrice = if (product.costPrice > 0.0) product.costPrice.toString() else "",
                            stock = product.stock.toString(),
                            imageUrl = product.imageUrl
                        )
                    }
                }
            }
        }
    }

    fun onNameChange(value: String) { _formState.update { it.copy(name = value, nameError = false) } }
    fun onDescriptionChange(value: String) { _formState.update { it.copy(description = value) } }
    fun onPriceChange(value: String) { _formState.update { it.copy(price = value.toDecimalInput(), priceError = false) } }
    fun onCostPriceChange(value: String) { _formState.update { it.copy(costPrice = value.toDecimalInput()) } }
    fun onStockChange(value: String) { _formState.update { it.copy(stock = value.toIntegerInput()) } }

    fun onPriceFocusLost() {
        _formState.update { state ->
            if (state.price.isEmpty()) return@update state
            state.price.toDoubleOrNull()?.let { state.copy(price = "%.2f".format(it)) } ?: state
        }
    }

    fun onCostPriceFocusLost() {
        _formState.update { state ->
            if (state.costPrice.isEmpty()) return@update state
            state.costPrice.toDoubleOrNull()?.let { state.copy(costPrice = "%.2f".format(it)) } ?: state
        }
    }

    fun onStockFocused() {
        _formState.update { state -> if (state.stock == "0") state.copy(stock = "") else state }
    }

    fun onImageCaptured(uri: String) { _formState.update { it.copy(imageUrl = uri) } }

    fun onPermissionResult(permission: String, isGranted: Boolean) {
        if (!isGranted && !visiblePermissionDialogQueue.contains(permission)) {
            visiblePermissionDialogQueue.add(permission)
        }
    }

    fun dismissDialog() { visiblePermissionDialogQueue.removeFirstOrNull() }

    fun saveProduct() {
        val state = _formState.value
        val price = state.price.toDoubleOrNull()
        if (state.name.isBlank()) { _formState.update { it.copy(nameError = true) }; return }
        if (price == null || price <= 0.0) { _formState.update { it.copy(priceError = true) }; return }
        viewModelScope.launch {
            _formState.update { it.copy(isLoading = true) }
            try {
                val product = buildProduct(state, price)
                if (productId == null) productRepository.create(product) else productRepository.update(product)
                logStockChangeIfNeeded(product)
                _events.send(CreateProductEvent.ProductSaved)
            } finally {
                _formState.update { it.copy(isLoading = false) }
            }
        }
    }

    private fun buildProduct(state: CreateProductFormState, price: Double): Product = Product(
        id = productId ?: "",
        storeId = storeId,
        name = state.name.trim(),
        description = state.description.trim(),
        price = price,
        costPrice = state.costPrice.toDoubleOrNull() ?: 0.0,
        stock = state.stock.toIntOrNull() ?: 0,
        imageUrl = state.imageUrl
    )

    private suspend fun logStockChangeIfNeeded(product: Product) {
        val prevStock = originalStock ?: return
        if (prevStock == product.stock || productId == null) return
        inventoryLogRepository.log(
            InventoryLog(
                storeId = storeId,
                productId = productId,
                productName = product.name,
                previousStock = prevStock,
                newStock = product.stock
            )
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
