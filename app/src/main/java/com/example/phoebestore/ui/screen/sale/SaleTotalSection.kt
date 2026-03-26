package com.example.phoebestore.ui.screen.sale

import android.content.res.Configuration
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
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
import com.example.phoebestore.ui.theme.PhoebeStoreTheme

@Composable
fun SaleTotalSection(
    totalAmount: Double,
    currencyName: String,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = totalAmount > 0.0,
        enter = fadeIn() + expandVertically(),
        exit = fadeOut() + shrinkVertically(),
        modifier = modifier
    ) {
        Column {
            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "${stringResource(R.string.record_sale_total_label)}: $currencyName ${"%.2f".format(totalAmount)}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(8.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun SaleTotalSectionLightPreview() {
    PhoebeStoreTheme {
        SaleTotalSection(
            totalAmount = 35.50,
            currencyName = "USD"
        )
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun SaleTotalSectionDarkPreview() {
    PhoebeStoreTheme {
        SaleTotalSection(
            totalAmount = 35.50,
            currencyName = "BOB"
        )
    }
}
