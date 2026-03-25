package com.example.phoebestore.ui.screen.store

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.phoebestore.domain.model.Currency
import com.example.phoebestore.domain.model.Store
import com.example.phoebestore.domain.repository.StoreRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CreateStoreViewModel @Inject constructor(
    private val storeRepository: StoreRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val storeId: Long? = savedStateHandle["storeId"]

    var formState by mutableStateOf(CreateStoreFormState())
        private set

    val visiblePermissionDialogQueue = mutableStateListOf<String>()

    private val _events = Channel<CreateStoreEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    init {
        storeId?.let { loadStore(it) }
    }

    private fun loadStore(id: Long) {
        viewModelScope.launch {
            storeRepository.getById(id)?.let { store ->
                formState = CreateStoreFormState(
                    name = store.name,
                    description = store.description,
                    currency = store.currency,
                    logoUrl = store.logoUrl,
                    photoUrl = store.photoUrl
                )
            }
        }
    }

    fun onNameChange(value: String) {
        formState = formState.copy(name = value, nameError = false)
    }

    fun onDescriptionChange(value: String) {
        formState = formState.copy(description = value)
    }

    fun onCurrencyChange(value: Currency) {
        formState = formState.copy(currency = value)
    }

    fun onLogoCaptured(uri: String) {
        formState = formState.copy(logoUrl = uri)
    }

    fun onPhotoCaptured(uri: String) {
        formState = formState.copy(photoUrl = uri)
    }

    fun onPermissionResult(permission: String, isGranted: Boolean) {
        if (!isGranted && !visiblePermissionDialogQueue.contains(permission)) {
            visiblePermissionDialogQueue.add(permission)
        }
    }

    fun dismissDialog() {
        visiblePermissionDialogQueue.removeFirstOrNull()
    }

    fun saveStore() {
        if (formState.name.isBlank()) {
            formState = formState.copy(nameError = true)
            return
        }
        viewModelScope.launch {
            formState = formState.copy(isLoading = true)
            try {
                val store = Store(
                    id = storeId ?: 0L,
                    name = formState.name.trim(),
                    description = formState.description.trim(),
                    currency = formState.currency,
                    logoUrl = formState.logoUrl,
                    photoUrl = formState.photoUrl
                )
                if (storeId == null) storeRepository.create(store) else storeRepository.update(store)
                _events.send(CreateStoreEvent.StoreSaved)
            } finally {
                formState = formState.copy(isLoading = false)
            }
        }
    }
}

data class CreateStoreFormState(
    val name: String = "",
    val description: String = "",
    val currency: Currency = Currency.USD,
    val logoUrl: String = "",
    val photoUrl: String = "",
    val isLoading: Boolean = false,
    val nameError: Boolean = false
)

sealed class CreateStoreEvent {
    data object StoreSaved : CreateStoreEvent()
}
