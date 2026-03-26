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
    modifier: Modifier = Modifier
) {
    if (store == null) return

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

            OverviewRow(stringResource(R.string.home_overview_total_sales), stringResource(R.string.home_overview_placeholder_value))
            Spacer(modifier = Modifier.height(8.dp))
            OverviewRow(stringResource(R.string.home_overview_revenue), stringResource(R.string.home_overview_placeholder_value))
            Spacer(modifier = Modifier.height(8.dp))
            OverviewRow(stringResource(R.string.home_overview_profit), stringResource(R.string.home_overview_placeholder_value))
            Spacer(modifier = Modifier.height(8.dp))
            OverviewRow(stringResource(R.string.home_overview_products_in_stock), stringResource(R.string.home_overview_placeholder_value))
            Spacer(modifier = Modifier.height(8.dp))
            OverviewRow(stringResource(R.string.home_overview_low_stock_alerts), stringResource(R.string.home_overview_placeholder_value))
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
        StoreOverviewPlaceholder(store = previewStore)
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun StoreOverviewPlaceholderDarkPreview() {
    PhoebeStoreTheme {
        StoreOverviewPlaceholder(store = previewStore)
    }
}
