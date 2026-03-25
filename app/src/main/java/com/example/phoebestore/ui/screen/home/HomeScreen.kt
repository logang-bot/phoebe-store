package com.example.phoebestore.ui.screen.home

import android.content.res.Configuration
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import com.example.phoebestore.ui.theme.PhoebeStoreTheme

@Composable
fun HomeScreen(
    onNavigateToStoreList: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val lastStore by viewModel.lastStore.collectAsStateWithLifecycle()
    val welcomeMessages = stringArrayResource(R.array.home_welcome_messages)
    val welcomeMessage = remember { welcomeMessages.random() }

    HomeScreenContent(
        lastStore = lastStore,
        welcomeMessage = welcomeMessage,
        onNavigateToStoreList = onNavigateToStoreList
    )
}

@Composable
private fun HomeScreenContent(
    lastStore: Store?,
    welcomeMessage: String,
    onNavigateToStoreList: () -> Unit
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
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(24.dp))

            LastStoreCard(
                store = lastStore,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = onNavigateToStoreList,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(R.string.home_view_all_stores))
            }

            Spacer(modifier = Modifier.height(24.dp))

            StoreOverviewPlaceholder(store = lastStore)
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
            welcomeMessage = "Welcome back!",
            onNavigateToStoreList = {}
        )
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun HomeScreenDarkPreview() {
    PhoebeStoreTheme {
        HomeScreenContent(
            lastStore = previewStore,
            welcomeMessage = "Welcome back!",
            onNavigateToStoreList = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun HomeScreenEmptyLightPreview() {
    PhoebeStoreTheme {
        HomeScreenContent(
            lastStore = null,
            welcomeMessage = "Ready to manage your stores?",
            onNavigateToStoreList = {}
        )
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun HomeScreenEmptyDarkPreview() {
    PhoebeStoreTheme {
        HomeScreenContent(
            lastStore = null,
            welcomeMessage = "Ready to manage your stores?",
            onNavigateToStoreList = {}
        )
    }
}
