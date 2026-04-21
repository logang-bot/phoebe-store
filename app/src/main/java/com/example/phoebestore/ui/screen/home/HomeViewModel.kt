package com.example.phoebestore.ui.screen.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.phoebestore.domain.repository.ProductRepository
import com.example.phoebestore.domain.repository.SaleRepository
import com.example.phoebestore.domain.repository.StoreRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    storeRepository: StoreRepository,
    saleRepository: SaleRepository,
    productRepository: ProductRepository
) : ViewModel() {

    private var autoNavHandled = false

    fun shouldAutoNav(): Boolean = !autoNavHandled

    fun markAutoNavHandled() {
        autoNavHandled = true
    }

    val uiState: StateFlow<HomeUiState> = storeRepository.getAll()
        .flatMapLatest { stores ->
            val lastStore = stores.firstOrNull()
            if (lastStore == null) {
                flowOf(HomeUiState(isInitialized = true))
            } else {
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
