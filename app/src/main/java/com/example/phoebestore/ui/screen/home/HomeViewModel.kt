package com.example.phoebestore.ui.screen.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.phoebestore.data.sync.SyncManager
import com.example.phoebestore.domain.repository.ProductRepository
import com.example.phoebestore.domain.repository.SaleRepository
import com.example.phoebestore.domain.repository.StoreRepository
import com.example.phoebestore.domain.repository.UserSettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    storeRepository: StoreRepository,
    saleRepository: SaleRepository,
    productRepository: ProductRepository,
    userSettingsRepository: UserSettingsRepository,
    syncManager: SyncManager
) : ViewModel() {

    private var autoNavHandled = false

    fun shouldAutoNav(): Boolean = !autoNavHandled

    fun markAutoNavHandled() {
        autoNavHandled = true
    }

    val isSyncing: StateFlow<Boolean> = syncManager.isSyncing

    val uiState: StateFlow<HomeUiState> = userSettingsRepository.lastAccessedStoreId
        .flatMapLatest { storeId ->
            if (storeId == null) return@flatMapLatest flowOf(HomeUiState(isInitialized = true))
            storeRepository.getAll()
                .map { stores -> stores.firstOrNull { it.id == storeId } }
                .flatMapLatest { lastStore ->
                    if (lastStore == null) return@flatMapLatest flowOf(HomeUiState(isInitialized = true))
                    combine(
                        saleRepository.getByStore(lastStore.id),
                        productRepository.getByStore(lastStore.id)
                    ) { sales, products ->
                        val lowestStock = products.filter { it.stock <= 5 }.sortedBy { it.stock }.take(3)
                        HomeUiState(
                            lastStore = lastStore,
                            totalSales = sales.size,
                            formattedRevenue = "%.2f".format(sales.sumOf { it.totalAmount }),
                            formattedProfit = "%.2f".format(sales.sumOf { it.quantity * (it.unitPrice - it.unitCost) }),
                            totalStock = products.sumOf { it.stock },
                            lowStockAlerts = lowestStock.takeIf { it.isNotEmpty() }
                                                 ?.joinToString(", ") { it.name },
                            hasProducts = products.isNotEmpty(),
                            isInitialized = true
                        )
                    }
                }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = HomeUiState()
        )
}
