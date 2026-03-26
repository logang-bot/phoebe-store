package com.example.phoebestore.ui.screen.sale

import android.content.res.Configuration
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.phoebestore.R
import com.example.phoebestore.domain.model.ProfitOutcome
import com.example.phoebestore.ui.theme.PhoebeStoreTheme
import com.example.phoebestore.ui.theme.warningDark
import com.example.phoebestore.ui.theme.warningLight
import kotlin.math.abs

@Composable
fun SaleModificationInfo(
    visible: Boolean,
    isPriceModified: Boolean,
    isCostModified: Boolean,
    profitOutcome: ProfitOutcome,
    currencyName: String,
    unitPrice: String,
    unitCost: String,
    profitDelta: Double,
    currentProfit: Double,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn() + expandVertically(),
        exit = fadeOut() + shrinkVertically(),
        modifier = modifier
    ) {
        val warningColor = if (isSystemInDarkTheme()) warningDark else warningLight
        val infoColor = when (profitOutcome) {
            ProfitOutcome.EXTRA_PROFIT -> MaterialTheme.colorScheme.tertiary
            ProfitOutcome.SMALLER_PROFIT -> warningColor
            ProfitOutcome.LOSS -> MaterialTheme.colorScheme.error
            ProfitOutcome.NORMAL_PROFIT -> MaterialTheme.colorScheme.onSurface
        }
        val subtitle = when {
            isPriceModified && isCostModified -> stringResource(R.string.record_sale_modification_price_and_cost_changed)
            isPriceModified -> stringResource(R.string.record_sale_modification_price_changed)
            else -> stringResource(R.string.record_sale_modification_cost_changed)
        }
        val header = stringResource(
            R.string.record_sale_modification_header,
            currencyName,
            "%.2f".format(unitPrice.toDoubleOrNull() ?: 0.0),
            "%.2f".format(unitCost.toDoubleOrNull() ?: 0.0)
        )
        val extraProfitText = stringResource(R.string.record_sale_modification_extra_profit)
        val smallerProfitText = stringResource(R.string.record_sale_modification_smaller_profit)
        val lossText = stringResource(R.string.record_sale_modification_loss)
        val comparedText = stringResource(R.string.record_sale_modification_compared_to_standard)
        val boldStyle = SpanStyle(fontWeight = FontWeight.Bold)
        val body = buildAnnotatedString {
            append(header)
            append(" ")
            when (profitOutcome) {
                ProfitOutcome.EXTRA_PROFIT -> {
                    append(extraProfitText)
                    append(" ")
                    withStyle(boldStyle) { append("$currencyName ${"%.2f".format(profitDelta)}") }
                    append(" ")
                    append(comparedText)
                }
                ProfitOutcome.SMALLER_PROFIT -> {
                    append(smallerProfitText)
                    append(" ")
                    withStyle(boldStyle) { append("$currencyName ${"%.2f".format(abs(profitDelta))}") }
                    append(" ")
                    append(comparedText)
                }
                ProfitOutcome.LOSS -> {
                    append(lossText)
                    append(" ")
                    withStyle(boldStyle) { append("$currencyName ${"%.2f".format(abs(currentProfit))}") }
                    append(".")
                }
                ProfitOutcome.NORMAL_PROFIT -> Unit
            }
        }

        Column(modifier = Modifier.padding(top = 12.dp)) {
            Text(
                text = subtitle,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
                color = infoColor
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = body,
                style = MaterialTheme.typography.bodyMedium,
                color = infoColor
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun SaleModificationInfoExtraProfitLightPreview() {
    PhoebeStoreTheme {
        SaleModificationInfo(
            visible = true,
            isPriceModified = true,
            isCostModified = false,
            profitOutcome = ProfitOutcome.EXTRA_PROFIT,
            currencyName = "USD",
            unitPrice = "12.00",
            unitCost = "5.00",
            profitDelta = 2.00,
            currentProfit = 7.00
        )
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun SaleModificationInfoExtraProfitDarkPreview() {
    PhoebeStoreTheme {
        SaleModificationInfo(
            visible = true,
            isPriceModified = true,
            isCostModified = false,
            profitOutcome = ProfitOutcome.EXTRA_PROFIT,
            currencyName = "USD",
            unitPrice = "12.00",
            unitCost = "5.00",
            profitDelta = 2.00,
            currentProfit = 7.00
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun SaleModificationInfoSmallerProfitLightPreview() {
    PhoebeStoreTheme {
        SaleModificationInfo(
            visible = true,
            isPriceModified = false,
            isCostModified = true,
            profitOutcome = ProfitOutcome.SMALLER_PROFIT,
            currencyName = "BOB",
            unitPrice = "10.00",
            unitCost = "7.00",
            profitDelta = -2.00,
            currentProfit = 3.00
        )
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun SaleModificationInfoSmallerProfitDarkPreview() {
    PhoebeStoreTheme {
        SaleModificationInfo(
            visible = true,
            isPriceModified = false,
            isCostModified = true,
            profitOutcome = ProfitOutcome.SMALLER_PROFIT,
            currencyName = "BOB",
            unitPrice = "10.00",
            unitCost = "7.00",
            profitDelta = -2.00,
            currentProfit = 3.00
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun SaleModificationInfoLossLightPreview() {
    PhoebeStoreTheme {
        SaleModificationInfo(
            visible = true,
            isPriceModified = true,
            isCostModified = true,
            profitOutcome = ProfitOutcome.LOSS,
            currencyName = "USD",
            unitPrice = "4.00",
            unitCost = "6.00",
            profitDelta = -4.00,
            currentProfit = -2.00
        )
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun SaleModificationInfoLossDarkPreview() {
    PhoebeStoreTheme {
        SaleModificationInfo(
            visible = true,
            isPriceModified = true,
            isCostModified = true,
            profitOutcome = ProfitOutcome.LOSS,
            currencyName = "USD",
            unitPrice = "4.00",
            unitCost = "6.00",
            profitDelta = -4.00,
            currentProfit = -2.00
        )
    }
}
