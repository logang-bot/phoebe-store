package com.example.phoebestore.ui.screen.store

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

@Composable
fun StoreDetailScreen(
    storeId: Long,
    onNavigateToEditStore: (storeId: Long) -> Unit,
    onNavigateToCreateProduct: (storeId: Long) -> Unit,
    onNavigateToEditProduct: (storeId: Long, productId: Long) -> Unit,
    onNavigateToRecordSale: (storeId: Long) -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text("Store Detail Screen — storeId: $storeId")
    }
}
