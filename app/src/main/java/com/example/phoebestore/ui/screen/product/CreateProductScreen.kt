package com.example.phoebestore.ui.screen.product

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

@Composable
fun CreateProductScreen(
    storeId: Long,
    productId: Long?,
    onProductSaved: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        val mode = if (productId == null) "Create" else "Edit"
        Text("$mode Product Screen — storeId: $storeId")
    }
}
