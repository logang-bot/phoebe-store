package com.example.phoebestore.ui.screen.sale

import com.example.phoebestore.domain.model.Currency
import com.example.phoebestore.domain.model.Product
import com.example.phoebestore.domain.model.ProfitOutcome

data class RecordSaleFormState(
    val products: List<Product> = emptyList(),
    val selectedProduct: Product? = null,
    val isCustomProduct: Boolean = false,
    val isSearchSelected: Boolean = false,
    val searchQuery: String = "",
    val filteredProducts: List<Product> = emptyList(),
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
    val showConfirmDialog: Boolean = false,
    val productNameError: Boolean = false,
    val quantityError: Boolean = false,
    val unitPriceError: Boolean = false,
    // Derived display fields — computed by the ViewModel, never set by callers
    val formattedSoldAt: String = "",
    val formattedTotalAmount: String = "",
    val formattedUnitPrice: String = "",
    val formattedUnitCost: String = "",
    val formattedProfitDelta: String = "",
    val formattedAbsCurrentProfit: String = "",
    val showModificationInfo: Boolean = false,
    val canSave: Boolean = false
)
