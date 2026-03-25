package com.example.phoebestore.ui.screen.store

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.phoebestore.R
import com.example.phoebestore.domain.model.Currency
import com.example.phoebestore.domain.model.Store
import com.example.phoebestore.ui.theme.PhoebeStoreTheme

@Composable
fun StoreListScreen(
    onNavigateToStoreDetail: (storeId: Long) -> Unit,
    onNavigateToCreateStore: () -> Unit,
    viewModel: StoreListViewModel = hiltViewModel()
) {
    val stores by viewModel.stores.collectAsStateWithLifecycle()

    StoreListScreenContent(
        stores = stores,
        onNavigateToStoreDetail = onNavigateToStoreDetail,
        onNavigateToCreateStore = onNavigateToCreateStore
    )
}

@Composable
private fun StoreListScreenContent(
    stores: List<Store>,
    onNavigateToStoreDetail: (storeId: Long) -> Unit,
    onNavigateToCreateStore: () -> Unit
) {
    Scaffold(
        containerColor = MaterialTheme.colorScheme.surfaceContainer
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Text(
                text = stringResource(R.string.store_list_title),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 24.dp)
            )

            if (stores.isEmpty()) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = stringResource(R.string.store_list_empty_title),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = stringResource(R.string.store_list_empty_subtitle),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 32.dp)
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(stores, key = { it.id }) { store ->
                        StoreCard(
                            store = store,
                            onClick = { onNavigateToStoreDetail(store.id) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .animateItem()
                        )
                    }
                }
            }

            Button(
                onClick = onNavigateToCreateStore,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(stringResource(R.string.store_list_create_button))
            }
        }
    }
}

private val previewStores = listOf(
    Store(id = 1L, name = "Phoebe's Boutique", description = "Fashion & Accessories", currency = Currency.BOB),
    Store(id = 2L, name = "Tech Corner", description = "Electronics & Gadgets", currency = Currency.BOB),
    Store(id = 3L, name = "The Green Market", currency = Currency.USD)
)

@Preview(showBackground = true)
@Composable
private fun StoreListScreenLightPreview() {
    PhoebeStoreTheme {
        StoreListScreenContent(
            stores = previewStores,
            onNavigateToStoreDetail = {},
            onNavigateToCreateStore = {}
        )
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun StoreListScreenDarkPreview() {
    PhoebeStoreTheme {
        StoreListScreenContent(
            stores = previewStores,
            onNavigateToStoreDetail = {},
            onNavigateToCreateStore = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun StoreListScreenEmptyLightPreview() {
    PhoebeStoreTheme {
        StoreListScreenContent(
            stores = emptyList(),
            onNavigateToStoreDetail = {},
            onNavigateToCreateStore = {}
        )
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun StoreListScreenEmptyDarkPreview() {
    PhoebeStoreTheme {
        StoreListScreenContent(
            stores = emptyList(),
            onNavigateToStoreDetail = {},
            onNavigateToCreateStore = {}
        )
    }
}
