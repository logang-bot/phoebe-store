package com.example.phoebestore.ui.screen.home

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CardDefaults
import com.example.phoebestore.ui.common.ThemedCard
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.phoebestore.R
import com.example.phoebestore.domain.model.Currency
import com.example.phoebestore.domain.model.Store

import com.example.phoebestore.ui.theme.PhoebeStoreTheme

@Composable
internal fun StoreOverviewPlaceholder(
    store: Store?,
    totalSales: Int,
    formattedRevenue: String,
    formattedProfit: String,
    totalStock: Int,
    lowStockAlerts: String?,
    modifier: Modifier = Modifier
) {
    if (store == null) return

    val rows = listOf(
        stringResource(R.string.home_overview_total_sales) to "$totalSales",
        stringResource(R.string.home_overview_revenue) to formattedRevenue,
        stringResource(R.string.home_overview_profit) to formattedProfit,
        stringResource(R.string.home_overview_products_in_stock) to "$totalStock",
        stringResource(R.string.home_overview_low_stock_alerts) to
            (lowStockAlerts ?: stringResource(R.string.home_overview_low_stock_none))
    )

    ThemedCard(
        modifier = modifier.fillMaxWidth(),
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
                OverviewRow(label, value)
            }
        }
    }
}

@Composable
private fun OverviewRow(label: String, value: String) {
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

private val previewStore = Store(
    id = 1L,
    name = "Phoebe's Boutique",
    currency = Currency.USD
)

@Preview(showBackground = true)
@Composable
private fun StoreOverviewPlaceholderLightPreview() {
    PhoebeStoreTheme {
        StoreOverviewPlaceholder(
            store = previewStore,
            totalSales = 8,
            formattedRevenue = "240.00",
            formattedProfit = "90.00",
            totalStock = 42,
            lowStockAlerts = "Lipstick, Mascara, Blush",
        )
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun StoreOverviewPlaceholderDarkPreview() {
    PhoebeStoreTheme {
        StoreOverviewPlaceholder(
            store = previewStore,
            totalSales = 8,
            formattedRevenue = "240.00",
            formattedProfit = "90.00",
            totalStock = 42,
            lowStockAlerts = "Lipstick, Mascara, Blush",
        )
    }
}
