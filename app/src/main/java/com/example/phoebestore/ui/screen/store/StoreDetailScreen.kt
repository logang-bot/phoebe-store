package com.example.phoebestore.ui.screen.store

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import com.example.phoebestore.ui.common.ThemedCard
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.example.phoebestore.R
import com.example.phoebestore.domain.model.Currency
import com.example.phoebestore.domain.model.Store
import com.example.phoebestore.ui.theme.PhoebeStoreTheme

@Composable
fun StoreDetailScreen(
    storeId: Long,
    onNavigateToEditStore: (storeId: Long) -> Unit,
    onNavigateToProductList: (storeId: Long) -> Unit,
    onNavigateToSalesList: (storeId: Long) -> Unit,
    onNavigateToCreateSale: () -> Unit = {},
    viewModel: StoreDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    StoreDetailScreenContent(
        store = uiState.store,
        totalSales = uiState.totalSales,
        formattedRevenue = uiState.formattedRevenue,
        formattedProfit = uiState.formattedProfit,
        onNavigateToEditStore = { onNavigateToEditStore(storeId) },
        onNavigateToProductList = { onNavigateToProductList(storeId) },
        onNavigateToSalesList = { onNavigateToSalesList(storeId) },
        onCreateSale = onNavigateToCreateSale
    )
}

@Composable
private fun StoreDetailScreenContent(
    store: Store?,
    totalSales: Int,
    formattedRevenue: String,
    formattedProfit: String,
    onNavigateToEditStore: () -> Unit,
    onNavigateToProductList: () -> Unit,
    onNavigateToSalesList: () -> Unit,
    onCreateSale: () -> Unit = {}
) {
    Scaffold(
        containerColor = MaterialTheme.colorScheme.surfaceContainer,
        bottomBar = {
            Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
                Button(
                    onClick = onCreateSale,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(stringResource(R.string.store_detail_create_sale_button))
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(innerPadding)
        ) {
            StoreDetailHeader(
                store = store,
                onNavigateToEditStore = onNavigateToEditStore
            )

            Column(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp)
            ) {
                if (store != null && store.description.isNotBlank()) {
                    Text(
                        text = store.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }

                if (store != null) {
                    Text(
                        text = stringResource(R.string.home_currency_format, store.currency.name),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }

                Button(
                    onClick = onNavigateToSalesList,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.secondary,
                        contentColor = MaterialTheme.colorScheme.onSecondary
                    )
                ) {
                    Text(stringResource(R.string.store_detail_sales_button))
                }

                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = onNavigateToProductList,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.secondary,
                        contentColor = MaterialTheme.colorScheme.onSecondary
                    )
                ) {
                    Text(stringResource(R.string.store_detail_products_button))
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Overview metrics card
                OverviewCard(
                    totalSales = totalSales,
                    formattedRevenue = formattedRevenue,
                    formattedProfit = formattedProfit
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Analytics placeholder card
                AnalyticsPlaceholderCard()
            }
        }
    }
}

@Composable
private fun StoreDetailHeader(
    store: Store?,
    onNavigateToEditStore: () -> Unit
) {
    val photoHeight = 200.dp
    val logoSize = 80.dp
    val logoOffset = logoSize / 2

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(photoHeight + logoOffset),
        contentAlignment = Alignment.TopCenter
    ) {
        // Cover photo
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(photoHeight)
                .background(MaterialTheme.colorScheme.surfaceVariant)
        ) {
            if (store?.photoUrl?.isNotBlank() == true) {
                AsyncImage(
                    model = store.photoUrl,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }

            // Edit button at top-left of photo
            IconButton(
                onClick = onNavigateToEditStore,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(8.dp)
                    .background(
                        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.85f),
                        shape = CircleShape
                    )
            ) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = stringResource(R.string.store_detail_edit_button),
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
        }

        // Logo circle overlapping photo boundary
        Box(
            modifier = Modifier
                .size(logoSize)
                .offset(y = photoHeight - logoOffset)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceContainer)
                .border(2.dp, MaterialTheme.colorScheme.outline, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            if (store?.logoUrl?.isNotBlank() == true) {
                AsyncImage(
                    model = store.logoUrl,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(36.dp)
                )
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp, bottom = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = store?.name ?: "",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun OverviewCard(
    totalSales: Int,
    formattedRevenue: String,
    formattedProfit: String
) {
    val placeholder = stringResource(R.string.home_overview_placeholder_value)
    val rows = listOf(
        stringResource(R.string.home_overview_total_sales) to "$totalSales",
        stringResource(R.string.home_overview_revenue) to formattedRevenue,
        stringResource(R.string.home_overview_profit) to formattedProfit,
        stringResource(R.string.home_overview_products_in_stock) to placeholder,
        stringResource(R.string.home_overview_low_stock_alerts) to placeholder
    )
    ThemedCard(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = stringResource(R.string.home_overview_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            Spacer(modifier = Modifier.height(12.dp))
            rows.forEachIndexed { index, (label, value) ->
                if (index > 0) Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = value,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}

@Composable
private fun AnalyticsPlaceholderCard() {
    ThemedCard(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = stringResource(R.string.store_detail_analytics_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = stringResource(R.string.store_detail_analytics_coming_soon),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }
    }
}

private val previewStore = Store(
    id = 1L,
    name = "Phoebe's Boutique",
    description = "Fashion & Accessories",
    currency = Currency.USD
)

@Preview(showBackground = true)
@Composable
private fun StoreDetailScreenLightPreview() {
    PhoebeStoreTheme {
        StoreDetailScreenContent(
            store = previewStore,
            totalSales = 12,
            formattedRevenue = "350.00",
            formattedProfit = "120.00",
            onNavigateToEditStore = {},
            onNavigateToProductList = {},
            onNavigateToSalesList = {}
        )
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun StoreDetailScreenDarkPreview() {
    PhoebeStoreTheme {
        StoreDetailScreenContent(
            store = previewStore,
            totalSales = 12,
            formattedRevenue = "350.00",
            formattedProfit = "120.00",
            onNavigateToEditStore = {},
            onNavigateToProductList = {},
            onNavigateToSalesList = {}
        )
    }
}
