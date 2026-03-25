package com.example.phoebestore.ui.screen.product

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.phoebestore.domain.model.Product
import com.example.phoebestore.domain.repository.ProductRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CreateProductViewModel @Inject constructor(
    private val productRepository: ProductRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val storeId: Long = checkNotNull(savedStateHandle["storeId"])
    private val productId: Long? = savedStateHandle["productId"]

    var formState by mutableStateOf(CreateProductFormState())
        private set

    val visiblePermissionDialogQueue = mutableStateListOf<String>()

    private val _events = Channel<CreateProductEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    init {
        productId?.let { loadProduct(it) }
    }

    private fun loadProduct(id: Long) {
        viewModelScope.launch {
            productRepository.getById(id)?.let { product ->
                formState = CreateProductFormState(
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

    fun onNameChange(value: String) { formState = formState.copy(name = value, nameError = false) }
    fun onDescriptionChange(value: String) { formState = formState.copy(description = value) }
    fun onPriceChange(value: String) { formState = formState.copy(price = value, priceError = false) }
    fun onCostPriceChange(value: String) { formState = formState.copy(costPrice = value) }
    fun onStockChange(value: String) { formState = formState.copy(stock = value) }
    fun onImageCaptured(uri: String) { formState = formState.copy(imageUrl = uri) }

    fun onPermissionResult(permission: String, isGranted: Boolean) {
        if (!isGranted && !visiblePermissionDialogQueue.contains(permission)) {
            visiblePermissionDialogQueue.add(permission)
        }
    }

    fun dismissDialog() { visiblePermissionDialogQueue.removeFirstOrNull() }

    fun saveProduct() {
        val price = formState.price.toDoubleOrNull()
        if (formState.name.isBlank()) {
            formState = formState.copy(nameError = true)
            return
        }
        if (price == null || price <= 0.0) {
            formState = formState.copy(priceError = true)
            return
        }
        viewModelScope.launch {
            formState = formState.copy(isLoading = true)
            try {
                val product = Product(
                    id = productId ?: 0L,
                    storeId = storeId,
                    name = formState.name.trim(),
                    description = formState.description.trim(),
                    price = price,
                    costPrice = formState.costPrice.toDoubleOrNull() ?: 0.0,
                    stock = formState.stock.toIntOrNull() ?: 0,
                    imageUrl = formState.imageUrl
                )
                if (productId == null) productRepository.create(product) else productRepository.update(product)
                _events.send(CreateProductEvent.ProductSaved)
            } finally {
                formState = formState.copy(isLoading = false)
            }
        }
    }
}

data class CreateProductFormState(
    val name: String = "",
    val description: String = "",
    val price: String = "",
    val costPrice: String = "",
    val stock: String = "0",
    val imageUrl: String = "",
    val isLoading: Boolean = false,
    val nameError: Boolean = false,
    val priceError: Boolean = false
)

sealed class CreateProductEvent {
    data object ProductSaved : CreateProductEvent()
}
