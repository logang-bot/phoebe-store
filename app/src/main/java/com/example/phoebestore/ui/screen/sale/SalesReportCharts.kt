package com.example.phoebestore.ui.screen.sale

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.phoebestore.R
import com.example.phoebestore.domain.model.ProfitOutcome

@Composable
internal fun InventoryChart(items: List<InventoryBarItem>, modifier: Modifier = Modifier) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = stringResource(R.string.sales_report_inventory_title),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )
        items.forEach { item ->
            InventoryBarRow(item = item, modifier = Modifier.fillMaxWidth())
        }
    }
}

@Composable
private fun InventoryBarRow(item: InventoryBarItem, modifier: Modifier = Modifier) {
    val animatable = remember(item) { Animatable(0f) }
    LaunchedEffect(item) {
        animatable.snapTo(0f)
        animatable.animateTo(item.fraction, animationSpec = tween(800, easing = FastOutSlowInEasing))
    }
    val primary = MaterialTheme.colorScheme.primary
    val bgColor = MaterialTheme.colorScheme.surfaceVariant

    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        Column(modifier = Modifier.width(110.dp)) {
            Text(
                text = item.productName,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = stringResource(R.string.product_card_stock, item.currentStock),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Spacer(Modifier.width(8.dp))
        Box(modifier = Modifier.weight(1f).height(24.dp)) {
            Box(Modifier.fillMaxSize().clip(RoundedCornerShape(12.dp)).background(bgColor))
            Box(Modifier.fillMaxWidth(animatable.value).fillMaxHeight().clip(RoundedCornerShape(12.dp)).background(primary))
        }
        Spacer(Modifier.width(8.dp))
        Text(
            text = "×${item.soldUnits}",
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.width(44.dp),
            textAlign = TextAlign.End
        )
    }
}

@Composable
internal fun DailyRevenueChart(items: List<DailyRevenueItem>, modifier: Modifier = Modifier) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = stringResource(R.string.sales_report_revenue_over_time_title),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )
        Row(
            modifier = Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.Bottom
        ) {
            items.forEach { item -> DailyRevenueBar(item = item) }
        }
    }
}

@Composable
private fun DailyRevenueBar(item: DailyRevenueItem, modifier: Modifier = Modifier) {
    val animatable = remember(item) { Animatable(0f) }
    LaunchedEffect(item) {
        animatable.snapTo(0f)
        animatable.animateTo(item.fraction, animationSpec = tween(800, easing = FastOutSlowInEasing))
    }
    val primary = MaterialTheme.colorScheme.primary
    val bgColor = MaterialTheme.colorScheme.surfaceVariant

    Column(
        modifier = modifier.width(36.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Box(modifier = Modifier.width(20.dp).height(80.dp)) {
            Box(Modifier.fillMaxSize().clip(RoundedCornerShape(4.dp)).background(bgColor))
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .fillMaxHeight(animatable.value)
                    .clip(RoundedCornerShape(4.dp))
                    .background(primary)
            )
        }
        Text(
            text = item.dayLabel,
            style = MaterialTheme.typography.labelSmall,
            textAlign = TextAlign.Center,
            maxLines = 1
        )
    }
}

@Composable
internal fun ProfitOutcomeChart(items: List<ProfitOutcomeBreakdownItem>, modifier: Modifier = Modifier) {
    val animatable = remember(items) { Animatable(0f) }
    LaunchedEffect(items) {
        animatable.snapTo(0f)
        animatable.animateTo(1f, animationSpec = tween(800, easing = FastOutSlowInEasing))
    }
    val bgColor = MaterialTheme.colorScheme.surfaceVariant

    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = stringResource(R.string.sales_report_profit_outcome_title),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )
        Box(modifier = Modifier.fillMaxWidth().height(24.dp)) {
            Box(Modifier.fillMaxSize().clip(RoundedCornerShape(12.dp)).background(bgColor))
            Row(modifier = Modifier.fillMaxWidth(animatable.value).fillMaxHeight().clip(RoundedCornerShape(12.dp))) {
                items.forEach { item ->
                    Box(Modifier.weight(item.fraction).fillMaxHeight().background(outcomeColor(item.outcome)))
                }
            }
        }
        OutcomeLegend(items = items, modifier = Modifier.fillMaxWidth())
    }
}

@Composable
private fun OutcomeLegend(items: List<ProfitOutcomeBreakdownItem>, modifier: Modifier = Modifier) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(6.dp)) {
        items.forEach { item ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(Modifier.size(10.dp).clip(CircleShape).background(outcomeColor(item.outcome)))
                Text(outcomeLabel(item.outcome), style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(1f))
                Text("×${item.count}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun outcomeColor(outcome: ProfitOutcome): Color = when (outcome) {
    ProfitOutcome.NORMAL_PROFIT -> MaterialTheme.colorScheme.primary
    ProfitOutcome.EXTRA_PROFIT -> MaterialTheme.colorScheme.tertiary
    ProfitOutcome.SMALLER_PROFIT -> MaterialTheme.colorScheme.secondary
    ProfitOutcome.LOSS -> MaterialTheme.colorScheme.error
}

@Composable
private fun outcomeLabel(outcome: ProfitOutcome): String = when (outcome) {
    ProfitOutcome.NORMAL_PROFIT -> stringResource(R.string.sales_report_outcome_normal)
    ProfitOutcome.EXTRA_PROFIT -> stringResource(R.string.sales_report_outcome_extra)
    ProfitOutcome.SMALLER_PROFIT -> stringResource(R.string.sales_report_outcome_smaller)
    ProfitOutcome.LOSS -> stringResource(R.string.sales_report_outcome_loss)
}
