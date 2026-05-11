package com.example.phoebestore.ui.screen.store

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.phoebestore.domain.model.Currency
import com.example.phoebestore.domain.model.Store
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
class CreateStoreViewModel @Inject constructor(
    private val storeRepository: StoreRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val storeId: String? = savedStateHandle["storeId"]

    private val _formState = MutableStateFlow(CreateStoreFormState())
    val formState: StateFlow<CreateStoreFormState> = _formState.asStateFlow()

    val visiblePermissionDialogQueue = mutableStateListOf<String>()

    private val _events = Channel<CreateStoreEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    init {
        storeId?.let { loadStore(it) }
    }

    private fun loadStore(id: String) {
        viewModelScope.launch {
            storeRepository.getById(id)?.let { store ->
                _formState.value = CreateStoreFormState(
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
        _formState.update { it.copy(name = value, nameError = false) }
    }

    fun onDescriptionChange(value: String) {
        _formState.update { it.copy(description = value) }
    }

    fun onCurrencyChange(value: Currency) {
        _formState.update { it.copy(currency = value) }
    }

    fun onLogoCaptured(uri: String) {
        _formState.update { it.copy(logoUrl = uri) }
    }

    fun onPhotoCaptured(uri: String) {
        _formState.update { it.copy(photoUrl = uri) }
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
        val state = _formState.value
        if (state.name.isBlank()) {
            _formState.update { it.copy(nameError = true) }
            return
        }
        viewModelScope.launch {
            _formState.update { it.copy(isLoading = true) }
            try {
                val store = Store(
                    id = storeId ?: "",
                    name = state.name.trim(),
                    description = state.description.trim(),
                    currency = state.currency,
                    logoUrl = state.logoUrl,
                    photoUrl = state.photoUrl
                )
                if (storeId == null) storeRepository.create(store) else storeRepository.update(store)
                _events.send(CreateStoreEvent.StoreSaved)
            } finally {
                _formState.update { it.copy(isLoading = false) }
            }
        }
    }
}

