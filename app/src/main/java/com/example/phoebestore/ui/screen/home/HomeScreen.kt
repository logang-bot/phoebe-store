package com.example.phoebestore.ui.screen.home

import android.content.res.Configuration
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringArrayResource
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
import com.example.phoebestore.ui.common.StoreCard
import com.example.phoebestore.ui.theme.PhoebeStoreTheme

@Composable
fun HomeScreen(
    onNavigateToStoreList: () -> Unit,
    onNavigateToStoreDetail: (storeId: Long) -> Unit,
    onNavigateToCreateSale: (storeId: Long) -> Unit = {},
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val welcomeMessages = stringArrayResource(R.array.home_welcome_messages)
    val welcomeMessage = remember { welcomeMessages.random() }

    HomeScreenContent(
        lastStore = uiState.lastStore,
        totalSales = uiState.totalSales,
        formattedRevenue = uiState.formattedRevenue,
        formattedProfit = uiState.formattedProfit,
        welcomeMessage = welcomeMessage,
        onNavigateToStoreList = onNavigateToStoreList,
        onNavigateToStoreDetail = onNavigateToStoreDetail,
        onNavigateToCreateSale = { uiState.lastStore?.let { onNavigateToCreateSale(it.id) } }
    )
}

@Composable
private fun HomeScreenContent(
    lastStore: Store?,
    totalSales: Int,
    formattedRevenue: String,
    formattedProfit: String,
    welcomeMessage: String,
    onNavigateToStoreList: () -> Unit,
    onNavigateToStoreDetail: (storeId: Long) -> Unit,
    onNavigateToCreateSale: () -> Unit = {}
) {
    Scaffold(
        containerColor = MaterialTheme.colorScheme.surfaceContainer
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(innerPadding)
                .padding(horizontal = 16.dp, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = welcomeMessage,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.fillMaxWidth().padding(vertical = 35.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            StoreCard(
                store = lastStore,
                flatBottom = lastStore != null,
                onClick = { lastStore?.let { onNavigateToStoreDetail(it.id) } },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp)
            )

            if (lastStore != null) {
                Button(
                    onClick = onNavigateToCreateSale,
                    shape = RoundedCornerShape(
                        topStart = 0.dp,
                        topEnd = 0.dp,
                        bottomStart = 12.dp,
                        bottomEnd = 12.dp
                    ),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.tertiary,
                        contentColor = MaterialTheme.colorScheme.onTertiary
                    ),
                    contentPadding = PaddingValues(vertical = 16.dp, horizontal = 24.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp)
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_point_of_sale),
                        contentDescription = null,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = stringResource(R.string.store_detail_create_sale_button),
                        style = MaterialTheme.typography.titleLarge
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = onNavigateToStoreList,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_storefront),
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(stringResource(R.string.home_view_all_stores))
            }

            Spacer(modifier = Modifier.height(24.dp))

            StoreOverviewPlaceholder(
                store = lastStore,
                totalSales = totalSales,
                formattedRevenue = formattedRevenue,
                formattedProfit = formattedProfit
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
private fun HomeScreenLightPreview() {
    PhoebeStoreTheme {
        HomeScreenContent(
            lastStore = previewStore,
            totalSales = 8,
            formattedRevenue = "240.00",
            formattedProfit = "90.00",
            welcomeMessage = "Welcome back!",
            onNavigateToStoreList = {},
            onNavigateToStoreDetail = { _ -> }
        )
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun HomeScreenDarkPreview() {
    PhoebeStoreTheme {
        HomeScreenContent(
            lastStore = previewStore,
            totalSales = 8,
            formattedRevenue = "240.00",
            formattedProfit = "90.00",
            welcomeMessage = "Welcome back!",
            onNavigateToStoreList = {},
            onNavigateToStoreDetail = { _ -> }
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun HomeScreenEmptyLightPreview() {
    PhoebeStoreTheme {
        HomeScreenContent(
            lastStore = null,
            totalSales = 0,
            formattedRevenue = "0.00",
            formattedProfit = "0.00",
            welcomeMessage = "Ready to manage your stores?",
            onNavigateToStoreList = {},
            onNavigateToStoreDetail = { _ -> }
        )
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun HomeScreenEmptyDarkPreview() {
    PhoebeStoreTheme {
        HomeScreenContent(
            lastStore = null,
            totalSales = 0,
            formattedRevenue = "0.00",
            formattedProfit = "0.00",
            welcomeMessage = "Ready to manage your stores?",
            onNavigateToStoreList = {},
            onNavigateToStoreDetail = { _ -> }
        )
    }
}
