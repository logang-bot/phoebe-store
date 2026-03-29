package com.example.phoebestore.ui.screen.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.phoebestore.domain.repository.SaleRepository
import com.example.phoebestore.domain.repository.StoreRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    storeRepository: StoreRepository,
    saleRepository: SaleRepository
) : ViewModel() {

    val uiState: StateFlow<HomeUiState> = storeRepository.getAll()
        .flatMapLatest { stores ->
            val lastStore = stores.firstOrNull()
            if (lastStore == null) {
                flowOf(HomeUiState())
            } else {
                saleRepository.getByStore(lastStore.id).map { sales ->
                    HomeUiState(
                        lastStore = lastStore,
                        totalSales = sales.size,
                        formattedRevenue = "%.2f".format(sales.sumOf { it.totalAmount }),
                        formattedProfit = "%.2f".format(sales.sumOf { it.quantity * (it.unitPrice - it.unitCost) })
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
